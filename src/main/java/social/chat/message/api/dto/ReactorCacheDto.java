package social.chat.message.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReactorCacheDto {
    @JsonIgnore
    Long reactorId;
    Long userId;
    @JsonIgnore
    Long messageId;
    Map<String, Integer> reactionCount;
    @JsonIgnore
    boolean isNew;
}
