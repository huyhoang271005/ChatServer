package social.chat.shared.storage.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.UnprocessableException;
import social.chat.shared.storage.api.CloudStorageDto;
import social.chat.shared.storage.api.CloudStorageImp;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudStorageService implements CloudStorageImp {
    S3Client s3Client;
    CloudStorageProperties cloudStorageProperties;
    S3Presigner s3Presigner;
    Tika tika = new Tika();
    static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private String getUniqueKeyFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            log.error("Can get unique key from url {}", url, e);
            return url;
        }
    }
    @Override
    public void deleteMultipleFile(Collection<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            log.error("Can't delete multiple files from empty file urls");
            return;
        }

        List<String> uniqueKeys = fileUrls
                .stream()
                .filter(Objects::nonNull)
                .map(this::getUniqueKeyFromUrl)
                .toList();
        if(uniqueKeys.isEmpty()) {
            log.error("Can't delete multiple file from empty unique keys");
            return;
        }
        try {
            List<ObjectIdentifier> objectsToDelete = uniqueKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();

            Delete deleteConfig = Delete.builder()
                    .objects(objectsToDelete)
                    .quiet(true)
                    .build();

            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(cloudStorageProperties.getBucketName())
                    .delete(deleteConfig)
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);

            if (response.hasErrors()) {
                response.errors().forEach(err ->
                        log.error(err.toString())
                );
            } else {
                log.info("Deleted files successfully");
            }

        } catch (Exception e) {
            log.error("Delete files failed", e);
        }
    }

    public Response<List<CloudStorageDto>> generateSignatureUrls(List<String> fileNames, UploadType uploadType,
                                                                 Long targetId) {
        if(targetId == null) {
            throw new UnprocessableException("TargetId must not be null");
        }
        return Response.success(
                GlobalMessage.Success.CREATED,
                fileNames.stream()
                        .filter(Objects::nonNull)
                        .map(s -> generateUploadUrl(s, uploadType, targetId))
                        .toList()
        );
    }

    private CloudStorageDto generateUploadUrl(String fileName, UploadType uploadType, Long targetId) {
        String ext = getFileExtension(fileName);
        String strictContentType = tika.detect("file." + ext);

        if ("video/webm".equals(strictContentType) &&
                (ext.equals("webm") || ext.equals("mp3") || ext.equals("ogg") || ext.equals("m4a"))) {

            strictContentType = "audio/webm";
        }

        String datePath = LocalDate.now().format(DATE_FORMATTER);

        String uniqueKey = "%s/%s/%s/%s.%s".formatted(uploadType, targetId, datePath, UUID.randomUUID(), ext);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentDisposition = "inline; filename=\"%s\"; filename*=UTF-8''%s"
                .formatted(encodedFileName, encodedFileName);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(cloudStorageProperties.getBucketName())
                .key(uniqueKey)
                .contentType(strictContentType)
                .contentDisposition(contentDisposition)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();
        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String imageUrl = "%s/%s".formatted(cloudStorageProperties.getBucketUrl(),
                uniqueKey);

        return new CloudStorageDto(fileName, imageUrl, strictContentType,
                contentDisposition, presignedUrl);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "bin";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase().trim();
    }
}
