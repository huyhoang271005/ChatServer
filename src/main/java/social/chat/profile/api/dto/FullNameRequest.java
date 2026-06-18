package social.chat.profile.api.dto;

import jakarta.validation.constraints.Pattern;
import social.chat.profile.internal.ProfileMessage;

public record FullNameRequest (
    @Pattern(regexp = ProfileRegexValidation.FULL_NAME, message = ProfileMessage.Validation.FULL_NAME_INVALID)
    String fullName
){}
