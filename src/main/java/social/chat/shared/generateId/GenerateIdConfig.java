package social.chat.shared.generateId;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateIdConfig {
    @Value("${yitter.worker-id}")
    short workerId;
    @Value("${yitter.worker-id-bit-length}")
    byte workerIdBitLength;
    @Value("${yitter.seq-bit-length}")
    byte seqBitLength;

    @PostConstruct
    public void init() {
        IdGeneratorOptions options = new IdGeneratorOptions(workerId);
        options.WorkerIdBitLength = workerIdBitLength;
        options.SeqBitLength = seqBitLength;
        YitIdHelper.setIdGenerator(options);
    }
}
