package studio.wormhole.almaserver.dto;

import com.opencsv.bean.CsvBindByName;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class CSVRecord {

  @CsvBindByName(column = "address", required = true)
  private String address;
  @CsvBindByName(column = "amount", required = true)
  private BigInteger amount;
  private long index;


}
