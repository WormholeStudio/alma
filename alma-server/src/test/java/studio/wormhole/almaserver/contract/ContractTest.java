package studio.wormhole.almaserver.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.common.io.Files;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.starcoin.bean.ScriptFunctionObj;
import org.starcoin.bean.TypeObj;
import org.starcoin.types.AccountAddress;
import org.starcoin.types.Ed25519PrivateKey;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.BcsSerializeHelper;
import org.starcoin.utils.ChainInfo;
import org.starcoin.utils.SignatureUtils;
import org.starcoin.utils.StarcoinClient;
import studio.wormhole.almaserver.dto.CSVRecord;
import studio.wormhole.almaserver.model.ApiMerkleProof;
import studio.wormhole.almaserver.model.ApiMerkleTree;
import studio.wormhole.almaserver.utils.MerkleTreeHelper;

public class ContractTest {

  private StarcoinClient starcoinClient = new StarcoinClient(ChainInfo.BARNARD);
  private String address = "0xf8af03dd08de49d81e4efd9e24c039cc";
  private String privateKeyString = "0x7899f7cac425b5ce7239eb313db06ac2a93c731ea4512b857f975c0447176b25";


  private Ed25519PrivateKey privateKey = SignatureUtils.strToPrivateKey(privateKeyString);
  private AccountAddress sender = AccountAddressUtils.create(address);

  private boolean checkTxt(String txn) {
    String rst = starcoinClient.getTransactionInfo(txn);
    JSONObject jsonObject = JSON.parseObject(rst);
    JSONObject result = jsonObject.getJSONObject("result");
    if (result != null) {
      return true;
    }
    return false;
  }


  @Test
  public void batchDeployContract() {
    String rootDir = "/Users/reilost/Dropbox/wormhole/alma-contract/storage/0xf8af03dd08de49d81e4efd9e24c039cc/modules";
    Iterable<File> fileIterable = Files
        .fileTraverser()
        .breadthFirst(new File(rootDir));
    Streams.stream(fileIterable)
        .filter(s -> s.isFile())
        .filter(s -> s.getPath().endsWith(".mv"))
        .filter(s -> !s.getName().startsWith("Test"))
        .filter(s -> !StringUtils.equals(s.getName(), "ALM.mv"))
        .filter(s -> StringUtils.containsAny(s.getName(), "script"))
        .forEach(s -> {
          String rst = starcoinClient.deployContractPackage(sender, privateKey, s.getAbsolutePath(),
              null);
          JSONObject jsonObject = JSON.parseObject(rst);
          String txn = jsonObject.getString("result");
          boolean txnState = checkTxt(txn);
          while (!txnState) {
            try {
              Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            txnState = checkTxt(txn);
          }
          System.out.println(s.getAbsolutePath() + "\n" + rst);
        });
  }


  Map<String, String> claimAddreeMap = ImmutableMap.of(
      "0x0d735c4ba0f540cbb287312f3e137596",
      "c53aa6cf58267162b1d4e9841f26ab4f99550cb66900c7e18151ba78cb34bf21",
      "0xf8af03dd08de49d81e4efd9e24c039cc",
      "0x7899f7cac425b5ce7239eb313db06ac2a93c731ea4512b857f975c0447176b25"
  );

  private void _testClaim(long airdropId,
      String root, ApiMerkleProof proof) {

    TypeObj typeObj =
        TypeObj.builder()
            .moduleAddress("0xf8af03dd08de49d81e4efd9e24c039cc")
            .moduleName("ALM")
            .name("ALM")
            .build();

    Ed25519PrivateKey claimUserPk = SignatureUtils
        .strToPrivateKey(claimAddreeMap.get(proof.getAddress()));
    AccountAddress claimUser = AccountAddressUtils.create(proof.getAddress());

    ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
        .builder()
        .moduleAddress("0xf8af03dd08de49d81e4efd9e24c039cc")
        .moduleName("MerkleDistributorScript")
        .functionName("claim_script")
        .tyArgs(Lists.newArrayList(typeObj))
        .args(Lists.newArrayList(
            BcsSerializeHelper.serializeAddressToBytes(sender),
            BcsSerializeHelper.serializeU64ToBytes(airdropId),
            BcsSerializeHelper.serializeVectorU8ToBytes(root),
            BcsSerializeHelper.serializeU64ToBytes(proof.getIndex()),
            BcsSerializeHelper.serializeU128ToBytes(proof.getAmount()),
            BcsSerializeHelper.serializeListToBytes(Lists
                .newArrayList(proof.getProof()))
            )
        )
        .build();
    String rst = starcoinClient.callScriptFunction(claimUser, claimUserPk, scriptFunctionObj);
    System.out.println(rst);
  }

  @Test
  public void mintAlm() {
    ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
        .builder()
        .moduleAddress("0xf8af03dd08de49d81e4efd9e24c039cc")
        .moduleName("ALM")
        .functionName("mint_script")
        .tyArgs(Lists.newArrayList())
        .args(Lists.newArrayList(
            BcsSerializeHelper.serializeU128ToBytes(new BigInteger("10000000000000000"))
            )
        )
        .build();
    String rst = starcoinClient.callScriptFunction(sender, privateKey, scriptFunctionObj);
    System.out.println(rst);
  }


  private static CsvToBean<CSVRecord> csvToBean = new CsvToBeanBuilder(
      new InputStreamReader(
          ContractTest.class.getClassLoader().getResourceAsStream("reward.csv")))
      .withType(CSVRecord.class)
      .withIgnoreLeadingWhiteSpace(true)
      .build();

  private static List<CSVRecord> records = Lists.newArrayList(csvToBean.iterator());


  @SneakyThrows
  @Test
  public void testCreateALMAirdrop() {

    long airDropId = System.currentTimeMillis();
    ApiMerkleTree merkleTree = MerkleTreeHelper.merkleTree(airDropId, records);
    BigInteger amount = merkleTree.getProofs().stream().map(s -> s.getAmount())
        .reduce((bigInteger, bigInteger2) -> bigInteger.add(bigInteger2)).get();
    TypeObj typeObj =
        TypeObj.builder()
            .moduleAddress("0xf8af03dd08de49d81e4efd9e24c039cc")
            .moduleName("ALM")
            .name("ALM")
            .build();
    ScriptFunctionObj scriptFunctionObj = ScriptFunctionObj
        .builder()
        .moduleAddress("0xf8af03dd08de49d81e4efd9e24c039cc")
        .moduleName("MerkleDistributorScript")
        .functionName("create")
        .tyArgs(Lists.newArrayList(typeObj))
        .args(Lists.newArrayList(
            BcsSerializeHelper.serializeU64ToBytes(airDropId),
            BcsSerializeHelper.serializeVectorU8ToBytes(merkleTree.getRoot()),
            BcsSerializeHelper.serializeU128ToBytes(amount),
            BcsSerializeHelper.serializeU64ToBytes(Long.valueOf(merkleTree.getProofs().size()))
            )
        )
        .build();
    String rst = starcoinClient.callScriptFunction(sender, privateKey, scriptFunctionObj);
    JSONObject jsonObject = JSON.parseObject(rst);
    String txn = jsonObject.getString("result");

    boolean txnSuccess = checkTxt(txn);
    while (!txnSuccess) {
      Thread.sleep(TimeUnit.SECONDS.toMillis(5));
      txnSuccess = checkTxt(txn);
    }
//    testClaim(airDropId, merkleTree);

    merkleTree.getProofs().stream().forEach(apiMerkleProof -> {
      System.out.println("----------------------");
      System.out.println("air_drop_id:" + airDropId);
      System.out.println("owner_address:" + address);
      System.out.println("root:" + merkleTree.getRoot());
      System.out.println("address:" + apiMerkleProof.getAddress());
      System.out.println("index:" + apiMerkleProof.getIndex());
      System.out.println("amount:" + apiMerkleProof.getAmount());
      System.out.println("proof:" + Joiner.on(",").join(apiMerkleProof.getProof()));
      System.out.println("json:" + JSON.toJSONString(apiMerkleProof));
      System.out.println("----------------------");
    });

    merkleTree.getProofs().stream()
        .filter(s->s.getAddress().equals(address))
        .forEach(proof -> _testClaim(airDropId, merkleTree.getRoot(), proof));
  }

}
