package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("roles")
public class RoleController {
    RoleService roleService;

    @PreAuthorize("hasAuthority('CREATE_ROLE_PERMISSION')")
    @PostMapping
    public ResponseEntity<Response<?>> createRole(@RequestBody RolePermissionDto rolePermissionDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(rolePermissionDto));
    }

    @PreAuthorize("hasAuthority('UPDATE_ROLE_PERMISSION')")
    @PutMapping
    public ResponseEntity<Response<?>> updateRole(@RequestBody RolePermissionDto rolePermissionDto) {
        return ResponseEntity.ok(roleService.updateRole(rolePermissionDto));
    }

    @PreAuthorize("hasAuthority('DELETE_ROLE_PERMISSION')")
    @DeleteMapping("{roleId}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.softDeleteRole(roleId));
    }

    @GetMapping
    public ResponseEntity<Response<?>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRolePermissions());
    }

    @PreAuthorize("hasAuthority('GET_PERMISSION')")
    @GetMapping("permissions")
    public ResponseEntity<Response<?>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }

    @PreAuthorize("hasAuthority('RESTORE_ROLE')")
    @PatchMapping("restore/{roleId}")
    public ResponseEntity<Response<?>> restoreRole(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.restoreRole(roleId));
    }
}
