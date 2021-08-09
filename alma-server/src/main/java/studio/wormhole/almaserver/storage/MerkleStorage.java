package studio.wormhole.almaserver.storage;

import studio.wormhole.almaserver.model.AirdropDetail;
import studio.wormhole.almaserver.model.MerkleRecord;

import java.util.List;
import java.util.Optional;

public interface MerkleStorage {

    String create(AirdropDetail airdropDetail, List<MerkleRecord> records);

    Optional<AirdropDetail> get(long airdropId, String root);

    boolean commit(long airdropId, String root, String txn);

    Optional<MerkleRecord> getRecord(long airdropId, String root, String address);

    boolean claim(long airdropId, String root, String txn, String address);
}
