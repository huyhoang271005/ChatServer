package social.chat.shared.storage.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.shared.dto.Response;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("uploads")
public class CloudStorageController {
    CloudStorageService cloudStorageService;

    @PostMapping("signatures")
    public ResponseEntity<Response<?>> uploadSignature(@RequestBody List<String> fileNames) {
        return ResponseEntity.ok(cloudStorageService.generateSignatureUrls(fileNames));
    }
}
