package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.config.generateId.GenerateId;

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

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RolePermission> rolePermissions;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    List<User> users;
}
