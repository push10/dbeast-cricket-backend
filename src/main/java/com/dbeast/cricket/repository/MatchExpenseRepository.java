package com.dbeast.cricket.repository;

import com.dbeast.cricket.entity.MatchExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchExpenseRepository extends JpaRepository<MatchExpense, Long> {

    List<MatchExpense> findByMatchIdOrderByExpenseDateAscIdAsc(Long matchId);
}
