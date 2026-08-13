/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.vendor.wechat.ee;

import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.wechat.AbstractWeChatProvider;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Shared WeChat Enterprise client for token exchange and member-profile resolution.
 *
 * @author Kimi Liu
 */
public abstract class AbstractWeChatEeProvider extends AbstractWeChatProvider {

    /**
     * Fixed enterprise basic-member endpoint.
     */
    private static final String MEMBER_URL = "https://qyapi.weixin.qq.com/cgi-bin/user/get";

    /**
     * Fixed enterprise sensitive-member endpoint.
     */
    private static final String DETAIL_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail";

    /**
     * Creates an enterprise family client from explicit dependencies and metadata.
     *
     * @param configuration complete non-null vendor dependencies
     * @param definition    non-null concrete vendor metadata
     */
    protected AbstractWeChatEeProvider(final VendorConfiguration configuration, final VendorDefinition definition) {
        super(configuration, definition);
    }

    /**
     * Parses a typed response and rejects nonzero enterprise error codes.
     *
     * @param <T>      response type
     * @param document response JSON
     * @param type     response class
     * @return validated response
     */
    private static <T extends Response> T read(final String document, final Class<T> type) {
        final T response = JsonKit.toPojo(document, type);
        if (response == null)
            throw new AuthorizedException("WeChat Enterprise response is empty");
        if (response.errcode() != 0)
            throw new AuthorizedException(
                    response.errmsg() == null ? "WeChat Enterprise request failed" : response.errmsg());
        return response;
    }

    /**
     * Retrieves a WeChat Enterprise application token and retains the callback code.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable inbound callback
     * @return successful message containing the mapped token
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.TOKEN))
                .queryParam("corpid", registration.clientId()).queryParam("corpsecret", secret(current)).build();
        final TokenResponse response = read(get(url), TokenResponse.class);
        if (response.access_token() == null)
            throw new AuthorizedException("WeChat Enterprise token response is missing access_token");
        return Message.success(
                VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                        .code(inbound.value("code").orElse(null)).build());
    }

    /**
     * Resolves the enterprise callback member and merges basic and optional sensitive details.
     *
     * @param context root operation context
     * @param token   non-null application token retaining the callback code
     * @return successful message containing the mapped enterprise identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final String callbackUrl = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("access_token", authorization.getToken()).queryParam("code", authorization.getCode())
                .build();
        final CallbackUser callback = read(get(callbackUrl), CallbackUser.class);
        if (callback.userid() == null)
            throw new AuthorizedException("WeChat Enterprise callback did not identify an enterprise member");
        Member member = read(
                get(
                        VendorRequestBuilder.fromUrl(MEMBER_URL).queryParam("access_token", authorization.getToken())
                                .queryParam("userid", callback.userid()).build()),
                Member.class);
        if (StringKit.isNotEmpty(callback.user_ticket())) {
            final Detail detail = read(
                    post(
                            VendorRequestBuilder.fromUrl(DETAIL_URL)
                                    .queryParam("access_token", authorization.getToken()).build(),
                            JsonKit.toJsonString(new Ticket(callback.user_ticket())),
                            MediaType.APPLICATION_JSON),
                    Detail.class);
            member = member.merge(detail);
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(member)).username(member.name())
                        .nickname(member.alias()).avatar(member.avatar()).location(member.address())
                        .email(member.email()).uuid(callback.userid()).gender(getWechatRealGender(member.gender()))
                        .token(authorization).source(descriptor().id()).build());
    }

    /**
     * Common enterprise response fields. @author Kimi Liu
     */
    private interface Response {

        /**
         * Returns the numeric enterprise status.
         *
         * @return numeric enterprise status
         */
        int errcode();

        /**
         * Returns the optional enterprise diagnostic.
         *
         * @return optional enterprise diagnostic
         */
        String errmsg();
    }

    /**
     * Token response.
     *
     * @param errcode      status
     * @param errmsg       message
     * @param access_token token
     * @param expires_in   lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(int errcode, String errmsg, String access_token, int expires_in) implements Response {
    }

    /**
     * Callback member response.
     *
     * @param errcode     status
     * @param errmsg      message
     * @param userid      member identifier
     * @param user_ticket optional detail ticket
     * @author Kimi Liu
     */
    private record CallbackUser(int errcode, String errmsg, String userid, String user_ticket) implements Response {
    }

    /**
     * Member response.
     *
     * @param errcode status
     * @param errmsg  message
     * @param name    display name
     * @param alias   alias
     * @param avatar  avatar URL
     * @param address address
     * @param email   email
     * @param gender  vendor gender code
     * @author Kimi Liu
     */
    private record Member(int errcode, String errmsg, String name, String alias, String avatar, String address,
            String email, String gender) implements Response {

        /**
         * Merges non-null sensitive details.
         *
         * @param detail sensitive detail
         * @return merged member
         */
        private Member merge(final Detail detail) {
            return new Member(errcode, errmsg, detail.name() == null ? name : detail.name(),
                    detail.alias() == null ? alias : detail.alias(), detail.avatar() == null ? avatar : detail.avatar(),
                    detail.address() == null ? address : detail.address(),
                    detail.email() == null ? email : detail.email(),
                    detail.gender() == null ? gender : detail.gender());
        }
    }

    /**
     * Sensitive detail response.
     *
     * @param errcode status
     * @param errmsg  message
     * @param name    display name
     * @param alias   alias
     * @param avatar  avatar URL
     * @param address address
     * @param email   email
     * @param gender  vendor gender code
     * @author Kimi Liu
     */
    private record Detail(int errcode, String errmsg, String name, String alias, String avatar, String address,
            String email, String gender) implements Response {
    }

    /**
     * Sensitive-detail request.
     *
     * @param user_ticket one-time member ticket
     * @author Kimi Liu
     */
    private record Ticket(String user_ticket) {
    }

}
