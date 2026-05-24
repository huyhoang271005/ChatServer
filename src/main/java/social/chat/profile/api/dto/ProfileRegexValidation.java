package social.chat.profile.api.dto;

public final class ProfileRegexValidation {
    public final static String USERNAME = "^[a-zA-Z0-9@._]{3,50}$";
    public final static String FULL_NAME = "^(?=.*\\p{Lu})(?=.*\\s)[\\p{L}\\s'’-]{3,50}$";
}
