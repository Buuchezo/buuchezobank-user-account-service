package com.buuchezo.useraccountservice.config;

import com.buuchezo.useraccountservice.entity.Admin;
import com.buuchezo.useraccountservice.entity.Role;
import com.buuchezo.useraccountservice.repository.AdminRepository;
import com.buuchezo.useraccountservice.repository.RoleRepository;
import com.buuchezo.useraccountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${app.admin.bootstrap.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.admin.bootstrap.email:}")
    private String adminEmail;

    @Value("${app.admin.bootstrap.password:}")
    private String adminPassword;

    @Value("${app.admin.bootstrap.first-name:Admin}")
    private String adminFirstName;

    @Value("${app.admin.bootstrap.last-name:User}")
    private String adminLastName;

    @Override
    public void run(String... args) {

        if (!bootstrapEnabled) {
            log.info("Admin bootstrap is disabled.");
            return;
        }

        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but admin email/password are missing"
            );
        }

        /*
         * ============================================================
         * 1. Ensure the separate ADMIN identity exists.
         * ============================================================
         */
        if (adminRepository.existsByEmail(adminEmail)) {

            log.info(
                    "Admin account already exists for {}",
                    adminEmail
            );

        } else {

            Admin admin = Admin.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .enabled(true)
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .build();

            adminRepository.save(admin);

            log.info(
                    "Initial admin account created for {}",
                    adminEmail
            );
        }

        /*
         * ============================================================
         * 2. Ensure the customer identity has CUSTOMER only.
         *
         * The same email may exist in both tables, but the User
         * entity must NEVER contain ADMIN.
         * ============================================================
         */
        userRepository.findByEmail(adminEmail).ifPresent(user -> {

            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "CUSTOMER role not found"
                            )
                    );

            Set<Role> currentRoles = user.getRoles();

            if (currentRoles == null) {
                currentRoles = new HashSet<>();
            }

            boolean alreadyCustomerOnly =
                    currentRoles.size() == 1
                            && currentRoles.contains(customerRole);

            if (alreadyCustomerOnly) {

                log.info(
                        "Customer identity {} already has CUSTOMER role only",
                        adminEmail
                );

                return;
            }

            /*
             * Replace the complete role collection instead of
             * removing one Role object from the existing collection.
             *
             * This guarantees that ADMIN cannot remain attached
             * to the customer identity.
             */
            Set<Role> customerOnlyRoles = new HashSet<>();
            customerOnlyRoles.add(customerRole);

            user.setRoles(customerOnlyRoles);
            userRepository.saveAndFlush(user);

            log.info(
                    "Customer/admin role separation completed for {}. Customer roles are now CUSTOMER only.",
                    adminEmail
            );
        });
    }
}
