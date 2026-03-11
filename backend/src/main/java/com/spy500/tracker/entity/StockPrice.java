package com.spy500.tracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_prices", indexes = {
    @Index(name = "idx_symbol_date", columnList = "stock_id, price_date", unique = true)
})
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false)
    private BigDecimal closePrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "adj_close")
    private BigDecimal adjClose;

    @Column(name = "price_change")
    private BigDecimal priceChange;

    @Column(name = "price_change_percent")
    private BigDecimal priceChangePercent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }
    
    public LocalDate getPriceDate() { return priceDate; }
    public void setPriceDate(LocalDate priceDate) { this.priceDate = priceDate; }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    
    public BigDecimal getAdjClose() { return adjClose; }
    public void setAdjClose(BigDecimal adjClose) { this.adjClose = adjClose; }
    
    public BigDecimal getPriceChange() { return priceChange; }
    public void setPriceChange(BigDecimal priceChange) { this.priceChange = priceChange; }
    
    public BigDecimal getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(BigDecimal priceChangePercent) { this.priceChangePercent = priceChangePercent; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
