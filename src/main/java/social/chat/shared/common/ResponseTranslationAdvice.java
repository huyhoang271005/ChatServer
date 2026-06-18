package social.chat.shared.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import social.chat.shared.dto.Response;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResponseTranslationAdvice implements ResponseBodyAdvice<Object> {
    MessageSource messageSource;

    public String getString(String messageKey, Object... args) {
        return messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return ResponseEntity.class.isAssignableFrom(returnType.getParameterType())
                || Response.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
                                            @NonNull MediaType selectedContentType,
                                            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            @NonNull ServerHttpRequest request,
                                            @NonNull ServerHttpResponse response) {
        if (body instanceof Response<?>(boolean success, String currentMessage, Object updatedData, Object[] args)) {
            String translatedMessage = currentMessage;

            if (currentMessage != null && !currentMessage.isEmpty()) {
                translatedMessage = getString(currentMessage, args);
            }

            if (updatedData instanceof List<?> list) {
                updatedData = list.stream()
                        .map(o -> o instanceof String key ? getString(key) : o)
                        .toList();
            }

            return new Response<>(
                    success,
                    translatedMessage,
                    updatedData,
                    args
            );
        }
        return body;
    }
}