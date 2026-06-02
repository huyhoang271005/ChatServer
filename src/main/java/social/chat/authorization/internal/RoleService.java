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
import social.chat.authorization.internal.entity.RolePermission;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.mapper.PermissionMapper;
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
    PermissionMapper permissionMapper;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Response<RolePermissionDto> createRole(RolePermissionDto rolePermissionDto) {
        if(roleRepository.existsByRoleName(rolePermissionDto.getRoleName())) {
            throw new ConflictException(AuthorizationMessage.Role.EXISTS);
        }
        Role role = roleMapper.toRole(rolePermissionDto);
        List<Permission> permissions = permissionRepository.findAllById(rolePermissionDto.getPermissions()
                .stream()
                .map(permissionDto -> Long.valueOf(permissionDto.getPermissionId()))
                .toList());
        List<RolePermission> rolePermissions = permissions.stream()
                        .map(permission -> RolePermission.builder()
                                .role(role)
                                .permission(permission)
                                .build())
                        .toList();
        role.setRolePermissions(rolePermissions);
       roleRepository.save(role);

        List<PermissionDto> permissionDtos = rolePermissions.stream()
                .map(rolePermission -> {
                    PermissionDto permissionDto = permissionMapper.toPermissionDto(rolePermission.getPermission());
                    permissionDto.setRolePermissionId(rolePermission.getRolePermissionId());
                    return permissionDto;
                })
                .toList();
        RolePermissionDto rolePermissionDto1 = roleMapper.toRolePermissionDto(role);
        rolePermissionDto1.setPermissions(permissionDtos);
        return Response.success(
                GlobalMessage.Success.CREATED,
                rolePermissionDto1
        );
    }

    @Transactional
    public Response<RolePermissionDto> updateRole(RolePermissionDto rolePermissionDto) {
        Role role = roleRepository.findById(Long.parseLong(rolePermissionDto.getRoleId()))
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        if(rolePermissionDto.getRoleName().equals(RoleDefault.ADMIN.name())) {
            throw new ConflictException(AuthorizationMessage.Role.DEFAULT_CAN_UPDATE, role.getRoleName());
        }
        if(role.getDeletedAt() != null) {
            throw new ConflictException(AuthorizationMessage.Role.DELETED);
        }
        List<Permission> permissions = permissionRepository.findAllById(rolePermissionDto.getPermissions()
                .stream()
                .map(permissionDto -> Long.valueOf(permissionDto.getPermissionId()))
                .toList());
        rolePermissionRepository.deleteAll(role.getRolePermissions());
        List<RolePermission> rolePermissions = permissions.stream()
                .map(permission -> RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build())
                .toList();
        rolePermissionRepository.saveAll(rolePermissions);
        roleCache.deleteRolePermissionCache(role.getRoleId());
        RolePermissionDto rolePermissionDto1 = roleMapper.toRolePermissionDto(role);
        rolePermissionDto1.setPermissions(rolePermissions.stream()
                .map(rolePermission -> {
                    PermissionDto permissionDto = permissionMapper.toPermissionDto(rolePermission.getPermission());
                    permissionDto.setRolePermissionId(rolePermission.getRolePermissionId());
                    return permissionDto;
                })
                .toList());
        return Response.success(
                GlobalMessage.Success.UPDATED,
                rolePermissionDto1
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
                        .map(permissionMapper::toPermissionDto)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public Response<List<RolePermissionDto>> getAllRolePermissions() {
        List<Role> roles = roleRepository.findAllRolesWithPermissions();
        List<RolePermissionDto> rolePermissionDtos = roles.stream()
                .map(role -> {
                    RolePermissionDto rolePermissionDto = roleMapper.toRolePermissionDto(role);
                    rolePermissionDto.setPermissions(role.getRolePermissions()
                            .stream()
                            .map(rolePermission -> {
                                PermissionDto permissionDto = permissionMapper.toPermissionDto(rolePermission.getPermission());
                                permissionDto.setRolePermissionId(rolePermission.getRolePermissionId());
                                return permissionDto;
                            })
                            .toList());
                    return rolePermissionDto;
                })
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
