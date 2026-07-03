package social.chat.shared.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import social.chat.message.api.dto.MessageDto;
import social.chat.shared.common.ResponseTranslationAdvice;
import social.chat.shared.websocket.DataDto;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketExceptionAdvice {
    WebsocketService websocketService;
    ObjectMapper objectMapper;
    ResponseTranslationAdvice responseTranslationAdvice;

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(MethodArgumentNotValidException ex,
                                          Principal principal, Message<?> message) {
        String clientMsgId = extractClientMsgId(message);
        String errorMessage = "Unknown validation error";

        if (ex.getBindingResult() != null) {
            errorMessage = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fieldError -> responseTranslationAdvice.getString(fieldError.getDefaultMessage()))
                    .collect(Collectors.joining(", "));
        }

        log.error(errorMessage);
        sendErrorToUser(principal, clientMsgId, errorMessage);
    }

    @MessageExceptionHandler(Exception.class)
    public void handleGeneralException(Exception ex, Principal principal, Message<?> message) {
        String clientMsgId = extractClientMsgId(message);

        String errorMessage = responseTranslationAdvice.getString(ex.getMessage());
        if (errorMessage == null || errorMessage.equals(ex.getMessage())) {
            errorMessage = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        }

        log.error(ex.toString());
        sendErrorToUser(principal, clientMsgId, errorMessage);
    }


    private String extractClientMsgId(Message<?> message) {
        if (message == null) return null;
        try {
            Object payload = message.getPayload();
            String jsonContent = payload instanceof byte[] ?
                    new String((byte[]) payload, StandardCharsets.UTF_8) :
                    payload.toString();

            DataDto dataDto = objectMapper.readValue(jsonContent, DataDto.class);
            return dataDto.clientMsgId();
        } catch (Exception e) {
            log.warn("Error when parse error {}", e.getMessage());
            return null;
        }
    }

    private void sendErrorToUser(Principal principal, String clientMsgId, String errorMessage) {
        if (principal != null) {
            try {
                websocketService.sendMessageToUser(
                        null,
                        Long.parseLong(principal.getName()),
                        clientMsgId,
                        WebsocketEventType.ERROR,
                        MessageDto.builder().text(errorMessage).build()
                );
            } catch (Exception e) {
                log.error("Error when send payload to user {}", e.getMessage());
            }
        }
    }
}