package com.finance.dashboard.config;

import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      FinancialRecordRepository recordRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedRecords();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = new User();
        admin.setName("Super Admin");
        admin.setEmail("admin@finance.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        User analyst = new User();
        analyst.setName("Alice Analyst");
        analyst.setEmail("analyst@finance.com");
        analyst.setPassword(passwordEncoder.encode("analyst123"));
        analyst.setRole(Role.ANALYST);
        analyst.setActive(true);

        User viewer = new User();
        viewer.setName("Victor Viewer");
        viewer.setEmail("viewer@finance.com");
        viewer.setPassword(passwordEncoder.encode("viewer123"));
        viewer.setRole(Role.VIEWER);
        viewer.setActive(true);

        userRepository.saveAll(List.of(admin, analyst, viewer));
        log.info("Seeded {} default users", 3);
    }

    private void seedRecords() {
        if (recordRepository.count() > 0) return;

        User admin = userRepository.findByEmail("admin@finance.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        List<FinancialRecord> records = List.of(
                createRecord(admin, new BigDecimal("50000.00"), TransactionType.INCOME,  "Salary",    LocalDate.of(2024, 1, 1),  "January salary"),
                createRecord(admin, new BigDecimal("12000.00"), TransactionType.EXPENSE, "Rent",      LocalDate.of(2024, 1, 5),  "Office rent"),
                createRecord(admin, new BigDecimal("3500.00"),  TransactionType.EXPENSE, "Utilities", LocalDate.of(2024, 1, 10), "Electricity & water"),
                createRecord(admin, new BigDecimal("50000.00"), TransactionType.INCOME,  "Salary",    LocalDate.of(2024, 2, 1),  "February salary"),
                createRecord(admin, new BigDecimal("8000.00"),  TransactionType.EXPENSE, "Marketing", LocalDate.of(2024, 2, 14), "Social media ads"),
                createRecord(admin, new BigDecimal("15000.00"), TransactionType.INCOME,  "Freelance", LocalDate.of(2024, 2, 20), "Consulting project"),
                createRecord(admin, new BigDecimal("50000.00"), TransactionType.INCOME,  "Salary",    LocalDate.of(2024, 3, 1),  "March salary"),
                createRecord(admin, new BigDecimal("12000.00"), TransactionType.EXPENSE, "Rent",      LocalDate.of(2024, 3, 5),  "Office rent"),
                createRecord(admin, new BigDecimal("5200.00"),  TransactionType.EXPENSE, "Software",  LocalDate.of(2024, 3, 12), "SaaS subscriptions"),
                createRecord(admin, new BigDecimal("9500.00"),  TransactionType.INCOME,  "Freelance", LocalDate.of(2024, 3, 25), "Side project")
        );

        recordRepository.saveAll(records);
        log.info("Seeded {} sample financial records", records.size());
    }

    private FinancialRecord createRecord(User user, BigDecimal amount, TransactionType type,
                                         String category, LocalDate date, String notes) {

        FinancialRecord record = new FinancialRecord();
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category);
        record.setDate(date);
        record.setNotes(notes);
        record.setCreatedBy(user);
        record.setDeleted(false);

        return record;
    }
}