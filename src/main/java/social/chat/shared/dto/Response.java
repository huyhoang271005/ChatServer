package social.chat.shared.dto;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public record Response<T> (
    boolean success,
    String message,
    T data,
    Object... args
){

    public static <T> Response<T> success(String messageKey, T data, Object ... args) {
        return new Response<>(
                true,
                messageKey,
                data,
                args
        );
    }
}
