package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserService userService;

    public FinancialRecordService(FinancialRecordRepository recordRepository,
                                 UserService userService) {
        this.recordRepository = recordRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<RecordResponse> getRecords(TransactionType type,
                                           String category,
                                           LocalDate startDate,
                                           LocalDate endDate,
                                           int page,
                                           int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

        return recordRepository
                .findAllWithFilters(type, category, startDate, endDate, pageable)
                .map(RecordResponse::from);
    }

    @Transactional(readOnly = true)
    public RecordResponse getRecordById(Long id) {
        return RecordResponse.from(findRecordOrThrow(id));
    }

    @Transactional
    public RecordResponse createRecord(RecordRequest request, String creatorEmail) {

        User creator = userService.findByEmail(creatorEmail);

        // ❌ removed builder → ✅ using setters
        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
        record.setCreatedBy(creator);
        record.setDeleted(false); // important for soft delete

        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public RecordResponse updateRecord(Long id, RecordRequest request) {

        FinancialRecord record = findRecordOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(Long id) {

        FinancialRecord record = findRecordOrThrow(id);
        record.setDeleted(true);   // soft delete
        recordRepository.save(record);
    }

    private FinancialRecord findRecordOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial record not found with id: " + id));
    }
}