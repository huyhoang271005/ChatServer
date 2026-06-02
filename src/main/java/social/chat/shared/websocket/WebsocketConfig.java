package social.chat.shared.websocket;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import social.chat.shared.security.CustomJwtAuthenticationConverter;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    WebsocketProperties websocketProperties;
    JwtDecoder jwtDecoder;
    CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint(websocketProperties.getEndpoint())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(websocketProperties.getBrokerPaths().toArray(new String[0]));
        registry.setApplicationDestinationPrefixes(websocketProperties
                .getAppPrefixes().toArray(new String[0]));
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public @NonNull Message<?> preSend(@NonNull Message<?> message,
                                               @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                        StompCommand.SEND.equals(accessor.getCommand()) ||
                        StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
                    String bearerToken = accessor.getFirstNativeHeader("Authorization");

                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        log.info("Bearer Token: {}", bearerToken);
                        String token = bearerToken.substring(7);
                        log.info("Token: {}", token);
                        try {
                            Jwt jwt = jwtDecoder.decode(token);
                            AbstractAuthenticationToken authentication = customJwtAuthenticationConverter.convert(jwt);
                            if (authentication != null) {
                                accessor.setUser(authentication);
                            }
                            if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                                Long userId = Long.parseLong(jwt.getSubject());
                            }
                            return message;
                        } catch (Exception e) {
                            log.error(e.getMessage());
                            throw new MessageDeliveryException("UNAUTHORIZED:TOKEN_EXPIRED");
                        }
                    }
                    throw new MessageDeliveryException("UNAUTHORIZED:TOKEN_INVALID");
                }
                return message;
            }
        });
    }
}
