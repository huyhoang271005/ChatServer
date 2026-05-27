package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.shared.generateId.GenerateId;

@Table(name = "tokens", indexes = {
        @Index(name = "idx_token_session", columnList = "session_id")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token {
    @Id
    @GenerateId
    @Column(name = "token_id")
    Long tokenId;

    @Column(name = "token_value",columnDefinition = "VARCHAR(MAX)")
    String tokenValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    Session session;
}
