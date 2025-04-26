package tragsatec.pes.persistence.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditUserListener;
import tragsatec.pes.persistence.audit.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditUserListener.class})
@Table(name = "user", uniqueConstraints = @UniqueConstraint(name = "unique_username", columnNames = "username"))
@Setter
@Getter
@NoArgsConstructor
public class UserEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 30, unique = true)
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