package studio.wormhole.almaserver.dto;

import lombok.Data;

@Data
public class AirdropDTO {


    private long projectId;
    private long startAt;
    private long participateEndAt;
    private long airdropAt;
    private long endAt;
    private long tokenId;
    private String name;
    private String ownerAddress;
    private String winnersCount;
    private String totalAmount;
    private String participateUrl;

    private long airdropId;
    private String root;
    private String txn;
    private String address;
}
