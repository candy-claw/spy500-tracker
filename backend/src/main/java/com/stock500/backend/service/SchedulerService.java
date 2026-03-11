package com.stock500.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final StockDataService stockDataService;

    public SchedulerService(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    // Run at 6PM on weekdays (market data available after close)
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void scheduledFetch() {
        log.info("Scheduled fetch starting...");
        stockDataService.fetchAndStoreDailyPrices();
    }
}
