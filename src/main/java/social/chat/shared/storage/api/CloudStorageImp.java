package social.chat.shared.storage.api;

import org.springframework.modulith.NamedInterface;

import java.util.Collection;

@NamedInterface
public interface CloudStorageImp {
    void deleteMultipleFile(Collection<String> fileUrls);
}
