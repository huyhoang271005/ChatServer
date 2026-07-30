package social.chat.shared.storage.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<Response<?>> uploadSignature(@RequestBody List<String> fileNames,
                                                       @RequestParam UploadType uploadType,
                                                       @RequestParam(required = false) Long targetId,
                                                       @AuthenticationPrincipal Jwt jwt) {
        if(uploadType == UploadType.USER){
            targetId = Long.parseLong(jwt.getSubject());
        }
        return ResponseEntity.ok(cloudStorageService
                .generateSignatureUrls(fileNames, uploadType, targetId));
    }
}
