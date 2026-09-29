package com.jewelvaulterp.bootstrap;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.role.dto.AssignRoleRequest;
import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.role.service.RoleService;
import com.jewelvaulterp.role.service.UserRoleService;
import com.jewelvaulterp.user.dto.CreateUserRequest;
import com.jewelvaulterp.user.dto.UserResponse;
import com.jewelvaulterp.user.repository.UserRepository;
import com.jewelvaulterp.user.service.UserService;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InitialAdminBootstrapService {

    private static final String COMPANY_NAME = "JewelVaultERP";
    private static final String COUNTRY_CODE = "LK";
    private static final String CURRENCY_CODE = "LKR";

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final Validator validator;

    public InitialAdminBootstrapService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            RoleService roleService,
            UserService userService,
            UserRoleService userRoleService,
            Validator validator
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.validator = validator;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BootstrapResult createInitialAdmin(String username, String email, String password) {
        CreateUserRequest validationRequest = new CreateUserRequest(
                UUID.randomUUID(), username, email, password
        );
        if (!validator.validate(validationRequest).isEmpty()) {
            throw new IllegalArgumentException("Initial admin details are invalid.");
        }

        if (userRepository.existsActiveAdmin()) {
            throw new BootstrapAlreadyInitializedException();
        }
        if (companyRepository.count() != 0 || userRepository.count() != 0 || roleRepository.count() != 0) {
            throw new BootstrapAlreadyInitializedException();
        }

        LocalDateTime now = LocalDateTime.now();
        Company company = companyRepository.save(new Company(
                UUID.randomUUID(), COMPANY_NAME, null, COUNTRY_CODE, CURRENCY_CODE, now, now
        ));
        RoleResponse role = roleService.create(new CreateRoleRequest(
                company.getId(), "ADMIN", "Initial company administrator"
        ));
        UserResponse user = userService.createUser(new CreateUserRequest(
                company.getId(), username, email, password
        ));
        userRoleService.assignRole(user.id(), new AssignRoleRequest(role.id()));

        return new BootstrapResult(company.getId(), role.id(), user.id());
    }

    public record BootstrapResult(UUID companyId, UUID adminRoleId, UUID adminUserId) {
    }

    public static final class BootstrapAlreadyInitializedException extends IllegalStateException {

        public BootstrapAlreadyInitializedException() {
            super("Database contains an ADMIN or existing tenant/identity data; bootstrap refused.");
        }
    }
}