package social.chat.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.config.common.ResponseTranslationAdvice;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    ObjectMapper objectMapper;
    ResponseTranslationAdvice responseTranslationAdvice;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String json = objectMapper.writeValueAsString(
                new Response<>(
                        false,
                        responseTranslationAdvice.getString(GlobalMessage.Error.TOKEN_INVALID),
                        authException.getMessage()
                )
        );
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(json);
    }
}
