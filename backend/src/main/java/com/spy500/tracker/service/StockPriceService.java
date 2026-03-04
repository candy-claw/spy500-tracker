package com.spy500.tracker.service;

import com.spy500.tracker.entity.Stock;
import com.spy500.tracker.entity.StockPrice;
import com.spy500.tracker.repository.StockPriceRepository;
import com.spy500.tracker.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class StockPriceService {

    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    // Alpha Vantage API - FREE tier (5 calls/minute, 500/day)
    // Get free API key from: https://www.alphavantage.co/support/#api-key
    @Value("${alphavantage.api.key:demo}")
    private String apiKey;
    
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long TIME_WINDOW_MS = 60000;
    
    private long requestCount = 0;
    private long windowStart = System.currentTimeMillis();

    public StockPriceService(StockPriceRepository stockPriceRepository, StockRepository stockRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockRepository = stockRepository;
    }

    private void applyRateLimit() {
        long now = System.currentTimeMillis();
        if (now - windowStart > TIME_WINDOW_MS) {
            windowStart = now;
            requestCount = 0;
        }
        
        if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
            long waitTime = TIME_WINDOW_MS - (now - windowStart);
            if (waitTime > 0) {
                log.info("Rate limit reached, waiting {} ms", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                windowStart = System.currentTimeMillis();
                requestCount = 0;
            }
        }
        requestCount++;
    }

    /**
     * Fetch stock data from Alpha Vantage
     */
    private String fetchStockData(String symbol) {
        applyRateLimit();
        
        try {
            String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s&outputsize=compact", 
                BASE_URL, symbol, apiKey);
            
            String result = restTemplate.getForObject(url, String.class);
            log.debug("Fetched data for {}", symbol);
            return result;
        } catch (Exception e) {
            log.error("Error fetching {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI")
    @Transactional
    public void fetchDailyPrices() {
        log.info("Starting daily price fetch...");
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                fetchStockPrices(stock);
            } catch (Exception e) {
                log.error("Error fetching prices for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        log.info("Daily price fetch completed");
    }

    public void fetchStockPrices(Stock stock) {
        String symbol = stock.getSymbol();
        
        try {
            String response = fetchStockData(symbol);
            if (response == null || response.isEmpty()) {
                log.warn("No data received for {}", symbol);
                return;
            }
            
            // Parse Alpha Vantage JSON response
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);
            
            // Check for API limit message
            if (responseMap.containsKey("Note") || responseMap.containsKey("Information")) {
                log.warn("API limit reached: {}", responseMap.get("Note"));
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> timeSeries = (Map<String, Object>) responseMap.get("Time Series (Daily)");
            
            if (timeSeries == null || timeSeries.isEmpty()) {
                log.warn("No time series data for {}", symbol);
                return;
            }
            
            Stock savedStock = stockRepository.findBySymbol(symbol).orElse(stock);
            
            // Update company name if needed
            if (savedStock.getCompanyName() == null || savedStock.getCompanyName().equals(symbol)) {
                savedStock.setCompanyName(symbol + " Corp");
                stockRepository.save(savedStock);
            }
            
            // Get last 5 days of data
            int count = 0;
            for (Map.Entry<String, Object> entry : timeSeries.entrySet()) {
                if (count >= 5) break;
                
                String dateStr = entry.getKey();
                LocalDate date = LocalDate.parse(dateStr);
                
                if (stockPriceRepository.findByStockAndPriceDate(savedStock, date).isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> dailyData = (Map<String, String>) entry.getValue();
                    
                    StockPrice price = new StockPrice();
                    price.setStock(savedStock);
                    price.setPriceDate(date);
                    price.setOpenPrice(new BigDecimal(dailyData.get("1. open")).setScale(2, RoundingMode.HALF_UP));
                    price.setHighPrice(new BigDecimal(dailyData.get("2. high")).setScale(2, RoundingMode.HALF_UP));
                    price.setLowPrice(new BigDecimal(dailyData.get("3. low")).setScale(2, RoundingMode.HALF_UP));
                    price.setClosePrice(new BigDecimal(dailyData.get("4. close")).setScale(2, RoundingMode.HALF_UP));
                    price.setVolume(Long.parseLong(dailyData.get("5. volume")));
                    price.setAdjClose(new BigDecimal(dailyData.get("4. close")).setScale(2, RoundingMode.HALF_UP));
                    
                    // Calculate change
                    BigDecimal open = price.getOpenPrice();
                    BigDecimal close = price.getClosePrice();
                    BigDecimal change = close.subtract(open);
                    price.setPriceChange(change.setScale(2, RoundingMode.HALF_UP));
                    if (open.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal changePercent = change.multiply(BigDecimal.valueOf(100))
                                .divide(open, 2, RoundingMode.HALF_UP);
                        price.setPriceChangePercent(changePercent);
                    }
                    
                    stockPriceRepository.save(price);
                    count++;
                }
            }
            log.info("Updated {} prices for {}", count, symbol);
            
        } catch (Exception e) {
            log.error("Error processing {}: {}", symbol, e.getMessage());
        }
    }

    public List<StockPrice> getStockPriceHistory(String symbol, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return stockPriceRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    public List<StockPrice> getLatestPrices() {
        LocalDate today = LocalDate.now();
        List<StockPrice> prices = stockPriceRepository.findByPriceDate(today);
        if (prices.isEmpty()) {
            today = today.minusDays(1);
            prices = stockPriceRepository.findByPriceDate(today);
        }
        return prices;
    }

    public List<Object[]> getTopPerformers(LocalDate date, int limit) {
        return stockPriceRepository.findTopPerformersByDate(date).stream()
                .limit(limit)
                .toList();
    }

    public List<Object[]> getWorstPerformers(LocalDate date, int limit) {
        return stockPriceRepository.findWorstPerformersByDate(date).stream()
                .limit(limit)
                .toList();
    }

    public void fetchDailyPricesForFewStocks(int limit) {
        log.info("Fetching prices for {} stocks (rate limited)...", limit);
        List<Stock> stocks = stockRepository.findAll().stream().limit(limit).toList();

        for (Stock stock : stocks) {
            try {
                fetchStockPrices(stock);
                log.info("Fetched prices for {}", stock.getSymbol());
            } catch (Exception e) {
                log.error("Error fetching prices for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }
        log.info("Price fetch completed for {} stocks", limit);
    }
}
