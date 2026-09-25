package com.jewelvaulterp.payable.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.invoice.entity.Invoice;
import com.jewelvaulterp.invoice.repository.InvoiceRepository;
import com.jewelvaulterp.payable.dto.CreatePayableRequest;
import com.jewelvaulterp.payable.dto.PayableResponse;
import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payable.entity.PayableStatus;
import com.jewelvaulterp.payable.repository.PayableRepository;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PayableService {

    private final PayableRepository payableRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final InvoiceRepository invoiceRepository;

    public PayableService(
            PayableRepository payableRepository,
            CompanyRepository companyRepository,
            SupplierRepository supplierRepository,
            InvoiceRepository invoiceRepository
    ) {
        this.payableRepository = payableRepository;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<PayableResponse> getAll() {
        return payableRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PayableResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<PayableResponse> getByCompany(UUID companyId) {
        return payableRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayableResponse> getBySupplier(UUID supplierId) {
        return payableRepository.findBySupplierId(supplierId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayableResponse> getByStatus(PayableStatus status) {
        return payableRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PayableResponse create(CreatePayableRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found"));

        if (!supplier.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Supplier does not belong to the specified company");
        }

        Invoice invoice = null;

        if (request.invoiceId() != null) {
            invoice = invoiceRepository.findById(request.invoiceId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Invoice not found"));

            if (!invoice.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException(
                        "Invoice does not belong to the specified company");
            }
        }

        Payable payable = new Payable(
                company,
                supplier,
                invoice,
                request.amount(),
                request.dueDate()
        );

        return toResponse(payableRepository.save(payable));
    }

    public PayableResponse recordPayment(
            UUID id,
            BigDecimal amount
    ) {
        Payable payable = findById(id);

        payable.recordPayment(amount);

        return toResponse(payable);
    }

    public PayableResponse markOverdue(UUID id) {
        Payable payable = findById(id);

        payable.markOverdue();

        return toResponse(payable);
    }

    public PayableResponse cancel(UUID id) {
        Payable payable = findById(id);

        payable.cancel();

        return toResponse(payable);
    }

    private Payable findById(UUID id) {
        return payableRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payable not found"));
    }

    private PayableResponse toResponse(Payable payable) {

        return new PayableResponse(
                payable.getId(),
                payable.getCompany().getId(),
                payable.getSupplier().getId(),
                payable.getInvoice() != null
                        ? payable.getInvoice().getId()
                        : null,
                payable.getAmount(),
                payable.getPaidAmount(),
                payable.getOutstandingAmount(),
                payable.getDueDate(),
                payable.getStatus(),
                payable.getCreatedAt(),
                payable.getUpdatedAt()
        );
    }
}