package studio.wormhole.almaserver.utils.ip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoteIP {
  RemoteIPType type;
  String ip;
}
