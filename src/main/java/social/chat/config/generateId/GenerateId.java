package social.chat.config.generateId;

import org.hibernate.annotations.IdGeneratorType;
import org.springframework.modulith.NamedInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NamedInterface
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@IdGeneratorType(IdentifierGeneratorCustom.class)
public @interface GenerateId {
}
