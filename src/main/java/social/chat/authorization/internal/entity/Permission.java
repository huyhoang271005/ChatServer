package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.util.List;

@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "permission_name")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends BaseEntity {
    @Id
    @GenerateId
    @Column(name = "permission_id")
    Long permissionId;

    @Column(name = "permission_name", length = 125)
    String permissionName;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RolePermission> rolePermissions;

    @Override
    public @Nullable Long getId() {
        return this.permissionId;
    }
}
