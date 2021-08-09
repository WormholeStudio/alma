package studio.wormhole.almaserver.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import studio.wormhole.almaserver.dto.AirdropDTO;
import studio.wormhole.almaserver.dto.CSVRecord;
import studio.wormhole.almaserver.dto.QueryDTO;
import studio.wormhole.almaserver.enums.AirdropBtnState;
import studio.wormhole.almaserver.enums.ClaimState;
import studio.wormhole.almaserver.enums.DraftState;
import studio.wormhole.almaserver.enums.State;
import studio.wormhole.almaserver.model.Airdrop;
import studio.wormhole.almaserver.model.AirdropDetail;
import studio.wormhole.almaserver.model.MerkleRecord;
import studio.wormhole.almaserver.service.AirdropService;
import studio.wormhole.almaserver.service.AirdropDetailService;

@Slf4j
@RestController
@RequestMapping("airdrop")
public class AirdropController {

  @Autowired
  private AirdropService airdropService;
  @Autowired
  private AirdropDetailService airdropDetailService;

  @PostMapping(value = "/create")
  public Airdrop create(@RequestBody AirdropDTO airdropDTO) {
    return airdropService.create(airdropDTO);
  }

  @PostMapping(value = "/list")
  public List<Airdrop> list(@RequestBody QueryDTO queryDTO) {

    List<Airdrop> airdropList = airdropService.list(queryDTO);
    log.info("query airdrop list size :{}", airdropList.size());
    return fillMerkleRecord(airdropList, queryDTO.getAddress());

  }

  private List<Airdrop> fillMerkleRecord(List<Airdrop> airdropList, String address) {

    if (StringUtils.isEmpty(address)) {
      return airdropList;
    }
    return airdropList.stream()
        .map(a -> {
          AirdropBtnState btnState = calBtn(a, null);
          if (a.getDraftState() == DraftState.DRAFT || a.getState() == State.DELETED) {
            return a.toBuilder().btnState(btnState).build();
          }
          Optional<MerkleRecord> record = airdropDetailService.getMerkleRecord(a.getId(), address);
          if (!record.isPresent()) {
            return a.toBuilder().btnState(btnState).build();
          }

          btnState = calBtn(a, record.get());
          return a.toBuilder().btnState(btnState).record(record.get()).build();
        }).collect(Collectors.toList());
  }

  private AirdropBtnState calBtn(Airdrop a, MerkleRecord record) {
    Date now = new Date();
    if (a.getStartAt().after(now)) {
      return AirdropBtnState.WAITING;
    }
    if (a.getStartAt().before(now) && a.getParticipateEndAt().after(now)) {
      return AirdropBtnState.JOIN;
    }
    if (a.getParticipateEndAt().before(now) && a.getAirdropAt().after(now)) {
      return AirdropBtnState.SENDING;
    }
    if (record != null) {
      if (record.getClaimState() == ClaimState.CLAIMED) {
        return AirdropBtnState.CLAIMED;
      }
      return AirdropBtnState.UNCLAIMED;
    }

    return AirdropBtnState.FINISH;
  }

  @PostMapping(value = "/publish")
  public Map<String, Boolean> publish(@RequestBody AirdropDTO airdropDTO) {
    boolean rst = airdropService.publish(airdropDTO.getAirdropId());
    return ImmutableMap.of("is_success", rst);
  }

  @PostMapping(value = "/delete")
  public Map<String, Boolean> delete(@RequestBody AirdropDTO airdropDTO) {
    boolean rst = airdropService.delete(airdropDTO.getAirdropId());
    return ImmutableMap.of("is_success", rst);
  }


  @SneakyThrows
  @RequestMapping(value = "/send", method = RequestMethod.POST)
  public AirdropDetail send(@RequestParam("file") MultipartFile file,
      @RequestParam(name = "airdrop_id") long airdropId) {

    CsvToBean<CSVRecord> csvToBean = new CsvToBeanBuilder(
        new InputStreamReader(file.getInputStream()))
        .withType(CSVRecord.class)
        .withIgnoreLeadingWhiteSpace(true)
        .build();

    List<CSVRecord> records = Lists.newArrayList(csvToBean.iterator());

    AirdropDetail airdropDetail = airdropDetailService.create(airdropId, records);

    return airdropDetail;
  }


  @PostMapping(value = "/commit")
  public Map<String, Boolean> commit(@RequestBody AirdropDTO airdropDTO) {

    boolean rst = airdropDetailService
        .commit(airdropDTO.getAirdropId(), airdropDTO.getRoot(), airdropDTO.getTxn());
    return ImmutableMap.of("is_success", rst);
  }


  @PostMapping(value = "/claim")
  public Map<String, Boolean> claim(@RequestBody AirdropDTO airdropDTO) {

    boolean rst = airdropDetailService
        .claim(airdropDTO.getAirdropId(), airdropDTO.getRoot(), airdropDTO.getTxn(),
            airdropDTO.getAddress());
    return ImmutableMap.of("is_success", rst);
  }

}
