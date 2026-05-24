package social.chat.authentication.internal.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.dto.RolePermissionDto;
import social.chat.authentication.internal.service.RoleService;
import social.chat.config.common.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("roles")
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ResponseEntity<Response<?>> createRole(@RequestBody RolePermissionDto rolePermissionDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(rolePermissionDto));
    }

    @PutMapping
    public ResponseEntity<Response<?>> updateRole(@RequestBody RolePermissionDto rolePermissionDto) {
        return ResponseEntity.ok(roleService.updateRole(rolePermissionDto));
    }

    @DeleteMapping("{roleId}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.softDeleteRole(roleId));
    }

    @GetMapping
    public ResponseEntity<Response<?>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRolePermissions());
    }

    @GetMapping("permissions")
    public ResponseEntity<Response<?>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }
}
