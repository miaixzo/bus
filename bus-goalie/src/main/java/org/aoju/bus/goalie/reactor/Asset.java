package org.aoju.bus.goalie.reactor;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Objects;

/**
 * api definition
 *
 * @author Justubborn
 * @since 2020/10/27
 */
@Data
public class Asset {

  private String id;
  private String name;
  private String host;
  private int port;
  private String url;
  private String method;
  private HttpMethod httpMethod;
  private boolean token;
  private boolean sign;
  private boolean firewall;
  private String version;
  private String description;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Asset asset = (Asset) o;
    return id.equals(asset.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
