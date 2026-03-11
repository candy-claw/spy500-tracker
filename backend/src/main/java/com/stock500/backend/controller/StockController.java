package com.stock500.backend.controller;

import com.stock500.backend.entity.DailyPrice;
import com.stock500.backend.entity.Stock;
import com.stock500.backend.service.StockService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")

@CrossOrigin(origins = "http://localhost:5173")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<Page<Stock>> getAllStocks(
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("symbol"));
        return ResponseEntity.ok(stockService.getAllStocks(sector, search, pageable));
    }

    @GetMapping("/with-prices")
    public ResponseEntity<List<Map<String, Object>>> getStocksWithPrices() {
        return ResponseEntity.ok(stockService.getStocksWithPrices());
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors() {
        return ResponseEntity.ok(stockService.getSectors());
    }

    @GetMapping("/latest")
    public ResponseEntity<Page<Map<String, Object>>> getLatestPrices(
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("symbol"));
        return ResponseEntity.ok(stockService.getLatestPrices(sector, search, pageable));
    }

    @GetMapping("/movers")
    public ResponseEntity<List<Map<String, Object>>> getMovers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(stockService.getMovers(limit));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Map<String, Object>> getStockDetail(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockDetail(symbol.toUpperCase()));
    }

    @GetMapping("/{symbol}/prices")
    public ResponseEntity<List<DailyPrice>> getStockPrices(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(stockService.getStockPrices(symbol.toUpperCase(), from, to));
    }

    @GetMapping("/{symbol}/latest")
    public ResponseEntity<DailyPrice> getLatestPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getLatestPrice(symbol.toUpperCase()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> triggerRefresh() {
        stockService.triggerRefresh();
        return ResponseEntity.ok(Map.of("status", "refresh triggered"));
    }

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, String>> triggerFetch() {
        stockService.triggerRefresh();
        return ResponseEntity.ok(Map.of("status", "fetch triggered"));
    }
}
