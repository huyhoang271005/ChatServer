package social.chat.profile.internal.mapper;

import org.mapstruct.*;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.entity.Profile;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProfileMapper {
    @Mapping(target = "avatarUrl", source = "avatarUrl", qualifiedByName = "checkAvatarUrl")
    ProfileDto toProfileDTO(Profile profile, @Context String userUnknowUrl);
    @Named("checkAvatarUrl")
    default String checkAvatarUrl(String avatarUrl, @Context String userUnknowUrl) {
        return avatarUrl == null ? userUnknowUrl : avatarUrl;
    }
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    void updateProfile(ProfileDto profileDto, @MappingTarget Profile profile);
}
