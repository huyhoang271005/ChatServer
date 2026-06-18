package social.chat.shared.dto;

import java.util.List;

public record ResponseList<T> (
    List<T> data,
    Boolean hasMore
){}
