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

   
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getRecordById(id)));
    }

    
    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody RecordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        RecordResponse created = recordService.createRecord(request, userDetails.getUsername());
        return ResponseEntity.status(201).body(ApiResponse.ok("Record created", created));
    }

    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Record updated", recordService.updateRecord(id, request)));
    }

   
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted", null));
    }
}
