package social.chat.config.common;

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

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResponseTranslationAdvice implements ResponseBodyAdvice<Object> {
    MessageSource messageSource;

    public String getString(String messageKey){
        String message = messageKey;
        try{
            message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        } catch (Exception ignored){
            log.error("Not found message source {}", messageKey);
        }
        return message;
    }
    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
                                            @NonNull MediaType selectedContentType,
                                            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            @NonNull ServerHttpRequest request,
                                            @NonNull ServerHttpResponse response) {
        if (body instanceof Response<?> apiResponse) {
            String currentMessage = apiResponse.getMessage();

            if (currentMessage != null && !currentMessage.isEmpty()) {
                String translated = getString(currentMessage);
                apiResponse.setMessage(translated);
                if(apiResponse.getData() != null && apiResponse.getData() instanceof List<?> list){
                    List<Object> updateList = list.stream()
                            .map(o -> o instanceof String key ? getString(key) : o)
                            .toList();
                    ((Response) apiResponse).setData(updateList);
                }
            }
        }
        return body;
    }
}
