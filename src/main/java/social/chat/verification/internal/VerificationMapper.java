package social.chat.verification.internal;

import org.mapstruct.Mapper;
import social.chat.verification.api.dto.VerificationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VerificationMapper {
    List<VerificationResponse> toVerificationResponseList(List<Verification> verifications);
}
