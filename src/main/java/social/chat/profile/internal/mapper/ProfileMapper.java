package social.chat.profile.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.entity.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileDto toProfileDTO(Profile profile);
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    void updateProfile(ProfileDto profileDto, @MappingTarget Profile profile);
}
