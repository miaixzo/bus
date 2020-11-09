package org.aoju.bus.goalie.reactor.login;

import lombok.Data;
import org.aoju.bus.base.consts.Consts;
import org.aoju.bus.base.entity.Message;
import org.aoju.bus.base.entity.OAuth2;

/**
 * @author Justubborn
 * @since 2020/11/6
 */
@Data
public class LoginResponse {

  private Message message;
  private OAuth2 oAuth2;

  public boolean isOk() {
    return Consts.STATUS_ZERO.equals(message.getErrcode());
  }
}
