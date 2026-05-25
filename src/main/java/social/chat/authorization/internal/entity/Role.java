package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.config.generateId.GenerateId;

import java.time.Instant;
import java.util.List;

@Table(name = "roles", indexes = {
        @Index(name = "idx_user_role", columnList = "role_id")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @GenerateId
    @Column(name = "role_id")
    Long roleId;

    @Column(name = "role_name", columnDefinition = "NVARCHAR(125)")
    String roleName;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RolePermission> rolePermissions;
}
