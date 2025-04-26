package tragsatec.pes.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.junit.jupiter.api.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.http.MediaType;
    import org.springframework.test.web.servlet.MockMvc;
    import tragsatec.pes.persistence.entity.RoleEntity;
    import tragsatec.pes.persistence.repository.RoleRepository;

    import java.util.List;

    import static org.hamcrest.Matchers.*;
    import static org.junit.jupiter.api.Assertions.assertFalse;
    import static org.junit.jupiter.api.Assertions.assertNotNull;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @SpringBootTest
    @AutoConfigureMockMvc
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permite @BeforeAll no estático y variables de instancia compartidas
    public class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private RoleRepository roleRepository;

        private Integer createdUserId;
        private Integer firstRoleId; // Variable para almacenar el ID del primer rol

        // Este método se ejecuta una vez antes de todas las pruebas de la clase
        @BeforeAll
        void setupClass() {
            // Obtén el primer rol de la base de datos una sola vez
            List<RoleEntity> roles = (List<RoleEntity>) roleRepository.findAll();
            assertFalse(roles.isEmpty(), "Setup failed: No roles found in the database.");
            this.firstRoleId = roles.get(0).getId(); // Almacena el ID del primer rol
            assertNotNull(this.firstRoleId, "Setup failed: First role ID is null.");
        }

        @Test
        @Order(1)
        void shouldCreateUser() throws Exception {
            // Define los datos del nuevo usuario
            String newUsername = "newuser_test_" + System.currentTimeMillis();
            boolean newLocked = false;
            // Usa el ID del rol obtenido en setupClass()
            int roleIdToUse = this.firstRoleId;

            String createUserJson = String.format(
                    "{\"username\":\"%s\", \"locked\":%b, \"role\":%d}",
                    newUsername, newLocked, roleIdToUse
            );

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createUserJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.id").value(greaterThan(0)))
                    .andExpect(jsonPath("$.username").value(newUsername))
                    .andExpect(jsonPath("$.locked").value(newLocked))
                    .andExpect(jsonPath("$.role.id").value(roleIdToUse))
                    .andDo(result -> {
                        String jsonResponse = result.getResponse().getContentAsString();
                        createdUserId = objectMapper.readTree(jsonResponse).get("id").asInt();
                    });

            assertNotNull(createdUserId, "Created User ID should not be null after creation");
        }

        @Test
        @Order(2)
        void shouldUpdateCreatedUser() throws Exception {
            assertNotNull(createdUserId, "Cannot update user if createdUserId is null");
            // Usa el ID del rol obtenido en setupClass()
            int roleIdToUse = this.firstRoleId;

            // Nuevos datos para la actualización
            String updatedUsername = "updateduser_" + createdUserId;
            boolean updatedLocked = true;

            String updateUserJson = String.format(
                    "{\"id\":%d, \"username\":\"%s\", \"locked\":%b, \"role\":%d}",
                    createdUserId, updatedUsername, updatedLocked, roleIdToUse
            );

            mockMvc.perform(put("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateUserJson))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(createdUserId))
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.locked").value(updatedLocked))
                    .andExpect(jsonPath("$.role.id").value(roleIdToUse));
        }

        @Test
        @Order(3)
        void shouldDeleteCreatedUser() throws Exception {
            assertNotNull(createdUserId, "Cannot delete user if createdUserId is null");

            mockMvc.perform(delete("/users/" + createdUserId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/users/" + createdUserId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(4)
        void shouldReturnAtLeastOneUser() throws Exception {
            mockMvc.perform(get("/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }
    }