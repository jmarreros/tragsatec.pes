package tragsatec.pes.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "last_access")
    private LocalDateTime lastLogin;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Boolean locked;

    // Relation to role entity
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role"), nullable = false)
    private RoleEntity role;
}