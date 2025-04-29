package tragsatec.pes;

        import org.junit.jupiter.api.Test;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
        import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
        import tragsatec.pes.persistence.entity.PermissionEntity;
        import tragsatec.pes.persistence.entity.RoleEntity;
        import tragsatec.pes.persistence.entity.UserEntity;
        import tragsatec.pes.persistence.repository.UserRepository;

        import java.util.Optional;
        import java.util.Set;
        import java.util.stream.Collectors;

        import static org.assertj.core.api.Assertions.assertThat;

        @DataJpaTest // Configura un entorno de prueba JPA
        // Configura para usar la base de datos real definida en application.properties/yml
        @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
        public class UserRoleTest {

            @Autowired
            private UserRepository userRepository;

            @Test
            void shouldFindRealUserJmarrerosAndRoles() {
                // Arrange: El usuario "jmarreros" debe existir en la base de datos configurada.
                String targetUsername = "jmarreros";

                // Act: Busca el usuario por su nombre de usuario
                Optional<UserEntity> foundUserOptional = userRepository.findByUsername(targetUsername);

                // Assert: Verifica que el usuario fue encontrado
                assertThat(foundUserOptional).isPresent(); // Asegura que se encontró el usuario

                UserEntity foundUser = foundUserOptional.get();
                assertThat(foundUser.getUsername()).isEqualTo(targetUsername); // Verifica el username

                // Imprimir en consola para verificación manual
                System.out.println("Usuario encontrado: " + foundUser.getUsername());

                // Obtener el rol del usuario
                RoleEntity userRole = foundUser.getRole();
                assertThat(userRole).isNotNull(); // Asegura que el usuario tiene un rol asignado

                // Imprimir el rol del usuario
                System.out.println("ID rol del usuario: " + userRole.getId());
                System.out.println("Nombre rol del usuario: " + userRole.getName());

                // Obtener y verificar los permisos del rol
                // La carga EAGER en RoleEntity.permissions asegura que los permisos se cargan junto con el rol
                Set<PermissionEntity> permissions = userRole.getPermissions();

                assertThat(permissions).isNotNull(); // Verifica que la colección de permisos no es nula

                if (!permissions.isEmpty()) {
                    System.out.println("Permisos del rol '" + userRole.getName() + "':");
                    for (PermissionEntity permission : permissions) {
                        // Imprime el detalle del permiso (puedes elegir id, name, description)
                        System.out.println("  - ID: " + permission.getId() + ", Nombre: " + permission.getName() + ", Descripción: " + permission.getDescription());
                    }
                    // Opcional: Aserción para verificar que hay al menos un permiso
                    assertThat(permissions).isNotEmpty();
                    // Opcional: Aserción para verificar un permiso específico por nombre
                    // assertThat(permissions).extracting(PermissionEntity::getName).contains("NOMBRE_PERMISO_ESPERADO");
                } else {
                    System.out.println("El rol '" + userRole.getName() + "' no tiene permisos asociados.");
                    // Opcional: Aserción para verificar que no hay permisos si eso es lo esperado
                    // assertThat(permissions).isEmpty();
                }

                // El código comentado sobre 'roles' (plural) no aplica si UserEntity tiene una relación @ManyToOne con RoleEntity
                // Si UserEntity tuviera @ManyToMany con RoleEntity, ese código sería relevante.
            }

            @Test
            void shouldNotFindNonExistentUser() {
                // Act: Busca un usuario que (probablemente) no existe
                Optional<UserEntity> foundUserOptional = userRepository.findByUsername("nonexistentuser_" + System.currentTimeMillis());

                // Assert: Verifica que el Optional está vacío
                assertThat(foundUserOptional).isNotPresent();
            }
        }