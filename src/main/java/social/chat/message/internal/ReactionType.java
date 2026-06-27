package social.chat.message.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ReactionType {
    LIKE("👍"),
    HEART("❤️"),
    HAHA("😆"),
    WOW("😮"),
    SAD("😢"),
    ANGRY("😡"),
    CARE("🥰"),
    CLAP("👏"),
    FIRE("🔥");

    String emoji;
}
