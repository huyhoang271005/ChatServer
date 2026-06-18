package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.dto.PermissionDto;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.api.events.AuthorizationUpdateRoleToUserRegisteredEvent;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.mapper.RoleMapper;
import social.chat.authorization.internal.repository.PermissionRepository;
import social.chat.authorization.internal.repository.RolePermissionRepository;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RolePermissionRepository rolePermissionRepository;
    RoleCache roleCache;
    RoleMapper roleMapper;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Response<RolePermissionDto> createRole(RolePermissionDto rolePermissionDto) {
        if(roleRepository.existsByRoleName(rolePermissionDto.roleName())) {
            throw new ConflictException(AuthorizationMessage.Role.EXISTS);
        }
        Role role = roleMapper.toRole(rolePermissionDto);
        List<Permission> permissions = permissionRepository.findAllById(rolePermissionDto.permissions()
                .stream()
                .map(PermissionDto::permissionId)
                .toList());
        role.addRolePermission(permissions);
        roleRepository.save(role);
        return Response.success(
                GlobalMessage.Success.CREATED,
                roleMapper.toRolePermissionDto(role)
        );
    }

    @Transactional
    public Response<RolePermissionDto> updateRole(RolePermissionDto rolePermissionDto) {
        Role role = roleRepository.findById(rolePermissionDto.roleId())
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        if(rolePermissionDto.roleName().equals(RoleDefault.ADMIN.name()) ||
            rolePermissionDto.roleName().equals(RoleDefault.USER.name())) {
            throw new ConflictException(AuthorizationMessage.Role.DEFAULT_CAN_UPDATE, role.getRoleName());
        }
        if(role.getDeletedAt() != null) {
            throw new ConflictException(AuthorizationMessage.Role.DELETED);
        }
        List<Permission> permissions = permissionRepository.findAllById(rolePermissionDto.permissions()
                .stream()
                .map(PermissionDto::permissionId)
                .toList());
        rolePermissionRepository.deleteByRole(role);
        role.addRolePermission(permissions);
        roleRepository.save(role);
        roleCache.deleteRolePermissionCache(role.getRoleId());
        return Response.success(
                GlobalMessage.Success.UPDATED,
                roleMapper.toRolePermissionDto(role)
        );
    }

    @Transactional
    public Response<Void> softDeleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        if(role.getDeletedAt() != null) {
            throw new ConflictException(AuthorizationMessage.Role.DELETED);
        }
        if(Arrays.stream(RoleDefault.values())
                .anyMatch(roleDefault -> roleDefault.name().equals(role.getRoleName()))){
            throw new ConflictException(AuthorizationMessage.Role.DEFAULT_CANT_REMOVE, role.getRoleName());
        }
        role.setDeletedAt(Instant.now());
        roleCache.deleteRolePermissionCache(roleId);
        AuthorizationUpdateRoleToUserRegisteredEvent event = new AuthorizationUpdateRoleToUserRegisteredEvent(roleId);
        applicationEventPublisher.publishEvent(event);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<List<PermissionDto>> getAllPermissions(){
        List<Permission> permissions = permissionRepository.findAll();
        return Response.success(
                GlobalMessage.Success.GET,
                permissions.stream()
                        .map(roleMapper::toPermissionDto)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Response<List<RolePermissionDto>> getAllRolePermissions() {
        List<Role> roles = roleRepository.findAllRolesWithPermissions();
        List<RolePermissionDto> rolePermissionDtos = roles.stream()
                .map(roleMapper::toRolePermissionDto)
                .toList();
        return Response.success(
                GlobalMessage.Success.GET,
                rolePermissionDtos
        );
    }

    @Transactional
    public Response<Void> restoreRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        role.setDeletedAt(null);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }

}
