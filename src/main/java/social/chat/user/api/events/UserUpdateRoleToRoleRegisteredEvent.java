package social.chat.user.api.events;

public record UserUpdateRoleToRoleRegisteredEvent(
        Long oldRoleId,
        Long newRoleId
) {
}
