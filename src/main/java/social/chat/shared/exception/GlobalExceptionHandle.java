package social.chat.shared.exception;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.modulith.NamedInterface;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.common.ResponseTranslationAdvice;

import java.util.List;

@NamedInterface
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalExceptionHandle {
    ResponseTranslationAdvice responseTranslationAdvice;

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response<List<String>>> handleConflict(ConflictException conflictException){
        String message = conflictException.getMessage();
        log.error(responseTranslationAdvice.getString(message, conflictException.getArgs()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new Response<>(
                        false,
                        message,
                        List.of(message),
                        conflictException.getArgs()
                )
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response<List<String>>> handleEntityNotFound(EntityNotFoundException entityNotFoundException){
        String message = entityNotFoundException.getMessage();
        log.error(responseTranslationAdvice.getString(message, entityNotFoundException.getArgs()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new Response<>(
                        false,
                        message,
                        List.of(message),
                        entityNotFoundException.getArgs()
                )
        );
    }

    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<Response<List<String>>> handleUnprocessable(UnprocessableException unprocessableException){
        String message = unprocessableException.getMessage();
        log.error(responseTranslationAdvice.getString(message, unprocessableException.getArgs()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new Response<>(
                        false,
                        message,
                        List.of(message),
                        unprocessableException.getArgs()
                )
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response<List<String>>> handleUnauthorized(UnauthorizedException unauthorizedException){
        String message = unauthorizedException.getMessage();
        log.error(responseTranslationAdvice.getString(message, unauthorizedException.getArgs()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new Response<>(
                        false,
                        message,
                        List.of(message),
                        unauthorizedException.getArgs()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<List<String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValid){
        List<String> errorsString = methodArgumentNotValid.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        log.error(String.join(", ", errorsString.stream()
                .map(responseTranslationAdvice::getString)
                .toList()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                new Response<>(
                        false,
                        GlobalMessage.Error.DATA_INVALID,
                        errorsString
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<List<String>>> handleException(Exception exception){
        String message = exception.getMessage();
        log.error(responseTranslationAdvice.getString(message));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new Response<>(
                        false,
                        GlobalMessage.Error.INTERNAL,
                        List.of(message)
                )
        );
    }
}
