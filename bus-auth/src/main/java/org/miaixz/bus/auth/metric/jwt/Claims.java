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
package org.miaixz.bus.auth.metric.jwt;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;

/**
 * JWT Claims 认证类，用于存储和处理 JWT 的 header 或 payload 数据。
 * <p>
 * Claims 表示 JWT 中的键值对集合，支持解析 Base64 编码的 JSON 字符串，存储为 Map 结构， 并提供设置、获取和序列化功能。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Claims implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852289137231L;

    /**
     * Claims 数据存储，使用 Map 保存键值对
     */
    private Map<String, Object> claims;

    /**
     * 设置 Claims 属性。
     * <p>
     * 如果属性值为 null，则移除该属性；否则将属性名和值存入 Map。
     * </p>
     *
     * @param name  属性名，不能为 null
     * @param value 属性值
     * @throws IllegalArgumentException 如果属性名为空
     */
    public void setClaim(final String name, final Object value) {
        Assert.notNull(name, "Name must be not null!");
        init();
        if (value == null) {
            claims.remove(name);
            return;
        }
        claims.put(name, value);
    }

    /**
     * 批量添加 Claims 属性。
     * <p>
     * 遍历提供的 Map，将每个键值对添加到 Claims 中。
     * </p>
     *
     * @param headerClaims 包含多个属性的 Map
     */
    public void putAll(final Map<String, ?> headerClaims) {
        if (headerClaims != null && !headerClaims.isEmpty()) {
            for (final Map.Entry<String, ?> entry : headerClaims.entrySet()) {
                setClaim(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 获取指定名称的属性值。
     *
     * @param name 属性名
     * @return 属性值，若不存在则返回 null
     */
    public Object getClaim(final String name) {
        init();
        return claims.get(name);
    }

    /**
     * 获取 Claims 的键值对集合。
     *
     * @return Claims 的 Map 表示
     */
    public Map<String, Object> getClaimsJson() {
        init();
        return claims;
    }

    /**
     * 解析 Base64 编码的 JSON 字符串并存储为 Claims。
     * <p>
     * 将 Base64 解码后的 JSON 字符串解析为键值对，存入内部 Map。
     * </p>
     *
     * @param tokenPart Base64 编码的 JSON 字符串
     * @param charset   字符编码
     * @throws IllegalArgumentException 如果 JSON 格式不正确
     */
    public void parse(final String tokenPart, final Charset charset) {
        String decoded = Base64.decodeString(tokenPart, charset);
        this.claims = parseJsonString(decoded);
    }

    /**
     * 将 Claims 转换为 JSON 字符串。
     *
     * @return JSON 格式的字符串表示
     */
    @Override
    public String toString() {
        init();
        return toJsonString(claims);
    }

    /**
     * 初始化 Claims 的 Map 存储。
     * <p>
     * 如果 claims 未初始化，则创建新的 HashMap。
     * </p>
     */
    private void init() {
        if (this.claims == null) {
            this.claims = new HashMap<>();
        }
    }

    /**
     * 解析 JSON 字符串为 Map。
     * <p>
     * 假设 JSON 为简单键值对（无嵌套），支持字符串和数字值。
     * </p>
     *
     * @param json JSON 字符串
     * @return 解析后的 Map
     * @throws IllegalArgumentException 如果 JSON 格式不正确
     */
    private Map<String, Object> parseJsonString(String json) {
        Map<String, Object> result = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) {
            return result;
        }
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
            String value = keyValue[1].trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                result.put(key, value.substring(1, value.length() - 1));
            } else if (value.matches("-?\\d+")) {
                result.put(key, Long.parseLong(value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 将 Map 转换为 JSON 字符串。
     * <p>
     * 将 Map 中的键值对序列化为 JSON 格式字符串，字符串值加引号，数字值不加引号。
     * </p>
     *
     * @param map 键值对 Map
     * @return JSON 字符串
     */
    private String toJsonString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

}
