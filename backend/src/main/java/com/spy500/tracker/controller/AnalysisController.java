package com.spy500.tracker.controller;

import com.spy500.tracker.entity.StockPrice;
import com.spy500.tracker.service.StockPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final StockPriceService stockPriceService;

    public AnalysisController(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    @GetMapping("/top-gainers")
    public ResponseEntity<List<Map<String, Object>>> getTopGainers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String date) {
        
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<Object[]> results = stockPriceService.getTopPerformers(targetDate, limit);
        
        List<Map<String, Object>> gainers = results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("symbol", row[0]);
                    map.put("changePercent", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(gainers);
    }

    @GetMapping("/top-losers")
    public ResponseEntity<List<Map<String, Object>>> getTopLosers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String date) {
        
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<Object[]> results = stockPriceService.getWorstPerformers(targetDate, limit);
        
        List<Map<String, Object>> losers = results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("symbol", row[0]);
                    map.put("changePercent", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(losers);
    }

    @GetMapping("/stock/{symbol}/analysis")
    public ResponseEntity<Map<String, Object>> getStockAnalysis(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {
        
        List<StockPrice> prices = stockPriceService.getStockPriceHistory(symbol.toUpperCase(), days);
        
        if (prices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Calculate statistics
        BigDecimal maxPrice = prices.stream()
                .map(StockPrice::getHighPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal minPrice = prices.stream()
                .map(StockPrice::getLowPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal avgPrice = prices.stream()
                .map(StockPrice::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
        
        Long totalVolume = prices.stream()
                .map(StockPrice::getVolume)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        
        double avgVolumeVal = totalVolume != null ? totalVolume.doubleValue() / prices.size() : 0;
        BigDecimal avgVolume = BigDecimal.valueOf(avgVolumeVal).setScale(0, RoundingMode.HALF_UP);
        
        // Calculate trend (simple moving average)
        BigDecimal sma20 = calculateSMA(prices, Math.min(20, prices.size()));
        
        // Current vs period start
        BigDecimal periodChange = prices.get(0).getClosePrice().subtract(
                prices.get(prices.size() - 1).getClosePrice());
        BigDecimal periodChangePercent = periodChange.multiply(BigDecimal.valueOf(100))
                .divide(prices.get(prices.size() - 1).getClosePrice(), 2, RoundingMode.HALF_UP);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("symbol", symbol.toUpperCase());
        analysis.put("days", days);
        analysis.put("maxPrice", maxPrice);
        analysis.put("minPrice", minPrice);
        analysis.put("avgPrice", avgPrice);
        analysis.put("avgVolume", avgVolume);
        analysis.put("currentPrice", prices.get(0).getClosePrice());
        analysis.put("periodChange", periodChange);
        analysis.put("periodChangePercent", periodChangePercent);
        analysis.put("sma20", sma20);
        analysis.put("priceHistory", prices);
        
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/market-summary")
    public ResponseEntity<Map<String, Object>> getMarketSummary() {
        LocalDate today = LocalDate.now();
        List<StockPrice> latestPrices = stockPriceService.getLatestPrices();
        
        if (latestPrices.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "message", "No data available",
                "date", today.toString()
            ));
        }
        
        long gainers = latestPrices.stream()
                .filter(p -> p.getPriceChangePercent() != null && p.getPriceChangePercent().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        long losers = latestPrices.stream()
                .filter(p -> p.getPriceChangePercent() != null && p.getPriceChangePercent().compareTo(BigDecimal.ZERO) < 0)
                .count();
        
        BigDecimal avgChange = latestPrices.stream()
                .map(StockPrice::getPriceChangePercent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(latestPrices.size()), 2, RoundingMode.HALF_UP);
        
        long totalVolume = latestPrices.stream()
                .map(StockPrice::getVolume)
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("date", today.toString());
        summary.put("totalStocks", latestPrices.size());
        summary.put("gainers", gainers);
        summary.put("losers", losers);
        summary.put("unchanged", latestPrices.size() - gainers - losers);
        summary.put("avgChangePercent", avgChange);
        summary.put("totalVolume", totalVolume);
        
        return ResponseEntity.ok(summary);
    }

    private BigDecimal calculateSMA(List<StockPrice> prices, int period) {
        if (prices.size() < period) {
            period = prices.size();
        }
        
        List<StockPrice> recentPrices = prices.subList(0, period);
        
        return recentPrices.stream()
                .map(StockPrice::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
    }
}
