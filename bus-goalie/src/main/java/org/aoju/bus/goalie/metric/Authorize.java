package org.aoju.bus.goalie.metric;

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
