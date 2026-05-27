package social.chat.cloudinary.internal;

import com.cloudinary.Cloudinary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    CloudinaryProperties cloudinaryProperties;
    Cloudinary cloudinary;

    public Response<Map<String, Object>> generateUploadSignature(String folderCloudinary) {

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
