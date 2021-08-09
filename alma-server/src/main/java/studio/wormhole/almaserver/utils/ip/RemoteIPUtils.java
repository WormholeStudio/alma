package studio.wormhole.almaserver.utils.ip;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class RemoteIPUtils {
  static List<String> cloudFlareIpAddress = Lists.newArrayList(
      "173.245.48.0/20",
      "103.21.244.0/22",
      "103.22.200.0/22",
      "103.31.4.0/22",
      "141.101.64.0/18",
      "108.162.192.0/18",
      "190.93.240.0/20",
      "188.114.96.0/20",
      "197.234.240.0/22",
      "198.41.128.0/17",
      "162.158.0.0/15",
      "104.16.0.0/12",
      "172.64.0.0/13",
      "131.0.72.0/22"
  );

  private static final List<IpAddressMatcher> matchers;

  static {
    matchers =
        cloudFlareIpAddress.stream().map(s -> new IpAddressMatcher(s)).collect(Collectors.toList());
  }

  public static String getRemoteIP(HttpServletRequest httpServletRequest) {
    String cdn = httpServletRequest.getHeader("cdn-loop");
    boolean fromCloudFlare = false;
    if (cdn != null && cdn.length() > 0) {
      fromCloudFlare = cdn.contains("cloudflare");
    }

    if (fromCloudFlare) {
      return httpServletRequest.getHeader("CF-Connecting-IP");
    }

    String ipXRealIp = httpServletRequest.getHeader("x-real-ip");

    return ipXRealIp;


  }

  private static String getRealIp(List<RemoteIP> ipList, boolean fromCloudFlare) {

    if (fromCloudFlare) {
      Optional<String> cloudFlareIp = getCloudFlareIp(ipList);
      if (cloudFlareIp.isPresent()) {
        return cloudFlareIp.get();
      }
    }

    return switchIp(ipList);
  }

  private static String switchIp(List<RemoteIP> ipList) {
    return ipList.stream().filter(rm -> rm.getType() == RemoteIPType.XREAL)
        .findFirst().map(s -> s.getIp())
        .orElse("");
  }

  private static Optional<String> getCloudFlareIp(List<RemoteIP> ipList) {
    return ipList.stream()
        .filter(rm -> rm.getType() == RemoteIPType.XFF)
        .findFirst()
        .map(xff -> {
              List<String> xffIpList = Splitter.on(",").trimResults().splitToList(xff.getIp());
              boolean isCdnIp = false;
              for (String s : Lists.reverse(xffIpList)) {
                if (isCdnIp) {
                  return s;
                }
                isCdnIp = matchers.stream().filter(matcher -> matcher.matches(s)).findAny().isPresent();
              }
              return xffIpList.get(0);
            }
        );
  }
}
