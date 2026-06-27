package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

@Table(name = "tokens", indexes = {
        @Index(name = "idx_device_id_token_type", columnList = "device_id, token_type")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_device_id_token_type", columnNames = {"device_id", "token_type"})
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.tokenId;
    }

    @Id
    @GenerateId
    @Column(name = "token_id")
    Long tokenId;

    @Column(name = "token_value", length = 700)
    String tokenValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    Device device;
}
