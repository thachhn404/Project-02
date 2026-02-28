package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name = "app.seed.modular", havingValue = "true")
public class SeedEnterpriseCollectorInitializer {

    @Bean
    public CommandLineRunner seedEnterpriseAndCollectors(
            UserRepository userRepository,
            EnterpriseRepository enterpriseRepository,
            CollectorRepository collectorRepository
    ) {
        return args -> {
            User entUser = userRepository.findByEmail("enterprise@test.com").orElse(null);
            if (entUser != null) {
                Enterprise enterprise = createEnterpriseIfNotFound(enterpriseRepository, entUser);
                ensureEnterpriseSeedFields(enterpriseRepository, enterprise);
                linkEnterpriseToUserIfMissing(userRepository, entUser, enterprise);

                userRepository.findByEmail("collector@test.com")
                        .ifPresent(u -> createCollectorIfNotFound(collectorRepository, u, enterprise));
                userRepository.findByEmail("collector2@test.com")
                        .ifPresent(u -> createCollectorIfNotFound(collectorRepository, u, enterprise));
            }
        };
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
            enterprise.setSupportedWasteTypeCodes("PLASTIC,PAPER,RECYCLABLE");
            enterprise.setServiceWards("Ward 1,Ward 2");
            enterprise.setServiceCities("City A");
            enterprise.setCreatedAt(LocalDateTime.now());
            enterprise.setUpdatedAt(LocalDateTime.now());
            return enterpriseRepository.save(enterprise);
        });
    }

    private void ensureEnterpriseSeedFields(EnterpriseRepository enterpriseRepository, Enterprise enterprise) {
        if (enterprise == null || enterprise.getId() == null) return;
        boolean changed = false;
        if (enterprise.getSupportedWasteTypeCodes() == null || enterprise.getSupportedWasteTypeCodes().isBlank()) {
            enterprise.setSupportedWasteTypeCodes("PLASTIC,PAPER,RECYCLABLE");
            changed = true;
        }
        if (enterprise.getServiceWards() == null || enterprise.getServiceWards().isBlank()) {
            enterprise.setServiceWards("Ward 1,Ward 2");
            changed = true;
        }
        if (enterprise.getServiceCities() == null || enterprise.getServiceCities().isBlank()) {
            enterprise.setServiceCities("City A");
            changed = true;
        }
        if (changed) {
            enterprise.setUpdatedAt(LocalDateTime.now());
            enterpriseRepository.save(enterprise);
        }
    }

    private void linkEnterpriseToUserIfMissing(UserRepository userRepository, User enterpriseUser, Enterprise enterprise) {
        if (enterprise == null) return;
        if (enterpriseUser.getEnterprise() != null && enterpriseUser.getEnterprise().getId() != null) return;
        enterpriseUser.setEnterprise(enterprise);
        userRepository.save(enterpriseUser);
    }

    private Collector createCollectorIfNotFound(CollectorRepository collectorRepository, User collectorUser, Enterprise enterprise) {
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
            collector.setViolationCount(0);
            collector.setCreatedAt(LocalDateTime.now());
            return collectorRepository.save(collector);
        });
    }
}
