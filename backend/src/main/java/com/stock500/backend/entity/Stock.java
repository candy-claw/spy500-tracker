package com.stock500.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@Builder
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

    @Column(name = "market_cap_str", length = 50)
    private String marketCap;

    @Column(name = "pe_ratio", length = 20)
    private String peRatio;

    @Column(name = "eps", length = 20)
    private String eps;

    @Column(name = "volume", length = 30)
    private String volume;

    @Column(name = "dividend", length = 20)
    private String dividend;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Stock() {}

    @Builder
    public Stock(String symbol, String name, String sector, String industry, String marketCap, 
                 String peRatio, String eps, String volume, String dividend,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.symbol = symbol;
        this.name = name;
        this.sector = sector;
        this.industry = industry;
        this.marketCap = marketCap;
        this.peRatio = peRatio;
        this.eps = eps;
        this.volume = volume;
        this.dividend = dividend;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
