package social.chat.profile.internal.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.service.ProfileService;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("profiles")
public class ProfileController {
    ProfileService profileService;

    @PostMapping("{userId}")
    public ResponseEntity<?> createProfile(@PathVariable Long userId,
                                           @Valid @RequestBody ProfileDto profileDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createProfile(userId, profileDto.getFullName()));
    }

    @PutMapping("{userId}")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileDto profileDto,
                                           @PathVariable Long userId) {
        return ResponseEntity.ok(profileService.updateProfile(userId, profileDto));
    }
}
