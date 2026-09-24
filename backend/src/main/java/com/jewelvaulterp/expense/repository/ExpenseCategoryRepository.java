package com.jewelvaulterp.expense.repository;

import com.jewelvaulterp.expense.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
    List<ExpenseCategory> findByCompanyId(UUID companyId);
}
