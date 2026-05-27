package social.chat.shared.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnprocessableException extends RuntimeException {
    Object[] args;

    public UnprocessableException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
