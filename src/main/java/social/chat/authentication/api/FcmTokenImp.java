package social.chat.authentication.api;

import java.util.List;

public interface FcmTokenImp {
    List<String> getFcmTokenByUserIds(List<Long> userIds);
    List<String> putFcmTokenByUserId(Long userId, List<String> fcmTokens);
}
