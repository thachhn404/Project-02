package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Permission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointRule;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.RolePermission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.PermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RolePermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.PointRuleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CitizenRepository citizenRepository,
            WasteCategoryRepository wasteCategoryRepository,
            WasteReportRepository wasteReportRepository,
            ReportImageRepository reportImageRepository,
            EnterpriseRepository enterpriseRepository,
            CollectorRepository collectorRepository,
            CollectionRequestRepository collectionRequestRepository,
            CollectionTrackingRepository collectionTrackingRepository,
            CollectorReportRepository collectorReportRepository,
            CollectorReportImageRepository collectorReportImageRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PointRuleRepository pointRuleRepository,
            PointTransactionRepository pointTransactionRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Initializing permissions
            Permission createReport = createPermissionIfNotFound(permissionRepository, "CREATE_REPORT",
                    "Create waste report", "CITIZEN");
            Permission viewOwnReports = createPermissionIfNotFound(permissionRepository, "VIEW_OWN_REPORTS",
                    "View own waste reports", "CITIZEN");

            Permission viewAreaReports = createPermissionIfNotFound(permissionRepository, "VIEW_AREA_REPORTS",
                    "View reports in assigned area", "ENTERPRISE");
            Permission assignCollector = createPermissionIfNotFound(permissionRepository, "ASSIGN_COLLECTOR",
                    "Assign collector to report", "ENTERPRISE");

            Permission viewTasks = createPermissionIfNotFound(permissionRepository, "VIEW_ASSIGNED_TASKS",
                    "View assigned collection tasks", "COLLECTOR");
            Permission updateStatus = createPermissionIfNotFound(permissionRepository, "UPDATE_TASK_STATUS",
                    "Update task collection status", "COLLECTOR");

            Role citizenRole = createRoleIfNotFound(roleRepository, "CITIZEN", "Citizen User");
            Role enterpriseRole = createRoleIfNotFound(roleRepository, "ENTERPRISE", "Recycling Enterprise");
            Role collectorRole = createRoleIfNotFound(roleRepository, "COLLECTOR", "Waste Collector");
            Role entAdminRole = createRoleIfNotFound(roleRepository, "ENTERPRISE_ADMIN", "Enterprise Administrator");
            Role adminRole = createRoleIfNotFound(roleRepository, "ADMIN", "System Admin");

            assignPermissionToRole(rolePermissionRepository, citizenRole, createReport);
            assignPermissionToRole(rolePermissionRepository, citizenRole, viewOwnReports);

            assignPermissionToRole(rolePermissionRepository, enterpriseRole, viewAreaReports);
            assignPermissionToRole(rolePermissionRepository, entAdminRole, viewAreaReports);
            assignPermissionToRole(rolePermissionRepository, entAdminRole, assignCollector);

            assignPermissionToRole(rolePermissionRepository, collectorRole, viewTasks);
            assignPermissionToRole(rolePermissionRepository, collectorRole, updateStatus);

            createUserIfNotFound(userRepository, passwordEncoder, "citizen@test.com", "citizen123", "Test Citizen",
                    citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "citizen2@test.com", "citizen123", "Test Citizen 2",
                    citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "enterprise@test.com", "enterprise123",
                    "Test Enterprise", enterpriseRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector@test.com", "collector123",
                    "Test Collector", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector2@test.com", "collector123",
                    "Test Collector 2", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "admin@test.com", "admin123", "Test Admin",
                    adminRole);

            Citizen citizen1 = userRepository.findByEmail("citizen@test.com")
                    .flatMap(u -> citizenRepository.findByUserId(u.getId()))
                    .orElseGet(() -> userRepository.findByEmail("citizen@test.com")
                            .map(u -> createCitizenIfNotFound(citizenRepository, u))
                            .orElseThrow());

            userRepository.findByEmail("citizen2@test.com")
                    .ifPresent(u -> createCitizenIfNotFound(citizenRepository, u));

            User enterpriseUser = userRepository.findByEmail("enterprise@test.com").orElseThrow();
            Enterprise enterprise = createEnterpriseIfNotFound(enterpriseRepository, enterpriseUser);
            linkEnterpriseToUserIfMissing(userRepository, enterpriseUser, enterprise);

            Collector collector1 = userRepository.findByEmail("collector@test.com")
                    .map(u -> createCollectorIfNotFound(collectorRepository, u, enterprise))
                    .orElseThrow();
            Collector collector2 = userRepository.findByEmail("collector2@test.com")
                    .map(u -> createCollectorIfNotFound(collectorRepository, u, enterprise))
                    .orElseThrow();

            seedWasteCategories(wasteCategoryRepository);

            seedCitizenAndEnterpriseFlow(
                    citizen1,
                    enterprise,
                    collector1,
                    collector2,
                    wasteReportRepository,
                    reportImageRepository,
                    collectionRequestRepository,
                    collectionTrackingRepository,
                    collectorReportRepository,
                    collectorReportImageRepository,
                    pointRuleRepository,
                    pointTransactionRepository,
                    feedbackRepository);
        };
    }

    private void seedWasteCategories(WasteCategoryRepository wasteCategoryRepository) {
        LocalDateTime now = LocalDateTime.now();

        createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy", WasteUnit.KG, new BigDecimal("2250.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Báo", WasteUnit.KG, new BigDecimal("3600.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy, hồ sơ", WasteUnit.KG, new BigDecimal("3150.0000"),
                now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Giấy tập", WasteUnit.KG, new BigDecimal("3600.0000"),
                now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Lon bia", WasteUnit.CAN, new BigDecimal("180.0000"),
                now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Sắt", WasteUnit.KG, new BigDecimal("3600.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Sắt lon", WasteUnit.KG, new BigDecimal("1440.0000"),
                now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Inox", WasteUnit.KG, new BigDecimal("5400.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Đồng", WasteUnit.KG, new BigDecimal("67500.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Nhôm", WasteUnit.KG, new BigDecimal("16200.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Chai thủy tinh", WasteUnit.BOTTLE,
                new BigDecimal("450.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Bao bì, hỗn hợp", WasteUnit.KG, new BigDecimal("1600.0000"),
                now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Meca", WasteUnit.KG, new BigDecimal("450.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ", WasteUnit.KG, new BigDecimal("3600.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ bình", WasteUnit.KG, new BigDecimal("4500.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ tôn", WasteUnit.KG, new BigDecimal("1800.0000"), now);
        createWasteCategoryIfNotFound(wasteCategoryRepository, "Mủ đen", WasteUnit.KG, new BigDecimal("150.0000"), now);
    }

    private WasteCategory createWasteCategoryIfNotFound(
            WasteCategoryRepository repo,
            String name,
            WasteUnit unit,
            BigDecimal pointPerUnit,
            LocalDateTime now) {
        return repo.findByNameIgnoreCase(name).orElseGet(() -> {
            WasteCategory category = new WasteCategory();
            category.setName(name);
            category.setUnit(unit);
            category.setPointPerUnit(pointPerUnit);
            category.setCreatedAt(now);
            category.setUpdatedAt(now);
            return repo.save(category);
        });
    }

    private Permission createPermissionIfNotFound(PermissionRepository repo, String code, String name, String module) {
        return repo.findByPermissionCode(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setPermissionCode(code);
            p.setPermissionName(name);
            p.setModule(module);
            return repo.save(p);
        });
    }

    private void assignPermissionToRole(RolePermissionRepository repo, Role role, Permission permission) {
        if (!repo.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            repo.save(rolePermission);
        }
    }

    private Role createRoleIfNotFound(RoleRepository roleRepository, String code, String name) {
        return roleRepository.findByRoleCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setRoleCode(code);
            role.setRoleName(name);
            return roleRepository.save(role);
        });
    }

    private void createUserIfNotFound(UserRepository userRepository, PasswordEncoder passwordEncoder, String email,
            String password, String fullName, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setStatus("active");
            userRepository.save(user);
        }
    }

    private Citizen createCitizenIfNotFound(CitizenRepository citizenRepository, User user) {
        if (user.getId() == null) {
            return null;
        }
        return citizenRepository.findByUserId(user.getId()).orElseGet(() -> {
            Citizen citizen = new Citizen();
            citizen.setUser(user);
            citizen.setEmail(user.getEmail());
            citizen.setFullName(user.getFullName());
            citizen.setPasswordHash(user.getPasswordHash());
            citizen.setPhone(user.getPhone());
            citizen.setTotalPoints(0);
            citizen.setTotalReports(0);
            citizen.setValidReports(0);
            return citizenRepository.save(citizen);
        });
    }

    private Enterprise createEnterpriseIfNotFound(EnterpriseRepository enterpriseRepository, User enterpriseUser) {
        String email = enterpriseUser.getEmail();
        if (email == null || email.isBlank()) {
            return null;
        }
        return enterpriseRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Enterprise enterprise = new Enterprise();
            enterprise.setName("Test Enterprise");
            enterprise.setEmail(email);
            enterprise.setStatus("active");
            enterprise.setCreatedAt(LocalDateTime.now());
            enterprise.setUpdatedAt(LocalDateTime.now());
            return enterpriseRepository.save(enterprise);
        });
    }

    private void linkEnterpriseToUserIfMissing(UserRepository userRepository, User enterpriseUser,
            Enterprise enterprise) {
        if (enterprise == null) {
            return;
        }
        if (enterpriseUser.getEnterprise() != null && enterpriseUser.getEnterprise().getId() != null) {
            return;
        }
        enterpriseUser.setEnterprise(enterprise);
        userRepository.save(enterpriseUser);
    }

    private Collector createCollectorIfNotFound(CollectorRepository collectorRepository, User collectorUser,
            Enterprise enterprise) {
        if (collectorUser.getId() == null || enterprise == null || enterprise.getId() == null) {
            return null;
        }
        return collectorRepository.findByUserId(collectorUser.getId()).orElseGet(() -> {
            Collector collector = new Collector();
            collector.setUser(collectorUser);
            collector.setEnterprise(enterprise);
            collector.setEmail(collectorUser.getEmail());
            collector.setFullName(collectorUser.getFullName());
            collector.setStatus(CollectorStatus.AVAILABLE);
            collector.setCreatedAt(LocalDateTime.now());
            return collectorRepository.save(collector);
        });
    }

    private void seedCitizenAndEnterpriseFlow(
            Citizen citizen,
            Enterprise enterprise,
            Collector collector1,
            Collector collector2,
            WasteReportRepository wasteReportRepository,
            ReportImageRepository reportImageRepository,
            CollectionRequestRepository collectionRequestRepository,
            CollectionTrackingRepository collectionTrackingRepository,
            CollectorReportRepository collectorReportRepository,
            CollectorReportImageRepository collectorReportImageRepository,
            PointRuleRepository pointRuleRepository,
            PointTransactionRepository pointTransactionRepository,
            FeedbackRepository feedbackRepository) {
        if (citizen == null || citizen.getId() == null
                || enterprise == null || enterprise.getId() == null
                || collector1 == null || collector1.getId() == null
                || collector2 == null || collector2.getId() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        WasteReport r1 = createWasteReportIfNotFound(wasteReportRepository, "WR-SEED-001", citizen,
                "PENDING", now.minusHours(2));
        createReportImageIfMissing(reportImageRepository, r1, "https://example.com/reports/seed-001.jpg");
        ensureCollectionRequest(collectionRequestRepository, "CR-SEED-001", r1, enterprise, null,
                CollectionRequestStatus.PENDING, null, null,
                null, null, null, now.minusHours(2), now.minusHours(2));

        WasteReport r2 = createWasteReportIfNotFound(wasteReportRepository, "WR-SEED-002", citizen,
                "PENDING", now.minusHours(3));
        createReportImageIfMissing(reportImageRepository, r2, "https://example.com/reports/seed-002.jpg");
        ensureCollectionRequest(collectionRequestRepository, "CR-SEED-002", r2, enterprise, null,
                CollectionRequestStatus.ACCEPTED_ENTERPRISE,
                null, null, null, null, null, now.minusHours(3), now.minusHours(1));

        WasteReport r3 = createWasteReportIfNotFound(wasteReportRepository, "WR-SEED-003", citizen,
                "PENDING", now.minusHours(4));
        createReportImageIfMissing(reportImageRepository, r3, "https://example.com/reports/seed-003.jpg");
        CollectionRequest cr3 = ensureCollectionRequest(collectionRequestRepository, "CR-SEED-003", r3, enterprise,
                collector1, CollectionRequestStatus.ASSIGNED,
                now.minusHours(2), null, null, null, null, now.minusHours(4), now.minusHours(2));
        ensureTrackingIfMissing(collectionTrackingRepository, cr3, collector1, "assigned", now.minusHours(2));

        WasteReport r4 = createWasteReportIfNotFound(wasteReportRepository, "WR-SEED-004", citizen,
                "PENDING", now.minusHours(5));
        createReportImageIfMissing(reportImageRepository, r4, "https://example.com/reports/seed-004.jpg");
        CollectionRequest cr4 = ensureCollectionRequest(collectionRequestRepository, "CR-SEED-004", r4, enterprise,
                collector1, CollectionRequestStatus.ON_THE_WAY,
                now.minusHours(4), now.minusHours(3), now.minusHours(2), null, null, now.minusHours(5),
                now.minusHours(2));
        ensureTrackingIfMissing(collectionTrackingRepository, cr4, collector1, "assigned", now.minusHours(4));
        ensureTrackingIfMissing(collectionTrackingRepository, cr4, collector1, "accepted", now.minusHours(3));
        ensureTrackingIfMissing(collectionTrackingRepository, cr4, collector1, "started", now.minusHours(2));

        WasteReport r5 = createWasteReportIfNotFound(wasteReportRepository, "WR-SEED-005", citizen,
                "COLLECTED", now.minusDays(1));
        createReportImageIfMissing(reportImageRepository, r5, "https://example.com/reports/seed-005.jpg");
        CollectionRequest cr5 = ensureCollectionRequest(collectionRequestRepository, "CR-SEED-005", r5, enterprise,
                collector2, CollectionRequestStatus.COLLECTED,
                now.minusDays(1).plusHours(1), now.minusDays(1).plusHours(2), now.minusDays(1).plusHours(3),
                new BigDecimal("8.50"),
                now.minusDays(1).plusHours(5), now.minusDays(1), now.minusDays(1).plusHours(5));
        ensureTrackingIfMissing(collectionTrackingRepository, cr5, collector2, "assigned",
                now.minusDays(1).plusHours(1));
        ensureTrackingIfMissing(collectionTrackingRepository, cr5, collector2, "accepted",
                now.minusDays(1).plusHours(2));
        ensureTrackingIfMissing(collectionTrackingRepository, cr5, collector2, "started",
                now.minusDays(1).plusHours(3));
        ensureTrackingIfMissing(collectionTrackingRepository, cr5, collector2, "collected",
                now.minusDays(1).plusHours(5));

        ensureCollectorReportIfMissing(collectorReportRepository, collectorReportImageRepository, cr5, collector2,
                now.minusDays(1).plusHours(5));

        PointRule rule = ensurePointRule(pointRuleRepository, enterprise, now.minusDays(30));
        ensurePointTransaction(pointTransactionRepository, citizen, r5, cr5, rule, now.minusDays(1).plusHours(5));

        ensureFeedback(feedbackRepository, citizen, cr5, now.minusDays(1).plusHours(6));

        updateCitizenStats(citizen);
    }

    private WasteReport createWasteReportIfNotFound(WasteReportRepository repo, String reportCode, Citizen citizen,
            String status, LocalDateTime createdAt) {
        return repo.findByReportCode(reportCode).orElseGet(() -> {
            WasteReport report = new WasteReport();
            report.setReportCode(reportCode);
            report.setCitizen(citizen);
            report.setWasteType("RECYCLABLE");
            report.setDescription("Seed report " + reportCode);
            report.setEstimatedWeight(new BigDecimal("1.00"));
            report.setLatitude(new BigDecimal("10.77653000"));
            report.setLongitude(new BigDecimal("106.70098000"));
            report.setAddress("Seed address");
            report.setImages("https://example.com/reports/" + reportCode + ".jpg");
            // Convert string status to enum
            report.setStatus(WasteReportStatus.valueOf(status));
            report.setCreatedAt(createdAt);
            report.setUpdatedAt(createdAt);
            return repo.save(report);
        });
    }

    private void createReportImageIfMissing(ReportImageRepository repo, WasteReport report, String imageUrl) {
        if (report == null || report.getId() == null) {
            return;
        }
        if (!repo.findByReport_Id(report.getId()).isEmpty()) {
            return;
        }
        ReportImage image = new ReportImage();
        image.setReport(report);
        image.setImageUrl(imageUrl);
        image.setImageType("report");
        image.setUploadedAt(report.getCreatedAt());
        repo.save(image);
    }

    private CollectionRequest ensureCollectionRequest(
            CollectionRequestRepository repo,
            String requestCode,
            WasteReport report,
            Enterprise enterprise,
            Collector collector,
            CollectionRequestStatus status,
            LocalDateTime assignedAt,
            LocalDateTime acceptedAt,
            LocalDateTime startedAt,
            BigDecimal actualWeightKg,
            LocalDateTime collectedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return repo.findByRequestCode(requestCode).orElseGet(() -> {
            CollectionRequest cr = new CollectionRequest();
            cr.setRequestCode(requestCode);
            cr.setReport(report);
            cr.setEnterprise(enterprise);
            cr.setCollector(collector);
            cr.setStatus(status);
            cr.setAssignedAt(assignedAt);
            cr.setAcceptedAt(acceptedAt);
            cr.setStartedAt(startedAt);
            cr.setActualWeightKg(actualWeightKg);
            cr.setCollectedAt(collectedAt);
            cr.setCreatedAt(createdAt);
            cr.setUpdatedAt(updatedAt);
            return repo.save(cr);
        });
    }

    private void ensureTrackingIfMissing(CollectionTrackingRepository repo, CollectionRequest request,
            Collector collector, String action, LocalDateTime createdAt) {
        if (request == null || request.getId() == null) {
            return;
        }
        if (repo.existsByCollectionRequest_IdAndAction(request.getId(), action)) {
            return;
        }
        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(request);
        tracking.setCollector(collector);
        tracking.setAction(action);
        tracking.setNote("seed");
        tracking.setCreatedAt(createdAt);
        repo.save(tracking);
    }

    private void ensureCollectorReportIfMissing(
            CollectorReportRepository reportRepository,
            CollectorReportImageRepository imageRepository,
            CollectionRequest request,
            Collector collector,
            LocalDateTime collectedAt) {
        if (request == null || request.getId() == null) {
            return;
        }
        if (reportRepository.findByCollectionRequestId(request.getId()).isPresent()) {
            return;
        }
        CollectorReport report = new CollectorReport();
        report.setCollectionRequest(request);
        report.setCollector(collector);
        report.setStatus(CollectorReportStatus.COMPLETED);
        report.setCollectorNote("Seed completed");
        report.setCollectedAt(collectedAt);
        report.setLatitude(new BigDecimal("10.77653000"));
        report.setLongitude(new BigDecimal("106.70098000"));
        report.setCreatedAt(collectedAt);
        CollectorReport saved = reportRepository.save(report);

        CollectorReportImage img = new CollectorReportImage();
        img.setCollectorReport(saved);
        img.setImageUrl("https://example.com/collectorReports/" + saved.getId() + ".jpg");
        img.setCreatedAt(collectedAt);
        imageRepository.save(img);
    }

    private PointRule ensurePointRule(PointRuleRepository repo, Enterprise enterprise, LocalDateTime createdAt) {
        return repo.findByEnterpriseIdAndRuleName(enterprise.getId(), "Seed rule").orElseGet(() -> {
            PointRule rule = new PointRule();
            rule.setEnterprise(enterprise);
            rule.setRuleName("Seed rule");
            rule.setRuleType("BASE");
            rule.setBasePoints(50);
            rule.setMultiplier(new BigDecimal("1.00"));
            rule.setIsActive(true);
            rule.setValidFrom(createdAt);
            rule.setCreatedAt(createdAt);
            rule.setUpdatedAt(createdAt);
            return repo.save(rule);
        });
    }

    private void ensurePointTransaction(PointTransactionRepository repo, Citizen citizen, WasteReport report,
            CollectionRequest request, PointRule rule, LocalDateTime createdAt) {
        if (report == null || report.getId() == null) {
            return;
        }
        if (!repo.findByReportId(report.getId()).isEmpty()) {
            return;
        }
        PointTransaction tx = new PointTransaction();
        tx.setCitizen(citizen);
        tx.setReport(report);
        tx.setCollectionRequest(request);
        tx.setRule(rule);
        tx.setPoints(50);
        tx.setTransactionType("EARN");
        tx.setDescription("Seed points");
        tx.setBalanceAfter(50);
        tx.setCreatedAt(createdAt);
        repo.save(tx);
    }

    private void ensureFeedback(FeedbackRepository repo, Citizen citizen, CollectionRequest request,
            LocalDateTime createdAt) {
        String code = "FB-SEED-001";
        if (repo.findByFeedbackCode(code).isPresent()) {
            return;
        }
        Feedback feedback = new Feedback();
        feedback.setFeedbackCode(code);
        feedback.setCitizen(citizen);
        feedback.setCollectionRequest(request);
        feedback.setFeedbackType("COMPLAINT");
        feedback.setSubject("Seed feedback");
        feedback.setContent("Seed content");
        feedback.setStatus("pending");
        feedback.setCreatedAt(createdAt);
        feedback.setUpdatedAt(createdAt);
        repo.save(feedback);
    }

    private void updateCitizenStats(Citizen citizen) {
        if (citizen.getTotalReports() == null) {
            citizen.setTotalReports(0);
        }
        if (citizen.getTotalPoints() == null) {
            citizen.setTotalPoints(0);
        }
        if (citizen.getValidReports() == null) {
            citizen.setValidReports(0);
        }
    }
}
