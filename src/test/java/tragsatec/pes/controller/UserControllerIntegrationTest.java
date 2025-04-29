package tragsatec.pes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    private Integer createdUserId;
    private String createdUsername; // Variable para almacenar el username
    private String firstRoleId; // Variable para almacenar el ID del primer rol
    private String authToken;

    // Este método se ejecuta una vez antes de todas las pruebas de la clase
    @BeforeAll
    void setupClass() throws Exception {
        this.firstRoleId = "ADMIN";

        // Genera el token de autenticación
        String loginJson = "{\"username\":\"ben\", \"password\":\"benspassword\"}";

        String response = mockMvc.perform(post("/login") // Llama al endpoint de login
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk()) // Espera una respuesta exitosa
                .andExpect(jsonPath("$.token").exists()) // Verifica que el token exista en la respuesta
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrae y guarda el token con el prefijo "Bearer "
        this.authToken = "Bearer " + objectMapper.readTree(response).get("token").asText();
        assertNotNull(this.authToken, "Setup failed: Auth token is null after login.");
    }

    @Test
    @Order(1)
    void shouldCreateUser() throws Exception {
        // Define los datos del nuevo usuario
        String newUsername = "newuser_test_" + System.currentTimeMillis();
        boolean newLocked = false;
        // Usa el ID del rol obtenido en setupClass()
        String roleIdToUse = this.firstRoleId;

        String createUserJson = String.format(
                "{\"username\":\"%s\", \"locked\":%b, \"role\":%s}",
                newUsername, newLocked, roleIdToUse
        );

        mockMvc.perform(post("/users")
                        .header("Authorization", authToken)
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
                    createdUsername = objectMapper.readTree(jsonResponse).get("username").asText(); // Guarda el username
                });

        assertNotNull(createdUserId, "Created User ID should not be null after creation");
        assertNotNull(createdUsername, "Created Username should not be null after creation");
    }

    @Test
    @Order(2)
    void shouldGetUserByUsername() throws Exception {
        assertNotNull(createdUsername, "Cannot get user by username if createdUsername is null");

        mockMvc.perform(get("/users/username/" + createdUsername)
                        .header("Authorization", authToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdUserId))
                .andExpect(jsonPath("$.username").value(createdUsername))
                .andExpect(jsonPath("$.role.id").value(firstRoleId));
    }


    @Test
    @Order(3)
    void shouldUpdateCreatedUser() throws Exception {
        assertNotNull(createdUserId, "Cannot update user if createdUserId is null");
        // Usa el ID del rol obtenido en setupClass()
        String roleIdToUse = this.firstRoleId;

        // Nuevos datos para la actualización
        String updatedUsername = "updateduser_" + createdUserId;
        boolean updatedLocked = true;

        String updateUserJson = String.format(
                "{\"id\":%d, \"username\":\"%s\", \"locked\":%b, \"role\":%s}",
                createdUserId, updatedUsername, updatedLocked, roleIdToUse
        );

        mockMvc.perform(put("/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdUserId))
                .andExpect(jsonPath("$.username").value(updatedUsername))
                .andExpect(jsonPath("$.locked").value(updatedLocked))
                .andExpect(jsonPath("$.role.id").value(roleIdToUse))
                .andDo(result -> {
                    // Actualiza el username guardado si la actualización fue exitosa
                    createdUsername = updatedUsername;
                });
    }

    @Test
    @Order(4)
    void shouldDeleteCreatedUser() throws Exception {
        assertNotNull(createdUserId, "Cannot delete user if createdUserId is null");

        // Añade el encabezado de autorización a la solicitud DELETE
        mockMvc.perform(delete("/users/" + createdUserId)
                        .header("Authorization", authToken)) // Añade el encabezado aquí
                .andExpect(status().isNoContent());

        // Añade el encabezado de autorización a la solicitud GET para verificar la eliminación por ID
        mockMvc.perform(get("/users/" + createdUserId)
                        .header("Authorization", authToken) // Añade el encabezado aquí
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Añade el encabezado de autorización a la solicitud GET para verificar la eliminación por username
        assertNotNull(createdUsername, "Cannot verify deletion by username if createdUsername is null");
        mockMvc.perform(get("/users/username/" + createdUsername)
                        .header("Authorization", authToken) // Añade el encabezado aquí
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera Not Found también por username
    }

    @Test
    @Order(5)
    void shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", authToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verifica que la respuesta es un array JSON (puede estar vacío o no)
                .andExpect(jsonPath("$").isArray());
    }
}