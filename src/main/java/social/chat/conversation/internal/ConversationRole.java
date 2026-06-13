package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ConversationRole {
    CREATOR(List.of(
            ConversationPermission.ADD_MEMBER,
            ConversationPermission.REMOVE_MEMBER
    )),
    ADMIN(List.of(

    )),
    MEMBER(List.of());

    final List<ConversationPermission> permissions;

    public boolean roleCheck(ConversationPermission permission) {
        return this.permissions.contains(permission);
    }

}
