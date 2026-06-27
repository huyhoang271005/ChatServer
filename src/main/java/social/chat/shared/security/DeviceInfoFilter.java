package social.chat.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import social.chat.shared.common.GlobalParamName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class DeviceInfoFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String uaLower = (userAgent != null) ? userAgent.toLowerCase() : "";

        String deviceType = detectDeviceType(request, uaLower);
        request.setAttribute(GlobalParamName.Attribute.DEVICE_TYPE, deviceType);

        String deviceName = getDeviceName(uaLower);
        request.setAttribute(GlobalParamName.Attribute.DEVICE_NAME, deviceName);

        String ipAddress = getIpAddress(request);
        request.setAttribute(GlobalParamName.Attribute.IP_ADDRESS, ipAddress);

        String location = getLocation(request);
        request.setAttribute(GlobalParamName.Attribute.LOCATION, location);

        filterChain.doFilter(request, response);
    }

    private String detectDeviceType(HttpServletRequest request, String uaLower) {

        String cfDeviceType = request.getHeader("CF-Device-Type");
        if (cfDeviceType != null && !cfDeviceType.isBlank()) {
            return cfDeviceType.toUpperCase();
        }

        if (uaLower.contains("android") || uaLower.contains("iphone") || uaLower.contains("ipad")) {
            return "MOBILE";
        }

        // Mặc định cuối cùng
        return "WEB";
    }

    private String getDeviceName(String uaLower) {

        if (uaLower.isBlank()) {
            return "Unknown Browser Unknown Device";
        }

        String browserName = "OtherBrowser";
        String osName = "OtherOS";

        // --- Detect Browser ---
        if ((uaLower.contains("chrome") || uaLower.contains("crios"))
                && !uaLower.contains("edge") && !uaLower.contains("opr") && !uaLower.contains("edg")) {
            browserName = "Chrome";
        } else if (uaLower.contains("firefox")) {
            browserName = "Firefox";
        } else if (uaLower.contains("safari") && !uaLower.contains("chrome") && !uaLower.contains("crios")) {
            browserName = "Safari";
        } else if (uaLower.contains("edge") || uaLower.contains("edg")) {
            browserName = "Edge";
        } else if (uaLower.contains("opr") || uaLower.contains("opera")) {
            browserName = "Opera";
        }

        // --- Detect OS / Device ---
        if (uaLower.contains("android")) {
            osName = "Android";
        } else if (uaLower.contains("iphone")) {
            osName = "iPhone";
        } else if (uaLower.contains("ipad")) {
            osName = "iPad";
        } else if (uaLower.contains("windows")) {
            osName = "Windows";
        } else if (uaLower.contains("macintosh") || uaLower.contains("mac os")) {
            osName = "Macintosh";
        } else if (uaLower.contains("linux")) {
            osName = "Linux";
        }

        return browserName + " " + osName;
    }

    private String getLocation(HttpServletRequest request) {
        List<String> parts = new ArrayList<>();
        getCleanHeader(request, "CF-IPCity").ifPresent(parts::add);
        getCleanHeader(request, "CF-Region").ifPresent(parts::add);
        getCleanHeader(request, "CF-IPCountry").ifPresent(parts::add);
        getCleanHeader(request, "CF-Timezone").ifPresent(parts::add);

        if (parts.isEmpty()) {
            return "Unknown Location";
        }
        return String.join(", ", parts);
    }

    private Optional<String> getCleanHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return Optional.of(decoded.trim());
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("CF-CONNECTING-IP");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("Error parsing IP address", e);
            ipAddress = "unknown";
        }
        return ipAddress;
    }
}