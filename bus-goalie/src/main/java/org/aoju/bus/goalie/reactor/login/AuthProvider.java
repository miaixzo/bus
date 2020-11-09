package org.aoju.bus.goalie.reactor.login;

/**
 * @author Justubborn
 * @since 2020/11/6
 */
public interface AuthProvider {

  /**
   * 认证接口
   *
   * @return OAuth2
   */
  default LoginResponse authorize(String token) {

    return new LoginResponse();
  }
}
