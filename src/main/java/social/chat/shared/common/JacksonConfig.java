package social.chat.shared.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                ObjectMapper objectMapper = jacksonConverter.getObjectMapper();
                SimpleModule module = new SimpleModule();

                // Cấu hình: Cứ gặp kiểu Long hoặc long, tự động biến thành String khi Serialize JSON
                module.addSerializer(Long.class, ToStringSerializer.instance);
                module.addSerializer(Long.TYPE, ToStringSerializer.instance);

                objectMapper.registerModule(module);
            }
        }
    }
}
