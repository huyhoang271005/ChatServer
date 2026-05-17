package social.chat.config.cronJob;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface DeleteJob {
    void flush();
}
