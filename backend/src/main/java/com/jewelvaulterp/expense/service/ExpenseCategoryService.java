package com.jewelvaulterp.expense.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.expense.dto.CreateExpenseCategoryRequest;
import com.jewelvaulterp.expense.entity.ExpenseCategory;
import com.jewelvaulterp.expense.repository.ExpenseCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;
    private final CompanyRepository companyRepository;

    public ExpenseCategoryService(
            ExpenseCategoryRepository repository,
            CompanyRepository companyRepository
    ) {
        this.repository = repository;
        this.companyRepository = companyRepository;
    }

    public List<ExpenseCategory> getByCompany(UUID companyId) {
        return repository.findByCompanyId(companyId);
    }

    public ExpenseCategory create(CreateExpenseCategoryRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        ExpenseCategory category = new ExpenseCategory(
                company,
                request.name(),
                request.description()
        );

        return repository.save(category);
    }
}
