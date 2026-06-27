package social.chat.shared.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnauthorizedException extends AuthenticationException {
    Object[] args;

    public UnauthorizedException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
