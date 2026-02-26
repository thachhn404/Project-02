package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Permission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.RolePermission;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.PermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RolePermissionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.seed.modular", havingValue = "true")
public class SeedAuthInitializer {

    @Bean
    public CommandLineRunner seedAuthData(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
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

    private Role createRoleIfNotFound(RoleRepository roleRepository, String code, String name) {
        return roleRepository.findByRoleCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setRoleCode(code);
            role.setRoleName(name);
            return roleRepository.save(role);
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
}

