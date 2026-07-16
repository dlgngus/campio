package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SourceUrlPolicy {

  @Value("${campio.ingestion.allow-unresolved-hosts:false}")
  private boolean allowUnresolvedHosts;

  public URI requirePublicHttpUrl(String value) {
    try {
      URI uri = URI.create(value);
      String scheme = uri.getScheme();
      String host = uri.getHost();
      if (host == null
          || uri.getUserInfo() != null
          || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
        throw new BadRequestException("Source URL must be a public HTTP or HTTPS URL");
      }
      for (InetAddress address : InetAddress.getAllByName(host)) {
        if (address.isAnyLocalAddress()
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()
            || address.isMulticastAddress()) {
          throw new BadRequestException("Private network source URLs are not allowed");
        }
      }
      return uri;
    } catch (UnknownHostException ex) {
      if (allowUnresolvedHosts) {
        return URI.create(value);
      }
      throw new BadRequestException("Source URL host could not be resolved");
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Source URL must be a public HTTP or HTTPS URL");
    }
  }
}
