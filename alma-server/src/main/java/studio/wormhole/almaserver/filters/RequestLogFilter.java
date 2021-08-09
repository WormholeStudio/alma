package studio.wormhole.almaserver.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.function.Predicate;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;
import studio.wormhole.almaserver.utils.ip.RemoteIPUtils;

@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

  private static final int MAXPAYLOADLENGTH = 64000;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    HttpServletRequest requestToUse = request;
    HttpServletResponse responseToUse = response;
    boolean isFirstRequest = !isAsyncDispatch(request);
    if (isFirstRequest && shouldLog(request)) {
      if (isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
        requestToUse = new ContentCachingRequestWrapper(request, MAXPAYLOADLENGTH);
        if (!(response instanceof ContentCachingResponseWrapper)) {
          responseToUse = new ContentCachingResponseWrapper(responseToUse);
        }
      }
      String message = createMessage(requestToUse, s -> StringUtils
          .equalsAnyIgnoreCase(s, "origin", "authorization", " user-agent", "host", "Content-Type",
              "Referer"));
      log.info("[{}]", message);
    }
    try {
      filterChain.doFilter(requestToUse, responseToUse);
    } finally {
      if (!isAsyncStarted(requestToUse) && shouldLog(request)) {
        ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) responseToUse;
        String responBody = new String(wrapper.getContentAsByteArray());
        if (responBody.length() > 512) {
          responBody = "too_large";
        }
        log.info("[{},{},response={}]", response.getStatus(),
            createMessage(requestToUse, s -> false), responBody);
        wrapper.copyBodyToResponse();
      }
    }
  }

  private boolean shouldLog(HttpServletRequest request) {
    if (!StringUtils.containsAny(request.getMethod(), "GET", "POST")) {
      return false;
    }
    String path = request.getRequestURI();
    return !StringUtils.containsAny(path, "webjars", "html", "docs", "swagger", "actuator",
        "qrcode", "upload", "permission", "all");
  }

  protected String createMessage(HttpServletRequest request, Predicate<String> headerPredicate) {
    StringBuilder msg = new StringBuilder();
    msg.append(request.getMethod()).append(" ");
    msg.append(request.getRequestURI());

    String queryString = request.getQueryString();
    if (queryString != null) {
      msg.append('?').append(queryString);
    }

    String client = RemoteIPUtils.getRemoteIP(request);
    if (StringUtils.isNotBlank(client)) {
      msg.append(", remote_ip=").append(client);
    }
    if (headerPredicate != null) {
      Enumeration<String> names = request.getHeaderNames();
      HttpHeaders filterHeads = new HttpHeaders();
      while (names.hasMoreElements()) {
        String header = names.nextElement();

        boolean need = headerPredicate.test(header);
        if (need) {
          if (header.equalsIgnoreCase("authorization")) {
            filterHeads.add(header, "token");
          } else {
            filterHeads.add(header, request.getHeader(header));
          }
        }
      }
      if (!filterHeads.isEmpty()) {
        msg.append(", headers=").append(filterHeads);
      }
    }

    String payload = getMessagePayload(request);
    if (payload != null) {
      msg.append(", payload=").append(payload);
    }
    return msg.toString();
  }

  @Nullable
  protected String getMessagePayload(HttpServletRequest request) {
    ContentCachingRequestWrapper wrapper =
        WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper != null) {
      byte[] buf = wrapper.getContentAsByteArray();
      if (buf.length > 0) {
        int length = Math.min(buf.length, MAXPAYLOADLENGTH);
        try {
          return new String(buf, 0, length, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException ex) {
          return "[unknown]";
        }
      }
    }
    return null;
  }
}

