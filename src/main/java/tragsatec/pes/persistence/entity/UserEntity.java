package tragsatec.pes.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tragsatec.pes.persistence.audit.AuditUserListener;
import tragsatec.pes.persistence.audit.AuditableEntity;
import tragsatec.pes.persistence.enums.UserRole;

import java.time.LocalDateTime;

@Entity
@EntityListeners({AuditingEntityListener.class, AuditUserListener.class})
@Table(name = "[user]", uniqueConstraints = @UniqueConstraint(name = "unique_username", columnNames = "username"))
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

    @Column(nullable = false)
    private Boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
}