package studio.wormhole.almaserver.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@Data
public class ApiMerkleTree {
    private String root;
    private List<ApiMerkleProof> proofs;
}
