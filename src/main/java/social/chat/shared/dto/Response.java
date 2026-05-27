package social.chat.shared.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;

@NamedInterface
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
