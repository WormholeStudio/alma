package studio.wormhole.almaserver.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import studio.wormhole.almaserver.enums.Token;

@SuperBuilder(toBuilder = true)

@NoArgsConstructor
@Data
public class TokenBase {

    private long tokenId;


    @JsonProperty("token_identifier")
    public String tokenJSON() {
        return Token.valueOfId(tokenId).orElse(Token.NONE).identifier();
    }

}
