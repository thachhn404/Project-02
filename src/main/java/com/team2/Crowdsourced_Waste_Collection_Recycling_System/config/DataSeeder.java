package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Role citizenRole = createRoleIfNotFound(roleRepository, "CITIZEN", "Citizen User");
            Role enterpriseRole = createRoleIfNotFound(roleRepository, "ENTERPRISE", "Recycling Enterprise");
            Role collectorRole = createRoleIfNotFound(roleRepository, "COLLECTOR", "Waste Collector");
            Role entAdminRole = createRoleIfNotFound(roleRepository, "ENTERPRISE_ADMIN", "Enterprise Administrator");
            Role adminRole = createRoleIfNotFound(roleRepository, "ADMIN", "System Admin");
            
            createUserIfNotFound(userRepository, passwordEncoder, "citizen@test.com", "citizen123", "Test Citizen", citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "enterprise@test.com", "enterprise123", "Test Enterprise", enterpriseRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector@test.com", "collector123", "Test Collector", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "admin@test.com", "admin123", "Test Admin", adminRole);
        };
    }

    private Role createRoleIfNotFound(RoleRepository roleRepository, String code, String name) {
        return roleRepository.findByRoleCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setRoleCode(code);
            role.setRoleName(name);
            System.out.println("Role " + code + " created");
            return roleRepository.save(role);
        });
    }

    private void createUserIfNotFound(UserRepository userRepository, PasswordEncoder passwordEncoder, String email, String password, String fullName, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setRole(role);
            user.setStatus("active");
            userRepository.save(user);
            System.out.println("User " + email + " created with role " + role.getRoleCode());
        }
    }
}
