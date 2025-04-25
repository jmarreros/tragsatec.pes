package tragsatec.pes.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user", uniqueConstraints = @UniqueConstraint(name = "unique_username", columnNames = "username"))
@Data
public class UserEntity {
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

    @Column(name="created_at")
    @CreatedDate
    @JsonIgnore
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    @LastModifiedDate
    @JsonIgnore
    private LocalDateTime updatedAt;

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