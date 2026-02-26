package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.UserRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.seed.modular", havingValue = "true")
public class SeedUsersInitializer {

    @Bean
    public CommandLineRunner seedUsers(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CitizenRepository citizenRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role citizenRole = roleRepository.findByRoleCode("CITIZEN").orElse(null);
            Role enterpriseRole = roleRepository.findByRoleCode("ENTERPRISE").orElse(null);
            Role collectorRole = roleRepository.findByRoleCode("COLLECTOR").orElse(null);
            Role adminRole = roleRepository.findByRoleCode("ADMIN").orElse(null);

            createUserIfNotFound(userRepository, passwordEncoder, "citizen@test.com", "citizen123", "Test Citizen", citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "citizen2@test.com", "citizen123", "Test Citizen 2", citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "enterprise@test.com", "enterprise123", "Test Enterprise", enterpriseRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector@test.com", "collector123", "Test Collector", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector2@test.com", "collector123", "Test Collector 2", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "admin@test.com", "admin123", "Test Admin", adminRole);

            userRepository.findByEmail("citizen@test.com")
                    .ifPresent(u -> createCitizenIfNotFound(citizenRepository, u));
            userRepository.findByEmail("citizen2@test.com")
                    .ifPresent(u -> createCitizenIfNotFound(citizenRepository, u));
        };
    }

    private void createUserIfNotFound(UserRepository userRepository, PasswordEncoder passwordEncoder, String email,
                                      String password, String fullName, Role role) {
        if (role == null) return;
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

    private void createCitizenIfNotFound(CitizenRepository citizenRepository, User user) {
        if (user.getId() == null) return;
        citizenRepository.findByUserId(user.getId()).orElseGet(() -> {
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
}

