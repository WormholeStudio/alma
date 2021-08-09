package studio.wormhole.almaserver.dto;

import lombok.Data;
import studio.wormhole.almaserver.enums.DraftState;

@Data
public class QueryDTO {

    String address;
    FilterEnum filter;
}
