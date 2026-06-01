package social.chat.shared.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseList<T> {
    List<T> data;
    Boolean hasMore;
}
