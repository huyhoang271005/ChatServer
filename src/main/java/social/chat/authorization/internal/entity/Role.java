package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "role_name")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.roleId;
    }

    @Id
    @GenerateId
    @Column(name = "role_id")
    Long roleId;

    @Nationalized
    @Column(name = "role_name", unique = true, length = 125)
    String roleName;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RolePermission> rolePermissions;

    public void addRolePermission(List<Permission> permissions) {
        if(this.rolePermissions == null) {
            this.rolePermissions = new ArrayList<>();
        }
        for (Permission permission : permissions) {
            RolePermission rolePermission = RolePermission.builder()
                    .permission(permission)
                    .role(this)
                    .build();
            this.rolePermissions.add(rolePermission);
        }
    }
}
