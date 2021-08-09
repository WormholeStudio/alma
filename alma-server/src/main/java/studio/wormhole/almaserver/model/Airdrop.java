package studio.wormhole.almaserver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import studio.wormhole.almaserver.dto.AirdropDTO;
import studio.wormhole.almaserver.enums.AirdropBtnState;
import studio.wormhole.almaserver.enums.DraftState;
import studio.wormhole.almaserver.enums.State;

@SuperBuilder(toBuilder = true)

@NoArgsConstructor
@Data
public class Airdrop extends TokenBase {

  private long id;
  private long projectId;
  private String name;
  private String ownerAddress;
  private String winnersCount;
  private String totalAmount;
  private String participateUrl;
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Date startAt;
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Date participateEndAt;
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Date airdropAt;
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Date endAt;
  private DraftState draftState;
  private State state;
  private MerkleRecord record;


  private AirdropBtnState btnState;

  public static Airdrop from(AirdropDTO airdropDTO) {
    return Airdrop.builder()
        .name(airdropDTO.getName())
        .projectId(airdropDTO.getProjectId())
        .ownerAddress(airdropDTO.getOwnerAddress())
        .winnersCount(airdropDTO.getWinnersCount())
        .totalAmount(airdropDTO.getTotalAmount())
        .participateUrl(airdropDTO.getParticipateUrl())
        .startAt(new Date(airdropDTO.getStartAt()))
        .participateEndAt(new Date(airdropDTO.getParticipateEndAt()))
        .airdropAt(new Date(airdropDTO.getAirdropAt()))
        .endAt(new Date(airdropDTO.getEndAt()))
        .draftState(DraftState.DRAFT)
        .tokenId(airdropDTO.getTokenId())
        .state(State.NORMAL)
        .build();
  }
}
