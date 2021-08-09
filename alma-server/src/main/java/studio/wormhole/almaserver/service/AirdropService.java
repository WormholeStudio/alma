package studio.wormhole.almaserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import studio.wormhole.almaserver.dto.AirdropDTO;
import studio.wormhole.almaserver.dto.QueryDTO;
import studio.wormhole.almaserver.model.Airdrop;
import studio.wormhole.almaserver.storage.AirdropStorage;

import java.util.List;

@Service
public class AirdropService {

    @Autowired
    AirdropStorage airdropStorage;

    public Airdrop create(AirdropDTO airdropDTO) {
        long id = System.nanoTime();
        Airdrop airdrop = Airdrop.from(airdropDTO);
        airdrop = airdrop.toBuilder().id(id).build();
        airdropStorage.create(airdrop);
        return airdrop;

    }

    public List<Airdrop> list(QueryDTO queryDTO) {
        List<Airdrop> list = airdropStorage.list(queryDTO);
        return list;
    }

    public boolean publish(long airdropId) {
        return airdropStorage.publish(airdropId);
    }

    public boolean delete(long airdropId) {
        return airdropStorage.delete(airdropId);
    }
}
