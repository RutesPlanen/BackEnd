package dk.rutesplanen.backend.config;

import dk.rutesplanen.backend.model.Role;
import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import dk.rutesplanen.backend.service.LoginTokenService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultAdminSeeder {

    // Kører automatisk ved opstart og opretter standard admin-brugeren hvis den ikke allerede findes
    @Bean
    public ApplicationRunner seedAdmin(UserRepository userRepository, LoginTokenService loginTokenService) {
        return args -> {
            String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@rutesplanen.dk");
            String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(loginTokenService.getPasswordEncoder().encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
            }
        };
    }
}
