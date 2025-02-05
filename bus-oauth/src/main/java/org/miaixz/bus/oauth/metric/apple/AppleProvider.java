/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org justauth.cn and other contributors.        ~
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

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Base64;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.cache.OauthCache;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.ErrorCode;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

/**
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
        // if failed will throw AuthorizedException
        String response = doPostAuthorizationCode(callback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        // https://developer.apple.com/documentation/sign_in_with_apple/tokenresponse
        AccToken.AccTokenBuilder builder = AccToken.builder().accessToken(accessTokenObject.getString("access_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .tokenType(accessTokenObject.getString("token_type")).idToken(accessTokenObject.getString("id_token"));
        if (!StringKit.isEmpty(callback.getUser())) {
            try {
                AppleUserInfo userInfo = JSONObject.parseObject(callback.getUser(), AppleUserInfo.class);
                builder.username(userInfo.getName().getFirstName() + " " + userInfo.getName().getLastName());
            } catch (Exception ignored) {
            }
        }
        return builder.build();
    }

    @Override
    public Material getUserInfo(AccToken authToken) {
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String[] idToken = authToken.getIdToken().split("\\.");
        String payload = new String(urlDecoder.decode(idToken[1]));
        JSONObject object = JSONObject.parseObject(payload);
        // https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/authenticating_users_with_sign_in_with_apple#3383773
        return Material.builder().rawJson(object).uuid(object.getString("sub")).email(object.getString("email"))
                .username(authToken.getUsername()).token(authToken).source(this.complex.toString()).build();
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
     * @see <a href=
     *      "https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret">creating-a-client-secret</a>
     * @return string
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
