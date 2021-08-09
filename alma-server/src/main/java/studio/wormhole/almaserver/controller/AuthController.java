package studio.wormhole.almaserver.controller;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.wormhole.almaserver.dto.AuthDTO;

@RestController
@RequestMapping("auth")
public class AuthController {

  @PostMapping(value = "/nonce")
  public Map<String, Object> list(@RequestBody AuthDTO queryDTO) {

    Map<String, Object> maps = ImmutableMap
        .of("nonce", System.nanoTime(), "message", "this is message");
    return maps;
  }

  @PostMapping(value = "/token")
  public Map<String, Object> token(@RequestBody AuthDTO queryDTO) {
    Map<String, Object> maps = ImmutableMap.of("jwt_token", System.nanoTime(),
        "refresh_token", "refresh_token",
        "expire_in", TimeUnit.HOURS.toMillis(1),
        "refresh_token_expire_in", TimeUnit.HOURS.toMillis(6)
    );
    return maps;
  }

}
