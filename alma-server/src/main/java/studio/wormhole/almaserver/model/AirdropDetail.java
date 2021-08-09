package studio.wormhole.almaserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)

@NoArgsConstructor
@Data
public class AirdropDetail extends TokenBase {

    private long airdropId;
    private long projectId;
    private String txn;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createAt;
    private ApiMerkleTree apiMerkleTree;
    private String root;

}
