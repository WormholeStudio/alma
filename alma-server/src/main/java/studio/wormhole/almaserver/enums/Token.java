package studio.wormhole.almaserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Token {

    NONE(0,"","NONE","NONE"),
    STC(1,"0x00000000000000000000000000000001","STC","STC"),
    ALM(2,"0xf8af03dd08de49d81e4efd9e24c039cc","ALM","ALM");
    private long id;
    private String contractAddress;
    private String module;
    private String struct;

    public static Optional<Token> valueOfId(long tokenId) {

        return Arrays.stream(Token.values()).filter(s->s.getId()==tokenId).findAny();
    }

    public String identifier(){
        return contractAddress+"::"+module+"::"+struct;
    }

}
