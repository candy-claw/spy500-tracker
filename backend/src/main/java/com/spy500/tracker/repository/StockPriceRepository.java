package com.spy500.tracker.repository;

import com.spy500.tracker.entity.StockPrice;
import com.spy500.tracker.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findByStockAndPriceDate(Stock stock, LocalDate priceDate);

    List<StockPrice> findByStockIdOrderByPriceDateDesc(Long stockId);

    List<StockPrice> findByStockIdAndPriceDateBetweenOrderByPriceDateAsc(
            Long stockId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sp FROM StockPrice sp WHERE sp.stock.symbol = :symbol AND sp.priceDate BETWEEN :startDate AND :endDate ORDER BY sp.priceDate ASC")
    List<StockPrice> findBySymbolAndDateRange(
            @Param("symbol") String symbol,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT sp FROM StockPrice sp WHERE sp.priceDate = :date")
    List<StockPrice> findByPriceDate(@Param("date") LocalDate date);

    @Query("SELECT sp.stock.symbol, sp.priceChangePercent FROM StockPrice sp WHERE sp.priceDate = :date AND sp.priceChangePercent IS NOT NULL ORDER BY sp.priceChangePercent ASC")
    List<Object[]> findWorstPerformersByDate(@Param("date") LocalDate date);

    @Query("SELECT sp.stock.symbol, sp.priceChangePercent FROM StockPrice sp WHERE sp.priceDate = :date AND sp.priceChangePercent IS NOT NULL ORDER BY sp.priceChangePercent DESC")
    List<Object[]> findTopPerformersByDate(@Param("date") LocalDate date);
}
