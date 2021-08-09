package studio.wormhole.almaserver.storage;

import studio.wormhole.almaserver.dto.QueryDTO;
import studio.wormhole.almaserver.model.Airdrop;

import java.util.List;

public interface AirdropStorage  {

    long create(Airdrop airdrop);
    List<Airdrop> list(QueryDTO queryDTO);

    boolean publish(long airdropId);

    boolean delete(long airdropId);
}
