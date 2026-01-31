package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Permission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.RolePermission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.PermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RolePermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CitizenRepository citizenRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Seed Permissions
            Permission createReport = createPermissionIfNotFound(permissionRepository, "CREATE_REPORT", "Create waste report", "CITIZEN");
            Permission viewOwnReports = createPermissionIfNotFound(permissionRepository, "VIEW_OWN_REPORTS", "View own waste reports", "CITIZEN");
            
            Permission viewAreaReports = createPermissionIfNotFound(permissionRepository, "VIEW_AREA_REPORTS", "View reports in assigned area", "ENTERPRISE");
            Permission assignCollector = createPermissionIfNotFound(permissionRepository, "ASSIGN_COLLECTOR", "Assign collector to report", "ENTERPRISE");
            
            Permission viewTasks = createPermissionIfNotFound(permissionRepository, "VIEW_ASSIGNED_TASKS", "View assigned collection tasks", "COLLECTOR");
            Permission updateStatus = createPermissionIfNotFound(permissionRepository, "UPDATE_TASK_STATUS", "Update task collection status", "COLLECTOR");

            // 2. Seed Roles
            Role citizenRole = createRoleIfNotFound(roleRepository, "CITIZEN", "Citizen User");
            Role enterpriseRole = createRoleIfNotFound(roleRepository, "ENTERPRISE", "Recycling Enterprise");
            Role collectorRole = createRoleIfNotFound(roleRepository, "COLLECTOR", "Waste Collector");
            Role entAdminRole = createRoleIfNotFound(roleRepository, "ENTERPRISE_ADMIN", "Enterprise Administrator");
            Role adminRole = createRoleIfNotFound(roleRepository, "ADMIN", "System Admin");

            // 3. Link Roles and Permissions
            assignPermissionToRole(rolePermissionRepository, citizenRole, createReport);
            assignPermissionToRole(rolePermissionRepository, citizenRole, viewOwnReports);
            
            assignPermissionToRole(rolePermissionRepository, enterpriseRole, viewAreaReports);
            assignPermissionToRole(rolePermissionRepository, entAdminRole, viewAreaReports);
            assignPermissionToRole(rolePermissionRepository, entAdminRole, assignCollector);
            
            assignPermissionToRole(rolePermissionRepository, collectorRole, viewTasks);
            assignPermissionToRole(rolePermissionRepository, collectorRole, updateStatus);

            // 4. Seed Users
            createUserIfNotFound(userRepository, passwordEncoder, "citizen@test.com", "citizen123", "Test Citizen", citizenRole);
            createUserIfNotFound(userRepository, passwordEncoder, "enterprise@test.com", "enterprise123", "Test Enterprise", enterpriseRole);
            createUserIfNotFound(userRepository, passwordEncoder, "collector@test.com", "collector123", "Test Collector", collectorRole);
            createUserIfNotFound(userRepository, passwordEncoder, "admin@test.com", "admin123", "Test Admin", adminRole);

            userRepository.findByEmail("citizen@test.com").ifPresent(user -> createCitizenIfNotFound(citizenRepository, user));
        };
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

    private void createCitizenIfNotFound(CitizenRepository citizenRepository, User user) {
        if (user.getId() == null) {
            return;
        }
        if (citizenRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        Citizen citizen = new Citizen();
        citizen.setUser(user);
        citizen.setEmail(user.getEmail());
        citizen.setFullName(user.getFullName());
        citizen.setPasswordHash(user.getPasswordHash());
        citizen.setPhone(user.getPhone());
        citizen.setTotalPoints(0);
        citizen.setTotalReports(0);
        citizen.setValidReports(0);
        citizenRepository.save(citizen);
    }
}
