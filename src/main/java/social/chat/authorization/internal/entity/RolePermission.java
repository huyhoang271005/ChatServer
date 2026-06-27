package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

@Table(name = "roles_permissions", indexes = {
        @Index(name = "idx_role_permission", columnList = "role_id, permission_id"),
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_role_permission", columnNames = {"role_id", "permission_id"})
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermission extends BaseEntity {
    @Id
    @GenerateId
    @Column(name = "role_permission_id")
    Long rolePermissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    Permission permission;

    @Override
    public @Nullable Long getId() {
        return this.rolePermissionId;
    }
}
