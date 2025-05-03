/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.oauth.metric.apple;

import lombok.Data;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.cache.OauthCache;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Map;

/**
 * Apple 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AppleProvider extends AbstractProvider {

    private static final String AUD = "https://appleid.apple.com";

    private volatile PrivateKey privateKey;

    public AppleProvider(Context context) {
        super(context, Registry.APPLE);
    }

    public AppleProvider(Context context, OauthCache oauthCache) {
        super(context, Registry.APPLE, oauthCache);
    }

    @Override
    public String authorize(String state) {
        return Builder.fromUrl(super.authorize(state)).queryParam("response_mode", "form_post")
                .queryParam("scope", this.getScopes(" ", false, getDefaultScopes(AppleScope.values()))).build();
    }

    @Override
    public AccToken getAccessToken(Callback callback) {
        if (!StringKit.isEmpty(callback.getError())) {
            throw new AuthorizedException(callback.getError());
        }
        this.context.setAppSecret(this.getToken());
        // 如果失败将抛出 AuthorizedException
        String response = doPostAuthorizationCode(callback.getCode());
        try {
            Map<String, Object> accessTokenObject = JsonKit.toPojo(response, Map.class);
            if (accessTokenObject == null) {
                throw new AuthorizedException("Failed to parse access token response: empty response");
            }

            String accessToken = (String) accessTokenObject.get("access_token");
            if (accessToken == null) {
                throw new AuthorizedException("Missing access_token in response");
            }
            Object expiresInObj = accessTokenObject.get("expires_in");
            int expiresIn = expiresInObj instanceof Number ? ((Number) expiresInObj).intValue() : 0;
            String refreshToken = (String) accessTokenObject.get("refresh_token");
            String tokenType = (String) accessTokenObject.get("token_type");
            String idToken = (String) accessTokenObject.get("id_token");

            AccToken.AccTokenBuilder builder = AccToken.builder().accessToken(accessToken).expireIn(expiresIn)
                    .refreshToken(refreshToken).tokenType(tokenType).idToken(idToken);

            if (!StringKit.isEmpty(callback.getUser())) {
                try {
                    Map<String, Object> userInfoMap = JsonKit.toPojo(callback.getUser(), Map.class);
                    if (userInfoMap != null) {
                        Map<String, Object> nameMap = (Map<String, Object>) userInfoMap.get("name");
                        if (nameMap != null) {
                            String firstName = (String) nameMap.get("firstName");
                            String lastName = (String) nameMap.get("lastName");
                            if (firstName != null && lastName != null) {
                                builder.username(firstName + " " + lastName);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 忽略用户信息的解析错误，继续返回 token
                }
            }
            return builder.build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse access token response: " + e.getMessage());
        }
    }

    @Override
    public Material getUserInfo(AccToken authToken) {
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String[] idToken = authToken.getIdToken().split("\\.");
        String payload = new String(urlDecoder.decode(idToken[1]));
        try {
            Map<String, Object> object = JsonKit.toPojo(payload, Map.class);
            if (object == null) {
                throw new AuthorizedException("Failed to parse id_token payload: empty response");
            }

            String sub = (String) object.get("sub");
            if (sub == null) {
                throw new AuthorizedException("Missing sub in id_token payload");
            }
            String email = (String) object.get("email");

            return Material.builder().rawJson(JsonKit.toJsonString(object)).uuid(sub).email(email)
                    .username(authToken.getUsername()).token(authToken).source(this.complex.toString()).build();
        } catch (Exception e) {
            throw new AuthorizedException("Failed to parse id_token payload: " + e.getMessage());
        }
    }

    @Override
    protected void checkConfig(Context context) {
        super.checkConfig(context);
        if (StringKit.isEmpty(context.getAppKey())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_CLIENT_ID.getCode(), this.complex);
        }
        if (StringKit.isEmpty(context.getAppSecret())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_CLIENT_SECRET.getCode(), this.complex);
        }
        if (StringKit.isEmpty(context.getKid())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_KID.getCode(), this.complex);
        }
        if (StringKit.isEmpty(context.getTeamId())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_TEAM_ID.getCode(), this.complex);
        }
    }

    /**
     * 获取token
     *
     * @return string
     * @see <a href=
     *      "https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret">creating-a-client-secret</a>
     */
    private String getToken() {
        /*
         * return JWT.header() .add(AbstractJwk.KID.getId(), this.context.getKid()).and()
         * .issuer(this.context.getTeamId()) .subject(this.context.getAppKey()).audience() .add(AUD).and()
         * .expiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3))).issuedAt(new Date())
         * .signWith(getPrivateKey()).compact();
         */
        return null;
    }

    private PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            synchronized (this) {
                if (this.privateKey == null) {
                    try (PEMParser pemParser = new PEMParser(new StringReader(this.context.getAppSecret()))) {
                        JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter();
                        PrivateKeyInfo keyInfo = (PrivateKeyInfo) pemParser.readObject();
                        this.privateKey = pemKeyConverter.getPrivateKey(keyInfo);
                    } catch (IOException e) {
                        throw new AuthorizedException("Failed to get apple private key", e);
                    }
                }
            }
        }
        return this.privateKey;
    }

    @Data
    static class AppleUserInfo {
        private AppleUsername name;
        private String email;
    }

    @Data
    static class AppleUsername {
        private String firstName;
        private String lastName;
    }

}
