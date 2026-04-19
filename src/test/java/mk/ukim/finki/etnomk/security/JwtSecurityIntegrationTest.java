package mk.ukim.finki.etnomk.security;

import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:etnomk-security-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"
})
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("stefan");
        user.setEmail("stefan@example.com");
        user.setPassword(passwordEncoder.encode("stefan123"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }

    @Test
    void jwtLoginReturnsBearerTokenWithoutLeakingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"stefan","password":"stefan123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(content().string(not(containsString("stefan123"))));
    }

    @Test
    void apiRequestsWithoutJwtReturnUnauthorizedInsteadOfLoginRedirect() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void invalidJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/does-not-exist")
                        .header(JwtConstants.HEADER, JwtConstants.TOKEN_PREFIX + "not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUsersCannotRegisterAdmins() throws Exception {
        mockMvc.perform(post("/admin/register")
                        .param("username", "new-admin")
                        .param("email", "admin@example.com")
                        .param("password", "admin123")
                        .param("confirmPassword", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedAdminCanOpenAdminRegistrationPage() throws Exception {
        mockMvc.perform(get("/admin/register")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void formLoginRedirectsAdminsToAdminDashboard() throws Exception {
        mockMvc.perform(post("/perform_login")
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void formLoginRedirectsNormalUsersHome() throws Exception {
        mockMvc.perform(post("/perform_login")
                        .param("username", "stefan")
                        .param("password", "stefan123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
