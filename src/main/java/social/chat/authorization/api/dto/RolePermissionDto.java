package social.chat.authorization.api.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.modulith.NamedInterface;

import java.time.Instant;
import java.util.List;

@NamedInterface
public record RolePermissionDto (
    Long roleId,
    @NotBlank
    String roleName,
    List<PermissionDto> permissions,
    Instant deletedAt
){}
