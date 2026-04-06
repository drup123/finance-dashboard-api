package com.finance.dashboard.repository;

import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find non-deleted record by id
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // All active records with optional filters (paginated)
    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
              AND (:type IS NULL OR r.type = :type)
              AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
              AND (:startDate IS NULL OR r.date >= :startDate)
              AND (:endDate IS NULL OR r.date <= :endDate)
            ORDER BY r.date DESC
            """)
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Sum of all INCOME
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = 'INCOME'")
    BigDecimal sumIncome();

    // Sum of all EXPENSE
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = 'EXPENSE'")
    BigDecimal sumExpense();

    // Category-wise totals
    @Query("SELECT r.category, r.type, SUM(r.amount) FROM FinancialRecord r WHERE r.deleted = false GROUP BY r.category, r.type ORDER BY r.category")
    List<Object[]> categoryWiseTotals();

    // Monthly trends
    @Query("""
            SELECT YEAR(r.date), MONTH(r.date), r.type, SUM(r.amount)
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY YEAR(r.date), MONTH(r.date), r.type
            ORDER BY YEAR(r.date) DESC, MONTH(r.date) DESC
            """)
    List<Object[]> monthlyTrends();

    // Recent N records
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}
