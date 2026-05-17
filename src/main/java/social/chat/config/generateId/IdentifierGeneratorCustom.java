package social.chat.config.generateId;

import com.github.yitter.idgen.YitIdHelper;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class IdentifierGeneratorCustom implements IdentifierGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return YitIdHelper.nextId();
    }
}
