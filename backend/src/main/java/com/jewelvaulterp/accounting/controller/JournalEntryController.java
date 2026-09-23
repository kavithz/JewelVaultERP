package com.jewelvaulterp.accounting.controller;

import com.jewelvaulterp.accounting.dto.CreateJournalEntryRequest;
import com.jewelvaulterp.accounting.dto.JournalEntryResponse;
import com.jewelvaulterp.accounting.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/journal-entries")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(
            JournalEntryService journalEntryService
    ) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping
    public List<JournalEntryResponse> getAll() {
        return journalEntryService.getAll();
    }

    @GetMapping("/{id}")
    public JournalEntryResponse getById(
            @PathVariable UUID id
    ) {
        return journalEntryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryResponse create(
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        return journalEntryService.create(request);
    }

    @PatchMapping("/{id}/post")
    public JournalEntryResponse post(
            @PathVariable UUID id
    ) {
        return journalEntryService.post(id);
    }

    @PatchMapping("/{id}/cancel")
    public JournalEntryResponse cancel(
            @PathVariable UUID id
    ) {
        return journalEntryService.cancel(id);
    }
}