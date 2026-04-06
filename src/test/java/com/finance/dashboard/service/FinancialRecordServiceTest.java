package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    FinancialRecordRepository recordRepository;

    @Mock
    UserService userService;

    @InjectMocks
    FinancialRecordService recordService;

    private User admin;
    private FinancialRecord sampleRecord;

    @BeforeEach
    void setUp() {

        // ❌ removed builder → ✅ manual creation
        admin = new User();
        admin.setId(1L);
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setPassword("enc");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        sampleRecord = new FinancialRecord();
        sampleRecord.setId(1L);
        sampleRecord.setAmount(new BigDecimal("1000"));
        sampleRecord.setType(TransactionType.INCOME);
        sampleRecord.setCategory("Salary");
        sampleRecord.setDate(LocalDate.now());
        sampleRecord.setNotes("Test");
        sampleRecord.setCreatedBy(admin);
        sampleRecord.setDeleted(false);
    }

    @Test
    void getRecordById_existingRecord_returnsDto() {

        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));

        var dto = recordService.getRecordById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCategory()).isEqualTo("Salary");
    }

    @Test
    void getRecordById_notFound_throwsNotFound() {

        when(recordRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRecord_validRequest_savesAndReturns() {

        RecordRequest req = new RecordRequest();
        req.setAmount(new BigDecimal("500"));
        req.setType(TransactionType.EXPENSE);
        req.setCategory("Rent");
        req.setDate(LocalDate.now());
        req.setNotes("Monthly rent");

        when(userService.findByEmail("admin@test.com")).thenReturn(admin);

        when(recordRepository.save(any())).thenAnswer(invocation -> {
            FinancialRecord r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        var dto = recordService.createRecord(req, "admin@test.com");

        assertThat(dto.getCategory()).isEqualTo("Rent");
        assertThat(dto.getType()).isEqualTo(TransactionType.EXPENSE);

        verify(recordRepository).save(any());
    }

    @Test
    void deleteRecord_existingRecord_softDeletes() {

        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(sampleRecord));

        when(recordRepository.save(any()))
                .thenReturn(sampleRecord);

        recordService.deleteRecord(1L);

        assertThat(sampleRecord.isDeleted()).isTrue();
        verify(recordRepository).save(sampleRecord);
    }
}