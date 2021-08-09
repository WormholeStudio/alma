package studio.wormhole.almaserver.storage.redis;

import com.alibaba.fastjson.JSON;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import studio.wormhole.almaserver.enums.ClaimState;
import studio.wormhole.almaserver.model.AirdropDetail;
import studio.wormhole.almaserver.model.MerkleRecord;
import studio.wormhole.almaserver.storage.MerkleStorage;


@Service
public class MerkleStorageRedisImpl implements MerkleStorage {

  private final RedisTemplate redisTemplate;

  public MerkleStorageRedisImpl(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public String create(AirdropDetail airdropDetail, List<MerkleRecord> records) {
    String key = "airdrop_detail_pair:" + airdropDetail.getAirdropId();
    redisTemplate.opsForValue().set(key, airdropDetail.getRoot());
    String detailKey = "airdrop_detail:" + airdropDetail.getRoot();
    redisTemplate.opsForValue().set(detailKey, JSON.toJSONString(airdropDetail));

    String recordAddressList = "airdrop_record_address_list:" + airdropDetail.getRoot();
    redisTemplate.opsForSet()
        .add(recordAddressList, records.stream().map(s -> s.getUserAddress()).toArray());

    records.forEach(r -> {
      String recordObj = "airdrop_record_obj:" + airdropDetail.getRoot() + "." + r.getUserAddress();
      redisTemplate.opsForValue()
          .set(recordObj, JSON.toJSONString(r));
    });

    return airdropDetail.getRoot();
  }

  @Override
  public Optional<AirdropDetail> get(long airdropId, String root) {
    String key = "airdrop_detail_pair:" + airdropId;
    String redisRoot = String.valueOf(redisTemplate.opsForValue().get(key));
    if (StringUtils.isNotEmpty(root)){
      if (!StringUtils.equals(root, redisRoot)) {
        return Optional.empty();
      }
    }
    String detailKey = "airdrop_detail:" + redisRoot;
    String obj = String.valueOf(redisTemplate.opsForValue().get(detailKey));
    return Optional.ofNullable(JSON.parseObject(obj, AirdropDetail.class));
  }

  @Override
  public boolean commit(long airdropId, String root, String txn) {
    return get(airdropId, root).map(airdropDetail -> {
      if (StringUtils.isNotEmpty(airdropDetail.getTxn())) {
        return false;
      }
      AirdropDetail detail = airdropDetail.toBuilder().txn(txn).build();
      String detailKey = "airdrop_detail:" + detail.getRoot();
      redisTemplate.opsForValue().set(detailKey, JSON.toJSONString(detail));
      return true;
    }).orElse(false);
  }

  @Override
  public Optional<MerkleRecord> getRecord(long airdropId, String root, String address) {
    return (Optional<MerkleRecord>) get(airdropId, root).map(airdropDetail -> {

      String recordObj = "airdrop_record_obj:" + airdropDetail.getRoot() + "." + address;
      Object o = redisTemplate.opsForValue().get(recordObj);
      if (Objects.isNull(o)) {
        return Optional.empty();
      }
      return Optional.ofNullable(JSON.parseObject(String.valueOf(o), MerkleRecord.class));
    }).orElse(Optional.empty());
  }

  @Override
  public boolean claim(long airdropId, String root, String txn, String address) {

    return getRecord(airdropId, root, address).map(record -> {
      if (record.getClaimState() == ClaimState.CLAIMED) {
        return false;
      }
      String recordObj = "airdrop_record_obj:" + record.getRoot() + "." + record.getUserAddress();
      MerkleRecord nr = record.toBuilder().txn(txn).claimAt(new Date())
          .claimState(ClaimState.CLAIMED).build();
      redisTemplate.opsForValue()
          .set(recordObj, JSON.toJSONString(nr));
      return true;
    }).orElse(false);
  }
}
