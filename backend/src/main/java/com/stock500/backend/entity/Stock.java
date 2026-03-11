package com.stock500.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "industry", length = 200)
    private String industry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Stock() {}

    public Stock(String symbol, String name, String sector, String industry) {
        this.symbol = symbol;
        this.name = name;
        this.sector = sector;
        this.industry = industry;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static StockBuilder builder() {
        return new StockBuilder();
    }

    public static class StockBuilder {
        private String symbol;
        private String name;
        private String sector;
        private String industry;

        public StockBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public StockBuilder name(String name) { this.name = name; return this; }
        public StockBuilder sector(String sector) { this.sector = sector; return this; }
        public StockBuilder industry(String industry) { this.industry = industry; return this; }

        public Stock build() {
            Stock stock = new Stock();
            stock.symbol = this.symbol;
            stock.name = this.name;
            stock.sector = this.sector;
            stock.industry = this.industry;
            return stock;
        }
    }
}
