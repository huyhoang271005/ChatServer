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
import org.springframework.modulith.NamedInterface;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import social.chat.shared.security.CustomJwtAuthenticationConverter;

import java.security.Principal;

@NamedInterface
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
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        taskScheduler.initialize();
        registry.enableSimpleBroker(websocketProperties.getBrokerPaths().toArray(new String[0]))
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler);
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

                if (accessor != null) {
                    StompCommand command = accessor.getCommand();

                    // 1. CHỈ GIẢI MÃ TOKEN KHI CONNECT
                    if (StompCommand.CONNECT.equals(command)) {
                        String bearerToken = accessor.getFirstNativeHeader("Authorization");

                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String token = bearerToken.substring(7);
                            try {
                                Jwt jwt = jwtDecoder.decode(token);
                                AbstractAuthenticationToken authentication = customJwtAuthenticationConverter.convert(jwt);

                                if (authentication != null) {
                                    accessor.setUser(authentication);
                                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                                    context.setAuthentication(authentication);
                                    SecurityContextHolder.setContext(context);
                                }
                                return message;
                            } catch (Exception e) {
                                throw new MessageDeliveryException("UNAUTHORIZED:TOKEN_EXPIRED");
                            }
                        }
                        throw new MessageDeliveryException("UNAUTHORIZED:TOKEN_INVALID");
                    }

                    // 2. LỆNH SUBSCRIBE CHỈ CẦN LẤY USER ĐÃ GHIM RA CHECK QUYỀN (Không giải mã lại)
                    if (StompCommand.SUBSCRIBE.equals(command)) {
                        Principal principal = accessor.getUser(); // Tự động có nhờ lệnh CONNECT ghim trước đó
                        if (principal == null) {
                            throw new MessageDeliveryException("UNAUTHORIZED");
                        }

                        String destination = accessor.getDestination();
                        if (destination != null) {
                            String stringSubscribe = destination.split("/")[2];
                            Long userIdSubscribe = Long.parseLong(stringSubscribe.split("\\.")[1]);
                            Long userId = Long.parseLong(principal.getName());

                            if (!userId.equals(userIdSubscribe)) {
                                throw new MessageDeliveryException("FORBIDDEN");
                            }
                            log.info("subscribed to {}", destination);
                        }
                    }
                }
                return message;
            }
        });
    }
}
