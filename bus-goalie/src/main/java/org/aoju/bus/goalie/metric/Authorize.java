package org.aoju.bus.goalie.metric;

/**
 * 访问授权认证
 *
 * @author Justubborn
 * @version 6.2.0
 * @since JDK 1.8+
 */
public interface Authorize {

    /**
     * 认证接口
     *
     * @param token 授权令牌
     * @return OAuth2
     */
    default Delegate authorize(String token) {
        return new Delegate();
    }

}
