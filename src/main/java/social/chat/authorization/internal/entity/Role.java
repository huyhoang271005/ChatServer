package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;
import java.util.ArrayList;
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

    @Nationalized
    @Column(name = "role_name", length = 125)
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
