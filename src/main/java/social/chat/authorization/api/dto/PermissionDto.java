package social.chat.authorization.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionDto (
    Long permissionId,
    Long rolePermissionId,
    @NotBlank
    String permissionName
){}
