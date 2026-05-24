package social.chat.cloudinary.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface CloudinaryImp {
    boolean deleteImage(String publicId);
}
