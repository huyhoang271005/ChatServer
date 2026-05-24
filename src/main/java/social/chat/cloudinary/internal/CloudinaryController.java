package social.chat.cloudinary.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import social.chat.config.common.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("images")
public class CloudinaryController {
    CloudinaryService cloudinaryService;

    @GetMapping("upload-signature/{folder}")
    public ResponseEntity<Response<?>> uploadSignature(@PathVariable String folder) {
        return ResponseEntity.ok(cloudinaryService.generateUploadSignature(folder));
    }
}
