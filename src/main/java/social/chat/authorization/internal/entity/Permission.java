package social.chat.authorization.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.config.generateId.GenerateId;

import java.util.List;

@Table(name = "permissions")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    @GenerateId
    @Column(name = "permission_id")
    Long permissionId;

    @Column(name = "permission_name", columnDefinition = "VARCHAR(125)")
    String permissionName;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RolePermission> rolePermissions;
}
