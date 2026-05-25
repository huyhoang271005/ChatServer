package social.chat.config.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Response<T> {
    boolean success;
    String message;
    T data;
    Object[] args;

    public Response(boolean success, String message, T data, Object ... args) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.args = args;
    }

    public static <T> Response<T> success(String messageKey, T data, Object ... args) {
        return new Response<>(
                true,
                messageKey,
                data,
                args
        );
    }
}
