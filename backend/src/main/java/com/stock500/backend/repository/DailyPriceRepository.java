package com.stock500.backend.repository;

import com.stock500.backend.entity.DailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPriceRepository extends JpaRepository<DailyPrice, Long> {

    List<DailyPrice> findBySymbolOrderByTradeDateDesc(String symbol);

    List<DailyPrice> findBySymbolAndTradeDateBetweenOrderByTradeDateAsc(
            String symbol, LocalDate from, LocalDate to);

    List<DailyPrice> findByTradeDateBetween(LocalDate from, LocalDate to);

    Optional<DailyPrice> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

    @Query(value = "SELECT dp.* FROM daily_prices dp " +
                   "INNER JOIN (SELECT symbol, MAX(trade_date) as max_date FROM daily_prices GROUP BY symbol) latest " +
                   "ON dp.symbol = latest.symbol AND dp.trade_date = latest.max_date",
           nativeQuery = true)
    List<DailyPrice> findLatestPricePerStock();

    @Query(value = "SELECT dp.* FROM daily_prices dp " +
                   "INNER JOIN (SELECT symbol, MAX(trade_date) as max_date FROM daily_prices GROUP BY symbol) latest " +
                   "ON dp.symbol = latest.symbol AND dp.trade_date = latest.max_date " +
                   "WHERE dp.symbol IN :symbols",
           nativeQuery = true)
    List<DailyPrice> findLatestPriceForSymbols(@Param("symbols") List<String> symbols);

    @Query("SELECT dp FROM DailyPrice dp WHERE dp.symbol = :symbol ORDER BY dp.tradeDate DESC LIMIT :limit")
    List<DailyPrice> findTopBySymbolOrderByTradeDateDesc(@Param("symbol") String symbol, @Param("limit") int limit);
}
