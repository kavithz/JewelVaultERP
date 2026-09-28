package com.jewelvaulterp.purchase.controller;

import com.jewelvaulterp.purchase.dto.CreatePurchaseRequest;
import com.jewelvaulterp.purchase.dto.PurchaseResponse;
import com.jewelvaulterp.purchase.entity.PurchaseStatus;
import com.jewelvaulterp.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

	private final PurchaseService purchaseService;

	public PurchaseController(PurchaseService purchaseService) {
		this.purchaseService = purchaseService;
	}

	@GetMapping
	public List<PurchaseResponse> getPurchases() {
		return purchaseService.getAllPurchases();
	}

	@GetMapping("/{id}")
	public PurchaseResponse getPurchase(@PathVariable UUID id) {
		return purchaseService.getPurchase(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseResponse createPurchase(
			@Valid @RequestBody CreatePurchaseRequest request
	) {
		return purchaseService.createPurchase(request);
	}

	@PatchMapping("/{id}/status")
	public PurchaseResponse updateStatus(
			@PathVariable UUID id,
			@RequestParam PurchaseStatus status
	) {
		return purchaseService.updateStatus(id, status);
	}
}
