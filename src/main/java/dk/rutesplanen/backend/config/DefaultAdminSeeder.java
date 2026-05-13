package dk.rutesplanen.backend.config;


import dk.rutesplanen.backend.model.User;
import dk.rutesplanen.backend.repositories.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultAdminSeeder {

    // Kører automatisk ved opstart og opretter standard admin-brugeren hvis den ikke allerede findes
    @Bean
    public ApplicationRunner seedAdmin(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmailAndPassword("admin@rutesplanen.dk", "admin123").isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@rutesplanen.dk");
                admin.setPassword("admin123");
                admin.setActive(true);
                userRepository.save(admin);
            }
        };
    }
}