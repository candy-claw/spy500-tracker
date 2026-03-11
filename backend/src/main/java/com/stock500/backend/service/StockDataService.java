package com.stock500.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock500.backend.entity.DailyPrice;
import com.stock500.backend.entity.Stock;
import com.stock500.backend.repository.DailyPriceRepository;
import com.stock500.backend.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class StockDataService {

    private static final Logger log = LoggerFactory.getLogger(StockDataService.class);

    private final StockRepository stockRepository;
    private final DailyPriceRepository dailyPriceRepository;
    private final ObjectMapper objectMapper;

    public StockDataService(StockRepository stockRepository, DailyPriceRepository dailyPriceRepository, ObjectMapper objectMapper) {
        this.stockRepository = stockRepository;
        this.dailyPriceRepository = dailyPriceRepository;
        this.objectMapper = objectMapper;
    }

    private static final String PYTHON_SCRIPT = "/Users/canday/.openclaw/scripts/stock_scraper.py";

    // S&P 500 tickers (backup list if Python script fails)
    private static final List<StockInfo> SP500_TICKERS = List.of(
        new StockInfo("AAPL",  "Apple Inc.",                       "Technology",          "Consumer Electronics"),
        new StockInfo("MSFT",  "Microsoft Corporation",            "Technology",          "Software"),
        new StockInfo("GOOGL", "Alphabet Inc.",                    "Communication Services","Internet Content & Information"),
        new StockInfo("AMZN",  "Amazon.com Inc.",                  "Consumer Discretionary","Broadline Retail"),
        new StockInfo("META",  "Meta Platforms Inc.",              "Communication Services","Internet Content & Information"),
        new StockInfo("NVDA",  "NVIDIA Corporation",              "Technology",          "Semiconductors"),
        new StockInfo("TSLA",  "Tesla Inc.",                       "Consumer Discretionary","Auto Manufacturers"),
        new StockInfo("BRK-B", "Berkshire Hathaway Inc.",         "Financials",          "Insurance"),
        new StockInfo("JPM",   "JPMorgan Chase & Co.",             "Financials",          "Banks"),
        new StockInfo("V",     "Visa Inc.",                        "Financials",          "Credit Services"),
        new StockInfo("UNH",   "UnitedHealth Group Inc.",          "Healthcare",          "Healthcare Plans"),
        new StockInfo("JNJ",   "Johnson & Johnson",                "Healthcare",          "Drug Manufacturers"),
        new StockInfo("XOM",   "Exxon Mobil Corporation",          "Energy",              "Oil & Gas Integrated"),
        new StockInfo("PG",    "Procter & Gamble Co.",             "Consumer Staples",    "Household Products"),
        new StockInfo("MA",    "Mastercard Incorporated",          "Financials",          "Credit Services"),
        new StockInfo("HD",    "The Home Depot Inc.",              "Consumer Discretionary","Home Improvement Retail"),
        new StockInfo("CVX",   "Chevron Corporation",              "Energy",              "Oil & Gas Integrated"),
        new StockInfo("ABBV",  "AbbVie Inc.",                      "Healthcare",          "Drug Manufacturers"),
        new StockInfo("MRK",   "Merck & Co. Inc.",                 "Healthcare",          "Drug Manufacturers"),
        new StockInfo("LLY",   "Eli Lilly and Company",            "Healthcare",          "Drug Manufacturers"),
        new StockInfo("PEP",   "PepsiCo Inc.",                     "Consumer Staples",    "Beverages"),
        new StockInfo("KO",    "The Coca-Cola Company",            "Consumer Staples",    "Beverages"),
        new StockInfo("COST",  "Costco Wholesale Corporation",     "Consumer Staples",    "Discount Stores"),
        new StockInfo("AVGO",  "Broadcom Inc.",                    "Technology",          "Semiconductors"),
        new StockInfo("CSCO",  "Cisco Systems Inc.",               "Technology",          "Communication Equipment"),
        new StockInfo("ACN",   "Accenture plc",                    "Technology",          "IT Services"),
        new StockInfo("MCD",   "McDonald's Corporation",           "Consumer Discretionary","Restaurants"),
        new StockInfo("WMT",   "Walmart Inc.",                     "Consumer Staples",    "Discount Stores"),
        new StockInfo("BAC",   "Bank of America Corporation",      "Financials",          "Banks"),
        new StockInfo("CRM",   "Salesforce Inc.",                  "Technology",          "Software"),
        new StockInfo("TMO",   "Thermo Fisher Scientific Inc.",    "Healthcare",          "Diagnostics"),
        new StockInfo("ABT",   "Abbott Laboratories",              "Healthcare",          "Medical Devices"),
        new StockInfo("NEE",   "NextEra Energy Inc.",              "Utilities",           "Utilities Regulated Electric"),
        new StockInfo("NKE",   "NIKE Inc.",                        "Consumer Discretionary","Footwear & Accessories"),
        new StockInfo("ORCL",  "Oracle Corporation",               "Technology",          "Software"),
        new StockInfo("DHR",   "Danaher Corporation",              "Healthcare",          "Diagnostics"),
        new StockInfo("TXN",   "Texas Instruments Incorporated",   "Technology",          "Semiconductors"),
        new StockInfo("PM",    "Philip Morris International Inc.", "Consumer Staples",    "Tobacco"),
        new StockInfo("UNP",   "Union Pacific Corporation",        "Industrials",         "Railroads"),
        new StockInfo("LOW",   "Lowe's Companies Inc.",            "Consumer Discretionary","Home Improvement Retail"),
        new StockInfo("AMGN",  "Amgen Inc.",                       "Healthcare",          "Drug Manufacturers"),
        new StockInfo("SBUX",  "Starbucks Corporation",            "Consumer Discretionary","Restaurants"),
        new StockInfo("INTU",  "Intuit Inc.",                      "Technology",          "Software"),
        new StockInfo("SPGI",  "S&P Global Inc.",                  "Financials",          "Financial Data & Stock Exchanges"),
        new StockInfo("GS",    "The Goldman Sachs Group Inc.",     "Financials",          "Banks"),
        new StockInfo("MS",    "Morgan Stanley",                   "Financials",          "Banks"),
        new StockInfo("BLK",   "BlackRock Inc.",                   "Financials",          "Asset Management"),
        new StockInfo("AXP",   "American Express Company",         "Financials",          "Credit Services"),
        new StockInfo("CAT",   "Caterpillar Inc.",                 "Industrials",         "Farm & Heavy Construction Machinery"),
        new StockInfo("DE",    "Deere & Company",                  "Industrials",         "Farm & Heavy Construction Machinery")
    );

    /**
     * Fetch stock data from Finviz using Python scraper
     */
    public void fetchAndStoreDailyPrices() {
        log.info("Starting S&P 500 data fetch from Finviz");

        try {
            // Run Python scraper
            List<Map<String, Object>> scrapedData = runPythonScraper();
            
            if (scrapedData.isEmpty()) {
                log.warn("Python scraper returned no data, using fallback");
                fetchFromFallback();
                return;
            }

            // Process scraped data
            int success = 0, failed = 0;
            for (Map<String, Object> data : scrapedData) {
                try {
                    String symbol = (String) data.get("symbol");
                    String tradeDateStr = (String) data.get("trade_date");
                    LocalDate tradeDate = LocalDate.parse(tradeDateStr);

                    // Get or create stock
                    Stock stock = stockRepository.findById(symbol).orElseGet(() -> {
                        Stock s = Stock.builder()
                                .symbol(symbol)
                                .name((String) data.get("name"))
                                .sector((String) data.get("sector"))
                                .industry((String) data.get("industry"))
                                .build();
                        return stockRepository.save(s);
                    });

                    // Check if daily price already exists
                    if (dailyPriceRepository.findBySymbolAndTradeDate(symbol, tradeDate).isPresent()) {
                        log.debug("Skipping {} for {} - already exists", symbol, tradeDate);
                        continue;
                    }

                    // Parse numeric values
                    BigDecimal peRatio = parseBigDecimal(data.get("pe_ratio"));
                    BigDecimal eps = parseBigDecimal(data.get("eps"));
                    BigDecimal dividend = parseBigDecimal(data.get("dividend"));
                    Long volume = parseLong(data.get("volume"));
                    String marketCap = (String) data.get("market_cap");

                    // Create daily price record
                    DailyPrice dp = DailyPrice.builder()
                            .symbol(symbol)
                            .tradeDate(tradeDate)
                            .peRatio(peRatio)
                            .marketCap(marketCap)
                            .eps(eps)
                            .volume(volume)
                            .dividend(dividend)
                            .build();

                    dailyPriceRepository.save(dp);
                    success++;
                    log.debug("Saved {} for {}", symbol, tradeDate);

                } catch (Exception e) {
                    log.warn("Failed to process {}: {}", data.get("symbol"), e.getMessage());
                    failed++;
                }
            }

            log.info("Fetch complete: {} success, {} failed", success, failed);

        } catch (Exception e) {
            log.error("Error running Python scraper: {}", e.getMessage());
            fetchFromFallback();
        }
    }

    /**
     * Run Python scraper and parse JSON output
     */
    private List<Map<String, Object>> runPythonScraper() throws Exception {
        log.info("Running Python scraper: {}", PYTHON_SCRIPT);

        ProcessBuilder pb = new ProcessBuilder("python3", PYTHON_SCRIPT);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script exited with code: " + exitCode);
        }

        String jsonOutput = output.toString();
        if (jsonOutput.isBlank()) {
            return Collections.emptyList();
        }

        return objectMapper.readValue(jsonOutput, new TypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * Fallback: use static stock info if scraper fails
     */
    private void fetchFromFallback() {
        log.info("Using fallback - storing static stock data");

        // Ensure all stocks exist in the DB
        for (StockInfo info : SP500_TICKERS) {
            stockRepository.findById(info.symbol()).ifPresentOrElse(
                s -> { /* already exists */ },
                () -> stockRepository.save(Stock.builder()
                        .symbol(info.symbol())
                        .name(info.name())
                        .sector(info.sector())
                        .industry(info.industry())
                        .build())
            );
        }
        log.info("Fallback complete: {} stocks stored", SP500_TICKERS.size());
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<StockInfo> getTickers() {
        return SP500_TICKERS;
    }

    public record StockInfo(String symbol, String name, String sector, String industry) {}
}
