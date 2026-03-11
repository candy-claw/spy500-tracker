package com.spy500.tracker.service;

import com.spy500.tracker.entity.Stock;
import com.spy500.tracker.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class StockService {
    
    private final StockRepository stockRepository;
    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // S&P 500 ticker list (common large caps)
    private static final List<String> SP500_TICKERS = Arrays.asList(
            "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "META", "TSLA", "BRK-B", "UNH", "JNJ",
            "V", "XOM", "JPM", "PG", "MA", "HD", "CVX", "LLY", "ABBV", "MRK",
            "PEP", "KO", "COST", "AVGO", "TMO", "WMT", "MCD", "CSCO", "ACN", "ABT",
            "DHR", "CRM", "ADBE", "NKE", "TXN", "PM", "NEE", "BMY", "UNP", "RTX",
            "HON", "LOW", "QCOM", "INTC", "IBM", "CAT", "AMD", "GE", "INTU", "SBUX",
            "GS", "BLK", "AMGN", "GILD", "DE", "PLD", "ADI", "CI", "TJX", "MMC",
            "VRTX", "ADP", "LRCX", "MDT", "SYK", "CVS", "MO", "ZTS", "REGN", "CB",
            "PNC", "BKNG", "T", "SCHW", "BA", "AMAT", "ISRG", "C", "TMUS", "MMM",
            "SPGI", "USB", "BDX", "ITW", "APD", "EL", "CL", "NOC", "MU", "ICE",
            "SO", "MCO", "AON", "FIS", "MCK", "SHW", "DG", "EOG", "HUM", "BSX"
    );

    @Transactional
    public void initializeStocks() {
        log.info("Initializing S&P 500 stocks...");
        int count = 0;
        for (String symbol : SP500_TICKERS) {
            if (!stockRepository.existsBySymbol(symbol)) {
                Stock stock = new Stock();
                stock.setSymbol(symbol);
                stock.setCompanyName(symbol); // Will be updated when fetching prices
                stockRepository.save(stock);
                count++;
            }
        }
        log.info("Initialized {} new stocks", count);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Stock getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol).orElse(null);
    }

    public List<String> getAllSymbols() {
        return stockRepository.findAll().stream()
                .map(Stock::getSymbol)
                .toList();
    }
}
