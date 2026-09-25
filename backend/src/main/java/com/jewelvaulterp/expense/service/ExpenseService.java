package com.jewelvaulterp.expense.service;

import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.expense.dto.CreateExpenseRequest;
import com.jewelvaulterp.expense.dto.ExpenseResponse;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseCategory;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import com.jewelvaulterp.expense.repository.ExpenseCategoryRepository;
import com.jewelvaulterp.expense.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CompanyRepository companyRepository,
            BranchRepository branchRepository,
            ExpenseCategoryRepository categoryRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ExpenseResponse> getAll() {
        return expenseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<ExpenseResponse> getByCompany(UUID companyId) {
        return expenseRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExpenseResponse> getByCategory(UUID categoryId) {
        return expenseRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExpenseResponse> getByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse create(CreateExpenseRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        ExpenseCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

        if (!category.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("Expense category does not belong to company");
        }

        Branch branch = null;

        if (request.branchId() != null) {
            branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

            if (!branch.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException("Branch does not belong to company");
            }
        }

        Expense expense = new Expense(
                company,
                branch,
                category,
                request.description(),
                request.amount(),
                request.expenseDate(),
                request.paymentMethod(),
                request.referenceNumber()
        );

        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse approve(UUID id) {
        Expense expense = findById(id);
        expense.approve();
        return toResponse(expense);
    }

    public ExpenseResponse markPaid(UUID id) {
        Expense expense = findById(id);
        expense.markPaid();
        return toResponse(expense);
    }

    public ExpenseResponse cancel(UUID id) {
        Expense expense = findById(id);
        expense.cancel();
        return toResponse(expense);
    }

    private Expense findById(UUID id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getCompany().getId(),
                expense.getBranch() != null ? expense.getBranch().getId() : null,
                expense.getCategory().getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getPaymentMethod(),
                expense.getReferenceNumber(),
                expense.getStatus(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
