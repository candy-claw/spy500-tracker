package com.stock500.backend.service;

import com.stock500.backend.entity.DailyPrice;
import com.stock500.backend.entity.Stock;
import com.stock500.backend.repository.DailyPriceRepository;
import com.stock500.backend.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final StockDataService stockDataService;

    public StockService(StockRepository stockRepository, DailyPriceRepository dailyPriceRepository, StockDataService stockDataService) {
        this.stockRepository = stockRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.stockDataService = stockDataService;
    }

    public Page<Stock> getAllStocks(String sector, String search, Pageable pageable) {
        return stockRepository.searchByNameOrSymbol(sector, search, pageable);
    }

    public List<Map<String, Object>> getStocksWithPrices() {
        List<Stock> allStocks = stockRepository.findAll();
        List<DailyPrice> latestPrices = dailyPriceRepository.findAll();
        
        // Get latest price for each symbol
        Map<String, DailyPrice> latestMap = new HashMap<>();
        for (DailyPrice dp : latestPrices) {
            String symbol = dp.getSymbol();
            if (!latestMap.containsKey(symbol) || 
                dp.getTradeDate().isAfter(latestMap.get(symbol).getTradeDate())) {
                latestMap.put(symbol, dp);
            }
        }
        
        return allStocks.stream().map(stock -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("symbol", stock.getSymbol());
            item.put("name", stock.getName());
            item.put("sector", stock.getSector());
            item.put("industry", stock.getIndustry());
            item.put("createdAt", stock.getCreatedAt());
            item.put("dailyPrice", latestMap.get(stock.getSymbol()));
            return item;
        }).collect(Collectors.toList());
    }

    public DailyPrice getLatestPrice(String symbol) {
        List<DailyPrice> prices = dailyPriceRepository.findBySymbolOrderByTradeDateDesc(symbol);
        return prices.isEmpty() ? null : prices.get(0);
    }

    public List<String> getSectors() {
        return stockRepository.findDistinctSectors();
    }

    public Map<String, Object> getStockDetail(String symbol) {
        Stock stock = stockRepository.findById(symbol)
                .orElseThrow(() -> new NoSuchElementException("Stock not found: " + symbol));

        List<DailyPrice> prices = dailyPriceRepository
                .findBySymbolOrderByTradeDateDesc(symbol);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stock", stock);
        if (!prices.isEmpty()) {
            result.put("latestPrice", prices.get(0));
            result.put("prices", prices);
        }
        return result;
    }

    public List<DailyPrice> getStockPrices(String symbol, LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(6);
        if (to == null)   to   = LocalDate.now();
        return dailyPriceRepository.findBySymbolAndTradeDateBetweenOrderByTradeDateAsc(symbol, from, to);
    }

    public Page<Map<String, Object>> getLatestPrices(String sector, String search, Pageable pageable) {
        Page<Stock> stockPage = stockRepository.searchByNameOrSymbol(sector, search, pageable);
        List<String> symbols = stockPage.getContent().stream()
                .map(Stock::getSymbol).collect(Collectors.toList());

        List<DailyPrice> latestPrices = symbols.isEmpty()
                ? Collections.emptyList()
                : dailyPriceRepository.findLatestPriceForSymbols(symbols);

        Map<String, DailyPrice> priceMap = latestPrices.stream()
                .collect(Collectors.toMap(DailyPrice::getSymbol, p -> p, (a, b) -> a));

        List<Map<String, Object>> content = stockPage.getContent().stream().map(stock -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stock", stock);
            item.put("dailyPrice", priceMap.get(stock.getSymbol()));
            return item;
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, stockPage.getTotalElements());
    }

    @Transactional
    public void triggerRefresh() {
        log.info("Manual refresh triggered");
        stockDataService.fetchAndStoreDailyPrices();
    }

    public List<Map<String, Object>> getMovers(int limit) {
        // Return stocks sorted by P/E ratio as a simple "movers" concept
        // In a real app, this would compute price changes
        List<Stock> allStocks = stockRepository.findAll();
        
        List<Map<String, Object>> movers = new ArrayList<>();
        
        for (Stock stock : allStocks) {
            List<DailyPrice> prices = dailyPriceRepository.findLatestPriceForSymbols(
                List.of(stock.getSymbol()));
            
            if (!prices.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("stock", stock);
                item.put("dailyPrice", prices.get(0));
                movers.add(item);
            }
        }
        
        // Sort by P/E ratio (higher first) as a placeholder for "movers"
        movers.sort((a, b) -> {
            DailyPrice pa = (DailyPrice) a.get("dailyPrice");
            DailyPrice pb = (DailyPrice) b.get("dailyPrice");
            if (pa == null || pa.getPeRatio() == null) return 1;
            if (pb == null || pb.getPeRatio() == null) return -1;
            return pb.getPeRatio().compareTo(pa.getPeRatio());
        });
        
        return movers.stream().limit(limit).collect(Collectors.toList());
    }
}
