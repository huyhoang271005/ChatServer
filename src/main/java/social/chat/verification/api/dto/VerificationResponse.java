package social.chat.verification.api.dto;

import social.chat.verification.internal.enums.VerificationStatus;
import social.chat.verification.internal.enums.VerificationType;

import java.time.Instant;

public record VerificationResponse (
    VerificationType verificationType,
    Instant usedAt,
    VerificationStatus verificationStatus,
    Instant createdAt
){}
