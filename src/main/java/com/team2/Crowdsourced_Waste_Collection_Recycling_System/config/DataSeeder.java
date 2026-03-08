package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Permission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.RolePermission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Voucher;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.VoucherRedemption;
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
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.PermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RolePermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.feedback.FeedbackRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRedemptionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PointTransactionRepository pointTransactionRepository,
            VoucherRepository voucherRepository,
            VoucherRedemptionRepository voucherRedemptionRepository,
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
            Permission adminViewUsers = createPermissionIfNotFound(permissionRepository, "ADMIN_VIEW_USERS",
                    "View all user accounts", "ADMIN");
            Permission adminSuspendUsers = createPermissionIfNotFound(permissionRepository, "ADMIN_SUSPEND_USERS",
                    "Suspend user accounts", "ADMIN");
            Permission adminActivateUsers = createPermissionIfNotFound(permissionRepository, "ADMIN_ACTIVATE_USERS",
                    "Activate suspended user accounts", "ADMIN");
            Permission adminCreateAccounts = createPermissionIfNotFound(permissionRepository, "ADMIN_CREATE_ACCOUNTS",
                    "Create system accounts", "ADMIN");
            Permission adminChangeRoles = createPermissionIfNotFound(permissionRepository, "ADMIN_CHANGE_ROLES",
                    "Change user roles", "ADMIN");

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
            assignPermissionToRole(rolePermissionRepository, adminRole, adminViewUsers);
            assignPermissionToRole(rolePermissionRepository, adminRole, adminSuspendUsers);
            assignPermissionToRole(rolePermissionRepository, adminRole, adminActivateUsers);
            assignPermissionToRole(rolePermissionRepository, adminRole, adminCreateAccounts);
//            assignPermissionToRole(rolePermissionRepository, adminRole, adminChangeRoles);

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
            seedVouchers(voucherRepository);
            seedVoucherRedemptions(citizen1, voucherRepository, voucherRedemptionRepository);

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
                    pointTransactionRepository,
                    feedbackRepository);
        };
    }

    private void seedVouchers(VoucherRepository voucherRepository) {
        LocalDateTime now = LocalDateTime.now();

        Voucher v1 = new Voucher();
        v1.setBannerUrl("https://jollibee.com.vn/media/catalog/product/cache/9011257231b13517d19d9bae81fd87cc/m/_/m_n_ngon_ph_i_th_-_1.png");
        v1.setLogoUrl("https://upload.wikimedia.org/wikipedia/en/8/84/Jollibee_2011_logo.svg");
        v1.setTitle("Jollibee Voucher - 50,000 VND");
        v1.setValueDisplay("50,000 VND");
        v1.setPointsRequired(55000);
        v1.setValidUntil(LocalDate.of(2026, 12, 31));
        v1.setActive(true);
        v1.setRemainingStock(100);
        v1.setTerms(List.of(
                "Valid for dine-in and takeout only.",
                "Not applicable for delivery orders.",
                "One voucher per transaction."
        ));
        v1.setCreatedAt(now);
        v1.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v1);

        Voucher v2 = new Voucher();
        v2.setBannerUrl("https://s3-hcmc02.higiocloud.vn/phuclong/2025/04/image-20250409083419.png");
        v2.setLogoUrl("https://www.phuclong.com.vn/_next/static/images/logo-ba196fcddcd6f23a70406fd4cf71d422.png");
        v2.setTitle("Phuc Long Voucher - 30,000 VND");
        v2.setValueDisplay("30,000 VND");
        v2.setPointsRequired(33000);
        v2.setValidUntil(LocalDate.of(2026, 11, 30));
        v2.setActive(true);
        v2.setRemainingStock(100);
        v2.setTerms(List.of(
                "Applicable for all beverages.",
                "Valid at all Phuc Long stores nationwide.",
                "Cannot be combined with other promotions."
        ));
        v2.setCreatedAt(now);
        v2.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v2);

        Voucher v3 = new Voucher();
        v3.setBannerUrl("https://katinat.vn/wp-content/uploads/2024/03/image.png");
        v3.setLogoUrl("https://katinat.vn/wp-content/uploads/2023/12/cropped-Kat-Logo-fa-rgb-05__1_-removebg-preview.png");
        v3.setTitle("Katinat Voucher - 50,000 VND");
        v3.setValueDisplay("50,000 VND");
        v3.setPointsRequired(55000);
        v3.setValidUntil(LocalDate.of(2026, 12, 15));
        v3.setActive(true);
        v3.setRemainingStock(100);
        v3.setTerms(List.of(
                "Valid at all Katinat branches.",
                "Not redeemable for cash.",
                "Valid for one-time use only."
        ));
        v3.setCreatedAt(now);
        v3.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v3);

        Voucher v4 = new Voucher();
        v4.setBannerUrl("https://static.kfcvietnam.com.vn/images/category/lg/MON%20AN%20NHE.jpg?v=LZrXEL");
        v4.setLogoUrl("https://web.archive.org/web/20220716042518im_/https://brasol.vn/public/ckeditor/uploads/thiet-ke-logo-tin-tuc/logo-kfc-png.png");
        v4.setTitle("KFC Voucher - 50,000 VND");
        v4.setValueDisplay("50,000 VND");
        v4.setPointsRequired(55000);
        v4.setValidUntil(LocalDate.of(2026, 12, 31));
        v4.setActive(true);
        v4.setRemainingStock(100);
        v4.setTerms(List.of(
                "Valid for all menu items.",
                "Show QR code at cashier.",
                "Not valid with other discounts."
        ));
        v4.setCreatedAt(now);
        v4.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v4);

        Voucher v5 = new Voucher();
        v5.setBannerUrl("https://www.highlandscoffee.com.vn/vnt_upload/home/web_banner_2000x2000.jpg");
        v5.setLogoUrl("https://www.highlandscoffee.com.vn/vnt_upload/weblink/red_BG_logo800.png");
        v5.setTitle("Highlands Coffee Voucher - 100,000 VND");
        v5.setValueDisplay("100,000 VND");
        v5.setPointsRequired(110000);
        v5.setValidUntil(LocalDate.of(2026, 12, 31));
        v5.setActive(true);
        v5.setRemainingStock(100);
        v5.setTerms(List.of(
                "Valid for all drinks and food items.",
                "Minimum bill required.",
                "Cannot be exchanged for cash."
        ));
        v5.setCreatedAt(now);
        v5.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v5);

        Voucher v6 = new Voucher();
        v6.setBannerUrl("https://cdn.prod.website-files.com/649249d29a20bd6bc3deac48/649249d29a20bd6bc3deae34_TousLesJours_MangoCloudCake.jpg");
        v6.setLogoUrl("https://cdn.prod.website-files.com/649249d29a20bd6bc3deac45/69692c3d9117f3d73ff839fa_4.0%20BI_Logo_Full_Green-p-1080.png");
        v6.setTitle("Tous Les Jours Voucher - 50,000 VND");
        v6.setValueDisplay("50,000 VND");
        v6.setPointsRequired(55000);
        v6.setValidUntil(LocalDate.of(2026, 10, 31));
        v6.setActive(true);
        v6.setRemainingStock(100);
        v6.setTerms(List.of(
                "Applicable for all bakery products.",
                "Valid for in-store purchases only.",
                "One voucher per receipt."
        ));
        v6.setCreatedAt(now);
        v6.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v6);

        Voucher v7 = new Voucher();
        v7.setBannerUrl("https://dingtea.vn/images/thu-3/image_cover.jpg");
        v7.setLogoUrl("https://dingtea.vn/images/logospare.png");
        v7.setTitle("Ding Tea Voucher - 50,000 VND");
        v7.setValueDisplay("50,000 VND");
        v7.setPointsRequired(55000);
        v7.setValidUntil(LocalDate.of(2026, 9, 30));
        v7.setActive(true);
        v7.setRemainingStock(100);
        v7.setTerms(List.of(
                "Valid for all drinks.",
                "Cannot be combined with other promotions.",
                "Valid nationwide."
        ));
        v7.setCreatedAt(now);
        v7.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v7);

        Voucher v8 = new Voucher();
        v8.setBannerUrl("https://www.lotteria.vn/media/catalog/product/cache/400x400/g/_/g_r_n_ph_n_1_3.jpg.webp");
        v8.setLogoUrl("https://www.lotteria.vn/grs-static/images/logo-white.svg");
        v8.setTitle("Lotteria Voucher - 50,000 VND");
        v8.setValueDisplay("50,000 VND");
        v8.setPointsRequired(55000);
        v8.setValidUntil(LocalDate.of(2026, 12, 31));
        v8.setActive(true);
        v8.setRemainingStock(100);
        v8.setTerms(List.of(
                "Valid for all menu items.",
                "Not applicable for delivery.",
                "One-time redemption only."
        ));
        v8.setCreatedAt(now);
        v8.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v8);

        Voucher v9 = new Voucher();
        v9.setBannerUrl("https://content-prod-live.cert.starbucks.com/binary/v2/asset/137-106110.jpg");
        v9.setLogoUrl("https://mondialbrand.com/wp-content/uploads/2023/08/logo-starbucks-y-nghia-va-lich-su-cua-bieu-tuong-ca-phe-nang-tien-ca-tu-1917-8.jpg");
        v9.setTitle("Starbucks Voucher - 100,000 VND");
        v9.setValueDisplay("100,000 VND");
        v9.setPointsRequired(110000);
        v9.setValidUntil(LocalDate.of(2026, 12, 31));
        v9.setActive(true);
        v9.setRemainingStock(100);
        v9.setTerms(List.of(
                "Valid at all Starbucks Vietnam stores.",
                "Not valid for bottled beverages.",
                "Cannot be redeemed for cash."
        ));
        v9.setCreatedAt(now);
        v9.setUpdatedAt(now);
        createVoucherIfNotFound(voucherRepository, v9);
    }

    private void createVoucherIfNotFound(VoucherRepository voucherRepository, Voucher voucher) {
        if (voucher.getTitle() == null || voucher.getTitle().isBlank()) {
            return;
        }
        if (voucherRepository.findByTitleIgnoreCase(voucher.getTitle()).isPresent()) {
            return;
        }
        Voucher saved = voucherRepository.save(voucher);
        if (saved.getId() != null && (saved.getVoucherCode() == null || saved.getVoucherCode().isBlank())) {
            saved.setVoucherCode(String.format("V%03d", saved.getId()));
            voucherRepository.save(saved);
        }
    }

    private void seedVoucherRedemptions(
            Citizen citizen,
            VoucherRepository voucherRepository,
            VoucherRedemptionRepository voucherRedemptionRepository) {
        if (citizen == null || citizen.getId() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        seedVoucherRedemption(citizen, voucherRepository, voucherRedemptionRepository,
                "Jollibee Voucher - 50,000 VND", "VOUCHER-JOLLI-001", 55000, "ACTIVE", now.minusDays(2));
        seedVoucherRedemption(citizen, voucherRepository, voucherRedemptionRepository,
                "Phuc Long Voucher - 30,000 VND", "VOUCHER-PLONG-002", 33000, "USED", now.minusDays(10));
        seedVoucherRedemption(citizen, voucherRepository, voucherRedemptionRepository,
                "Katinat Voucher - 50,000 VND", "VOUCHER-KATIN-003", 55000, "EXPIRED", now.minusDays(40));
        seedVoucherRedemption(citizen, voucherRepository, voucherRedemptionRepository,
                "KFC Voucher - 50,000 VND", "VOUCHER-KFCVN-004", 55000, "ACTIVE", now.minusDays(1));
        seedVoucherRedemption(citizen, voucherRepository, voucherRedemptionRepository,
                "Highlands Coffee Voucher - 100,000 VND", "VOUCHER-HIGH-005", 110000, "ACTIVE", now.minusDays(5));
    }

    private void seedVoucherRedemption(
            Citizen citizen,
            VoucherRepository voucherRepository,
            VoucherRedemptionRepository voucherRedemptionRepository,
            String voucherTitle,
            String redemptionCode,
            Integer pointsSpent,
            String status,
            LocalDateTime redeemedAt) {
        if (voucherTitle == null || voucherTitle.isBlank()
                || redemptionCode == null || redemptionCode.isBlank()) {
            return;
        }

        if (voucherRedemptionRepository.existsByRedemptionCodeIgnoreCase(redemptionCode)) {
            return;
        }

        Voucher voucher = voucherRepository.findByTitleIgnoreCase(voucherTitle).orElse(null);
        if (voucher == null || voucher.getId() == null) {
            return;
        }

        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setCitizen(citizen);
        redemption.setVoucher(voucher);
        redemption.setRedemptionCode(redemptionCode);
        redemption.setPointsSpent(pointsSpent != null ? pointsSpent : 0);
        redemption.setStatus(status != null ? status : "ACTIVE");
        redemption.setRedeemedAt(redeemedAt != null ? redeemedAt : LocalDateTime.now());
        voucherRedemptionRepository.save(redemption);
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
            collector.setStatus(CollectorStatus.ONLINE);
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

        ensureCollectorReportIfMissing(collectorReportRepository, cr5, collector2,
                now.minusDays(1).plusHours(5));

        ensurePointTransaction(pointTransactionRepository, citizen, r5, cr5, now.minusDays(1).plusHours(5));

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
            CollectionRequest request,
            Collector collector,
            LocalDateTime collectedAt) {
        if (request == null || request.getId() == null) {
            return;
        }
        if (reportRepository.existsByCollectionRequest_Id(request.getId())) {
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
        reportRepository.save(report);
    }

    private void ensurePointTransaction(PointTransactionRepository repo, Citizen citizen, WasteReport report,
            CollectionRequest request, LocalDateTime createdAt) {
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
