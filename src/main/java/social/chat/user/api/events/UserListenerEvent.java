package social.chat.user.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.user.api.UserImp;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserListenerEvent {
    UserImp userImp;

    @ApplicationModuleListener
    public void unbannedAccount(UserUnbannedAccountRegisteredEvent event){
        userImp.unbannedAccountCronjob();
    }

    @ApplicationModuleListener
    public void hardDeleteUser(UserHardDeleteUserRegisteredEvent event){
        userImp.hardDeleteUserCronjob();
    }
}
