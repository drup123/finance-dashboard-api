package com.finance.dashboard.controller;

import com.finance.dashboard.dto.ApiResponse;
import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.service.FinancialRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    public FinancialRecordController(FinancialRecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * GET /api/records
     * VIEWER, ANALYST, ADMIN – list records with optional filters + pagination.
     *
     * Query params:
     *   type       – INCOME | EXPENSE
     *   category   – case-insensitive exact match
     *   startDate  – yyyy-MM-dd
     *   endDate    – yyyy-MM-dd
     *   page       – 0-based page index (default 0)
     *   size       – page size (default 10)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getRecords(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RecordResponse> records =
                recordService.getRecords(type, category, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    /**
     * GET /api/records/{id}
     * VIEWER, ANALYST, ADMIN – get a single record.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getRecordById(id)));
    }

    /**
     * POST /api/records
     * ANALYST, ADMIN only – create a new record.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody RecordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        RecordResponse created = recordService.createRecord(request, userDetails.getUsername());
        return ResponseEntity.status(201).body(ApiResponse.ok("Record created", created));
    }

    /**
     * PUT /api/records/{id}
     * ADMIN only – update an existing record.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Record updated", recordService.updateRecord(id, request)));
    }

    /**
     * DELETE /api/records/{id}
     * ADMIN only – soft-delete a record.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted", null));
    }
}
