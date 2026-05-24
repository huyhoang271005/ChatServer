package social.chat.authentication.api.dto;

public final class AuthRegexValidation {
    public final static String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_+])\\S{8,}$";
}
