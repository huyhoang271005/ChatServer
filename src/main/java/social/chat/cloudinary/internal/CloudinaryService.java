package social.chat.cloudinary.internal;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import social.chat.cloudinary.api.CloudinaryImp;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService implements CloudinaryImp {
    CloudinaryProperties cloudinaryProperties;

    @Override
    public boolean deleteImage(String publicId) {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryProperties.getCloudName(),
                "api_key", cloudinaryProperties.getApiKey(),
                "api_secret", cloudinaryProperties.getApiSecret()
        ));
        boolean response;
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            response = "ok".equals(result.get("result"));
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    public Response<Map<String, Object>> generateUploadSignature(String folderCloudinary) {
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryProperties.getCloudName(),
                "api_key", cloudinaryProperties.getApiKey(),
                "api_secret", cloudinaryProperties.getApiSecret()
        ));

        long timestamp = System.currentTimeMillis() / 1000L;

        String folder = "app_chat/" + folderCloudinary;
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        params.put("upload_preset", "app_chat_preset");

        try {
            String signature = cloudinary.apiSignRequest(params, cloudinaryProperties.getApiSecret(), 1);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("signature", signature);
            responseData.put("timestamp", timestamp);
            responseData.put("cloudName", cloudinaryProperties.getCloudName());
            responseData.put("apiKey", cloudinaryProperties.getApiKey());
            responseData.put("folder", folder);
            responseData.put("uploadPreset", "app_chat_preset");
            responseData.put("signatureVersion", "v1");

            return Response.success(
                    GlobalMessage.Success.CREATED,
                    responseData
            );
        } catch (Exception e) {
            throw new RuntimeException("Error create cloudinary signature", e);
        }
    }
}
