package tragsatec.pes.persistence.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean locked;

    // Relation to role entity
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_role"), nullable = false)
    private RoleEntity role;

    @JsonProperty("role")
    public void setRoleById(Integer roleId) {
        this.role = new RoleEntity();
        this.role.setId(roleId);
    }
}