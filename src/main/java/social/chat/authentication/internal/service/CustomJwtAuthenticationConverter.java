package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.internal.enums.AccountStatus;
import social.chat.authentication.api.dto.PermissionDto;
import social.chat.authentication.api.dto.RolePermissionDto;
import social.chat.authentication.api.dto.UserCacheDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.cache.RoleCache;
import social.chat.authentication.internal.cache.UserCache;
import social.chat.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    RoleCache roleCache;
    UserCache userCache;

    @Transactional(readOnly = true)
    @Override
    public @Nullable AbstractAuthenticationToken convert(@NonNull Jwt source) {
        Long userId = Long.valueOf(source.getSubject());
        UserCacheDto userCacheDto = userCache.getUserCache(userId);
        RolePermissionDto rolePermissionDto = roleCache.getRolePermissionsCache(userCacheDto.getRoleId());
        if(userCacheDto.getAccountStatus() != AccountStatus.ACTIVE) {
            switch (userCacheDto.getAccountStatus()) {
                case AccountStatus.BLOCKED ->
                        throw new ConflictException(AuthenticationMessage.Account.BLOCKED);
                case AccountStatus.INACTIVE ->
                        throw new ConflictException(AuthenticationMessage.Account.INACTIVE);
                default ->
                        throw new ConflictException(AuthenticationMessage.Account.INVALID);
            }
        }
        List<GrantedAuthority> authorities = rolePermissionDto.getPermissions()
                .stream()
                .map(PermissionDto::getPermissionName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new JwtAuthenticationToken(source, authorities);
    }
}
