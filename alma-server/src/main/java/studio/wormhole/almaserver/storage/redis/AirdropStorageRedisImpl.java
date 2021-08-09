package studio.wormhole.almaserver.storage.redis;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import studio.wormhole.almaserver.dto.FilterEnum;
import studio.wormhole.almaserver.dto.QueryDTO;
import studio.wormhole.almaserver.enums.DraftState;
import studio.wormhole.almaserver.model.Airdrop;
import studio.wormhole.almaserver.storage.AirdropStorage;

@Slf4j
@Service
public class AirdropStorageRedisImpl implements
    AirdropStorage {


  private final RedisTemplate redisTemplate;

  public AirdropStorageRedisImpl(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public long create(Airdrop airdrop) {
    String key = "airdrop:" + airdrop.getId();
    redisTemplate.opsForValue().set(key, JSON.toJSONString(airdrop));
    String allIdSet = "airdrop_all";
    long rst = redisTemplate.opsForSet().add(allIdSet, airdrop.getId());
    log.info("add rst:{}", rst);
    return airdrop.getId();
  }


  private List<Airdrop> all() {
    String allIdSet = "airdrop_all";

    Set<Long> rst = redisTemplate.opsForSet().members(allIdSet);
    if (org.apache.commons.collections.CollectionUtils.isEmpty(rst)) {
      return Lists.newArrayList();
    }

    Set<String> keys = rst.stream().map(s -> "airdrop:" + String.valueOf(s))
        .collect(Collectors.toSet());
    List<String> list = redisTemplate.opsForValue().multiGet(keys);
    if (CollectionUtils.isEmpty(list)) {
      return Lists.newArrayList();
    }
    return list.stream().map(s -> JSON.parseObject(s, Airdrop.class)).collect(Collectors.toList());
  }

  private Optional<Airdrop> get(long airdropId) {
    String key = "airdrop:" + airdropId;
    Object o = redisTemplate.opsForValue().get(key);
    if (Objects.isNull(o)) {
      return Optional.empty();
    }
    return Optional.ofNullable(JSON.parseObject(String.valueOf(o), Airdrop.class));
  }

  @Override
  public List<Airdrop> list(QueryDTO queryDTO) {
    List<Airdrop> all = all();
    if (CollectionUtils.isEmpty(all)) {
      return Lists.newArrayList();
    }
    if (queryDTO.getFilter() == FilterEnum.MINE && !Strings.isNullOrEmpty(queryDTO.getAddress())) {
      return all.stream()
          .filter(s -> s.getOwnerAddress().equals(queryDTO.getAddress()))
          .collect(Collectors.toList());
    }
    return all;
  }

  @Override
  public boolean publish(long airdropId) {

    return get(airdropId).map(o -> {

      Airdrop nb = o.toBuilder().draftState(DraftState.PUBLISHED).build();
      create(nb);
      return true;
    }).orElse(false);

  }

  @Override
  public boolean delete(long airdropId) {
    String key = "airdrop:" + airdropId;
    redisTemplate.delete(key);
    String allIdSet = "airdrop_all";
    redisTemplate.opsForSet().remove(allIdSet, airdropId);
    return true;
  }
}
