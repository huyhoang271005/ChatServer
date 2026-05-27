package social.chat.shared.cronJob;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface DeleteJob {
    void flush();
}
