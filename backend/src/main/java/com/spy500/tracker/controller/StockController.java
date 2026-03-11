package com.spy500.tracker.controller;

import com.spy500.tracker.entity.Stock;
import com.spy500.tracker.entity.StockPrice;
import com.spy500.tracker.service.StockPriceService;
import com.spy500.tracker.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    private final StockService stockService;
    private final StockPriceService stockPriceService;

    public StockController(StockService stockService, StockPriceService stockPriceService) {
        this.stockService = stockService;
        this.stockPriceService = stockPriceService;
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> getStock(@PathVariable String symbol) {
        Stock stock = stockService.getStockBySymbol(symbol.toUpperCase());
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/{symbol}/prices")
    public ResponseEntity<List<StockPrice>> getStockPrices(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(stockPriceService.getStockPriceHistory(symbol.toUpperCase(), days));
    }

    @GetMapping("/prices/latest")
    public ResponseEntity<List<StockPrice>> getLatestPrices() {
        return ResponseEntity.ok(stockPriceService.getLatestPrices());
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeStocks() {
        stockService.initializeStocks();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Stocks initialized successfully - " + stockService.getAllStocks().size() + " stocks added");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fetch-prices")
    public ResponseEntity<Map<String, String>> fetchPrices() {
        try {
            stockPriceService.fetchDailyPricesForFewStocks(10); // Only fetch for 10 stocks at a time
            Map<String, String> response = new HashMap<>();
            response.put("message", "Prices fetched for 10 stocks (rate limited)");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
