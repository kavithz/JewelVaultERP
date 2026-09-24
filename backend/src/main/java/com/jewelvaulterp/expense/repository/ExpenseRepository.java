package com.jewelvaulterp.expense.repository;

import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByCompanyId(UUID companyId);
    List<Expense> findByCategoryId(UUID categoryId);
    List<Expense> findByStatus(ExpenseStatus status);
}
