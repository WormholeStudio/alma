package studio.wormhole.almaserver.dto;

import lombok.Data;

@Data
public class AuthDTO {


    private String address;
    private String publicKey;
    private String signMessage;

}
