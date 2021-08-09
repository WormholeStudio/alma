package studio.wormhole.almaserver.model;

import java.math.BigInteger;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@Data
public class ApiMerkleProof {
  private String address;
  private long index;
  private BigInteger amount;
  private List<String> proof;
}
