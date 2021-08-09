package studio.wormhole.almaserver.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import studio.wormhole.almaserver.dto.CSVRecord;
import studio.wormhole.almaserver.enums.ClaimState;
import studio.wormhole.almaserver.enums.Token;
import studio.wormhole.almaserver.model.AirdropDetail;
import studio.wormhole.almaserver.model.ApiMerkleTree;
import studio.wormhole.almaserver.model.MerkleRecord;
import studio.wormhole.almaserver.storage.MerkleStorage;
import studio.wormhole.almaserver.utils.MerkleTreeHelper;

@Service
public class AirdropDetailService {

  @Autowired
  private MerkleStorage merkleStorage;

  public AirdropDetail create(long airdropId, List<CSVRecord> records) {

    ApiMerkleTree apiMerkleTree = MerkleTreeHelper
        .merkleTree(airdropId,
            records.stream().filter(r -> r.getAmount() != BigInteger.ZERO).collect(
                Collectors.toList()));

    AirdropDetail airdropDetail = AirdropDetail.builder()
        .root(apiMerkleTree.getRoot())
        .apiMerkleTree(apiMerkleTree)
        .airdropId(airdropId)
        .projectId(1)
        .createAt(new Date())
        .tokenId(Token.STC.getId())
        .build();

    List<MerkleRecord> merkleRecordList = buildRecords(airdropDetail);
    merkleStorage.create(airdropDetail, merkleRecordList);
    return airdropDetail;
  }

  private List<MerkleRecord> buildRecords(AirdropDetail airdropDetail) {

    return airdropDetail.getApiMerkleTree().getProofs().stream().map(r -> MerkleRecord.builder()
        .id(System.nanoTime())
        .airdropId(airdropDetail.getAirdropId())
        .proof(r)
        .userAddress(r.getAddress())
        .claimState(ClaimState.UNCLAIMED)
        .root(airdropDetail.getRoot())
        .build()).collect(Collectors.toList());

  }


  public boolean commit(long airdropId, String root, String txn) {

    return get(airdropId, root)
        .filter(m -> StringUtils.isEmpty(m.getTxn()))
        .map(m -> m.toBuilder().txn(txn).build())
        .map(m -> merkleStorage.commit(airdropId, root, txn))
        .orElse(false);


  }

  public Optional<AirdropDetail> get(long airdropId, String root) {
    return merkleStorage.get(airdropId, root);
  }

  public Optional<MerkleRecord> getMerkleRecord(long id, String address) {

    Optional<AirdropDetail> merkle = get(id, null);
    if (!merkle.isPresent()) {
      return Optional.empty();
    }

    return merkleStorage.getRecord(id, merkle.get().getRoot(), address);
  }

  public boolean claim(long airdropId, String root, String txn, String address) {
    return merkleStorage.claim(airdropId, root, txn, address);
  }
}
