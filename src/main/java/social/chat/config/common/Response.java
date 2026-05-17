package social.chat.config.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Response<T> {
    boolean success;
    String message;
    T data;

    public static <T> Response<T> success(String messageKey, T data) {
        return new Response<T>(
                true,
                messageKey,
                data
        );
    }
}
