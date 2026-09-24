package com.jewelvaulterp.receivable.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.invoice.entity.Invoice;
import com.jewelvaulterp.invoice.repository.InvoiceRepository;
import com.jewelvaulterp.receivable.dto.CreateReceivableRequest;
import com.jewelvaulterp.receivable.dto.ReceivableResponse;
import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.receivable.entity.ReceivableStatus;
import com.jewelvaulterp.receivable.repository.ReceivableRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReceivableService {

    private final ReceivableRepository receivableRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    public ReceivableService(
            ReceivableRepository receivableRepository,
            CompanyRepository companyRepository,
            CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository
    ) {
        this.receivableRepository = receivableRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<ReceivableResponse> getAll() {
        return receivableRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReceivableResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<ReceivableResponse> getByCompany(UUID companyId) {
        return receivableRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReceivableResponse> getByCustomer(UUID customerId) {
        return receivableRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReceivableResponse> getByStatus(ReceivableStatus status) {
        return receivableRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReceivableResponse create(CreateReceivableRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        if (!customer.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Customer does not belong to the specified company");
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

        Receivable receivable = new Receivable(
                company,
                customer,
                invoice,
                request.amount(),
                request.dueDate()
        );

        return toResponse(receivableRepository.save(receivable));
    }

    public ReceivableResponse recordPayment(
            UUID id,
            BigDecimal amount
    ) {
        Receivable receivable = findById(id);

        receivable.recordPayment(amount);

        return toResponse(receivable);
    }

    public ReceivableResponse markOverdue(UUID id) {
        Receivable receivable = findById(id);

        receivable.markOverdue();

        return toResponse(receivable);
    }

    public ReceivableResponse cancel(UUID id) {
        Receivable receivable = findById(id);

        receivable.cancel();

        return toResponse(receivable);
    }

    private Receivable findById(UUID id) {
        return receivableRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Receivable not found"));
    }

    private ReceivableResponse toResponse(Receivable receivable) {

        return new ReceivableResponse(
                receivable.getId(),
                receivable.getCompany().getId(),
                receivable.getCustomer().getId(),
                receivable.getInvoice() != null
                        ? receivable.getInvoice().getId()
                        : null,
                receivable.getAmount(),
                receivable.getPaidAmount(),
                receivable.getOutstandingAmount(),
                receivable.getDueDate(),
                receivable.getStatus(),
                receivable.getCreatedAt(),
                receivable.getUpdatedAt()
        );
    }
}