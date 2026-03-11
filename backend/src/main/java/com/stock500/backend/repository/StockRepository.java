package com.stock500.backend.repository;

import com.stock500.backend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    List<Stock> findBySector(String sector);

    @Query("SELECT s FROM Stock s WHERE " +
           "(:sector IS NULL OR :sector = '' OR s.sector = :sector) AND " +
           "(:search IS NULL OR :search = '' OR UPPER(s.name) LIKE UPPER(CONCAT('%', :search, '%')) OR UPPER(s.symbol) LIKE UPPER(CONCAT('%', :search, '%')))")
    Page<Stock> searchByNameOrSymbol(@Param("sector") String sector,
                                     @Param("search") String search,
                                     Pageable pageable);

    @Query("SELECT DISTINCT s.sector FROM Stock s WHERE s.sector IS NOT NULL ORDER BY s.sector")
    List<String> findDistinctSectors();
}
