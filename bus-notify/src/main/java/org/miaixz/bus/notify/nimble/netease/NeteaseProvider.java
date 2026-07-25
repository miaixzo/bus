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
package org.miaixz.bus.notify.nimble.netease;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.FabricX;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.magic.Notice;
import org.miaixz.bus.notify.nimble.AbstractProvider;

/**
 * Abstract base class for NetEase Cloud notification providers, handling common logic like authentication.
 *
 * @param <T> The type of {@link Notice} this provider handles.
 * @param <K> The type of {@link Context} this provider uses.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class NeteaseProvider<T extends Notice, K extends Context> extends AbstractProvider<T, K> {

    /**
     * Hexadecimal digits for SHA1 encoding.
     */
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /**
     * Constructs a {@code NeteaseProvider} with the given context.
     *
     * @param properties The context containing configuration information for the provider.
     */
    public NeteaseProvider(K properties) {
        super(properties);
    }

    /**
     * Encodes a string value using SHA1 algorithm.
     *
     * @param value The string value to encode.
     * @return The SHA1 encoded string.
     * @throws RuntimeException if the SHA1 algorithm is not found.
     */
    private static String encode(String value) {
        if (null == value) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("sha1");
            messageDigest.update(value.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Notify",
                    e,
                    "NetEase checksum encoding failed: valuePresent={}, exception={}",
                    value != null,
                    e.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation.
     */
    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (byte aByte : bytes) {
            buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return buf.toString();
    }

    /**
     * Retrieves the HTTP headers required for NetEase Cloud API POST requests. These headers include AppKey, Nonce,
     * CurTime, and CheckSum for authentication.
     *
     * @return A {@link HashMap} containing the necessary HTTP headers.
     */
    protected HashMap<String, String> getPostHeader() {
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        HashMap<String, String> map = new HashMap<>();
        map.put("AppKey", context.getAppKey());
        map.put("Nonce", context.getNonce());
        map.put("CurTime", curTime);
        map.put("CheckSum", getCheckSum(curTime));
        return map;
    }

    /**
     * Sends a POST request to the specified router URL with the given parameters and headers.
     *
     * @param routerUrl The URL to send the POST request to.
     * @param map       The request body parameters.
     * @return A {@link Message} indicating the result of the sending operation.
     */
    public Message<Void> post(String routerUrl, Map<String, String> map) {
        Map<String, String> header = getPostHeader();
        Logger.debug(
                true,
                "Notify",
                "NetEase notify request started: url={}, parameterCount={}, headerCount={}",
                routerUrl,
                map == null ? 0 : map.size(),
                header == null ? 0 : header.size());
        String response = FabricX.post(routerUrl, map, header);
        String code = JsonKit.getValue(response, "Code");
        Message<Void> result = Message.<Void>builder()
                .errcode(String.valueOf(Http.Status.OK).equals(code) ? ErrorCode._SUCCESS.getKey() : code)
                .errmsg(JsonKit.getValue(response, "desc")).build();
        Logger.debug(
                false,
                "Notify",
                "NetEase notify response received: url={}, code={}, errcode={}, responseBytes={}",
                routerUrl,
                code,
                result.getErrcode(),
                response == null ? 0 : response.length());
        return result;
    }

    /**
     * Calculates the CheckSum for NetEase Cloud API authentication.
     *
     * @param curTime The current time in seconds since epoch.
     * @return The calculated CheckSum string.
     */
    private String getCheckSum(String curTime) {
        return encode(context.getAppSecret() + context.getNonce() + curTime);
    }

}
