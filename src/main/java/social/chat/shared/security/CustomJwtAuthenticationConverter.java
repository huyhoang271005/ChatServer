package social.chat.shared.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.authorization.api.dto.PermissionDto;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.user.api.UserImp;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    AuthorizationImp authorizationImp;
    UserImp userImp;

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Long userId = Long.valueOf(source.getSubject());
        Long roleId = userImp.getRoleIdAndCheckAccountStatus(userId);
        RolePermissionDto rolePermissionDto = authorizationImp.getRolePermissionByRoleId(roleId);
        List<GrantedAuthority> authorities = rolePermissionDto.getPermissions()
                .stream()
                .map(PermissionDto::getPermissionName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new JwtAuthenticationToken(source, authorities);
    }
}
