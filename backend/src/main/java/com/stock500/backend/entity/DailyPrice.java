package com.stock500.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_prices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"symbol", "trade_date"})
})
public class DailyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "market_cap", length = 20)
    private String marketCap;

    @Column(name = "eps", precision = 10, scale = 2)
    private BigDecimal eps;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "dividend", precision = 10, scale = 2)
    private BigDecimal dividend;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", insertable = false, updatable = false)
    private Stock stock;

    public DailyPrice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }

    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }

    public String getMarketCap() { return marketCap; }
    public void setMarketCap(String marketCap) { this.marketCap = marketCap; }

    public BigDecimal getEps() { return eps; }
    public void setEps(BigDecimal eps) { this.eps = eps; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public BigDecimal getDividend() { return dividend; }
    public void setDividend(BigDecimal dividend) { this.dividend = dividend; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }

    public static DailyPriceBuilder builder() {
        return new DailyPriceBuilder();
    }

    public static class DailyPriceBuilder {
        private Long id;
        private String symbol;
        private LocalDate tradeDate;
        private BigDecimal peRatio;
        private String marketCap;
        private BigDecimal eps;
        private Long volume;
        private BigDecimal dividend;

        public DailyPriceBuilder id(Long id) { this.id = id; return this; }
        public DailyPriceBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public DailyPriceBuilder tradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; return this; }
        public DailyPriceBuilder peRatio(BigDecimal peRatio) { this.peRatio = peRatio; return this; }
        public DailyPriceBuilder marketCap(String marketCap) { this.marketCap = marketCap; return this; }
        public DailyPriceBuilder eps(BigDecimal eps) { this.eps = eps; return this; }
        public DailyPriceBuilder volume(Long volume) { this.volume = volume; return this; }
        public DailyPriceBuilder dividend(BigDecimal dividend) { this.dividend = dividend; return this; }

        public DailyPrice build() {
            DailyPrice dp = new DailyPrice();
            dp.id = this.id;
            dp.symbol = this.symbol;
            dp.tradeDate = this.tradeDate;
            dp.peRatio = this.peRatio;
            dp.marketCap = this.marketCap;
            dp.eps = this.eps;
            dp.volume = this.volume;
            dp.dividend = this.dividend;
            return dp;
        }
    }
}
