/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.net.url;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.codec.PercentCodec;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * URL中查询字符串部分的封装，类似于：
 * <pre>
 *   key1=v1&amp;key2=&amp;key3=v3
 * </pre>
 * 查询封装分为解析查询字符串和构建查询字符串，解析可通过charset为null来自定义是否decode编码后的内容，
 * 构建则通过charset是否为null是否encode参数键值对
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlQuery {

    /**
     * query中的value，默认除"-", "_", ".", "*"外都编码
     * 这个类似于JDK提供的{@link java.net.URLEncoder}
     */
    public static final PercentCodec ALL = PercentCodec.Builder.of(RFC3986.UNRESERVED)
            .removeSafe(Symbol.C_TILDE).addSafe(Symbol.C_STAR).setEncodeSpaceAsPlus(true).build();

    private final TableMap<CharSequence, CharSequence> query;
    /**
     * 编码模式
     */
    private EncodeMode encodeMode;

    /**
     * 构造
     *
     * @param queryMap   初始化的查询键值对
     * @param encodeMode 编码模式
     */
    public UrlQuery(final Map<? extends CharSequence, ?> queryMap, final EncodeMode encodeMode) {
        if (MapKit.isNotEmpty(queryMap)) {
            query = new TableMap<>(queryMap.size());
            addAll(queryMap);
        } else {
            query = new TableMap<>(Normal._16);
        }
        this.encodeMode = ObjectKit.defaultIfNull(encodeMode, EncodeMode.NORMAL);
    }

    /**
     * 构建UrlQuery
     *
     * @param query 初始化的查询字符串
     * @param charset  decode用的编码，null表示不做decode
     * @return UrlQuery
     */
    public static UrlQuery of(final String query, final Charset charset) {
        return of(query, charset, true);
    }

    /**
     * 构建UrlQuery
     *
     * @param query       初始化的查询字符串
     * @param charset        decode用的编码，null表示不做decode
     * @param autoRemovePath 是否自动去除path部分，{@code true}则自动去除第一个?前的内容
     * @return UrlQuery
     */
    public static UrlQuery of(final String query, final Charset charset, final boolean autoRemovePath) {
        return of(query, charset, autoRemovePath, null);
    }

    /**
     * 构建UrlQuery
     *
     * @param query       初始化的查询字符串
     * @param charset        decode用的编码，null表示不做decode
     * @param autoRemovePath 是否自动去除path部分，{@code true}则自动去除第一个?前的内容
     * @param encodeMode     编码模式。
     * @return UrlQuery
     */
    public static UrlQuery of(final String query, final Charset charset, final boolean autoRemovePath, final EncodeMode encodeMode) {
        return of(encodeMode).parse(query, charset, autoRemovePath);
    }

    /**
     * 构建UrlQuery
     *
     * @return UrlQuery
     */
    public static UrlQuery of() {
        return of(EncodeMode.NORMAL);
    }

    /**
     * 构建UrlQuery
     *
     * @param encodeMode 编码模式
     * @return UrlQuery
     */
    public static UrlQuery of(final EncodeMode encodeMode) {
        return new UrlQuery(null, encodeMode);
    }

    /**
     * 构建UrlQuery
     *
     * @param queryMap 初始化的查询键值对
     * @return UrlQuery
     */
    public static UrlQuery of(final Map<? extends CharSequence, ?> queryMap) {
        return of(queryMap, null);
    }

    /**
     * 构建UrlQuery
     *
     * @param queryMap   初始化的查询键值对
     * @param encodeMode 编码模式
     * @return UrlQuery
     */
    public static UrlQuery of(final Map<? extends CharSequence, ?> queryMap, final EncodeMode encodeMode) {
        return new UrlQuery(queryMap, encodeMode);
    }

    /**
     * 对象转换为字符串，用于URL的Query中
     *
     * @param value 值
     * @return 字符串
     */
    private static String toString(final Object value) {
        final String result;
        if (value instanceof Iterable) {
            result = CollKit.join((Iterable<?>) value, Symbol.COMMA);
        } else if (value instanceof Iterator) {
            result = IteratorKit.join((Iterator<?>) value, Symbol.COMMA);
        } else {
            result = Convert.toString(value);
        }
        return result;
    }

    /**
     * 设置编码模式
     * 根据不同场景以及不同环境，对Query中的name和value采用不同的编码策略
     *
     * @param encodeMode 编码模式
     * @return this
     */
    public UrlQuery setEncodeMode(final EncodeMode encodeMode) {
        this.encodeMode = encodeMode;
        return this;
    }

    /**
     * 增加键值对
     *
     * @param key   键
     * @param value 值，集合和数组转换为逗号分隔形式
     * @return this
     */
    public UrlQuery add(final CharSequence key, final Object value) {
        this.query.put(key, toString(value));
        return this;
    }

    /**
     * 批量增加键值对
     *
     * @param queryMap query中的键值对
     * @return this
     */
    public UrlQuery addAll(final Map<? extends CharSequence, ?> queryMap) {
        if (MapKit.isNotEmpty(queryMap)) {
            queryMap.forEach(this::add);
        }
        return this;
    }

    /**
     * 解析URL中的查询字符串
     *
     * @param query 查询字符串，类似于key1=v1&amp;key2=&amp;key3=v3
     * @param charset  decode编码，null表示不做decode
     * @return this
     */
    public UrlQuery parse(final String query, final Charset charset) {
        return parse(query, charset, true);
    }

    /**
     * 解析URL中的查询字符串
     *
     * @param query       查询字符串，类似于key1=v1&amp;key2=&amp;key3=v3
     * @param charset        decode编码，null表示不做decode
     * @param autoRemovePath 是否自动去除path部分，{@code true}则自动去除第一个?前的内容
     * @return this
     */
    public UrlQuery parse(String query, final Charset charset, final boolean autoRemovePath) {
        if (StringKit.isBlank(query)) {
            return this;
        }

        if (autoRemovePath) {
            // 去掉Path部分
            final int pathEndPos = query.indexOf(Symbol.C_QUESTION_MARK);
            if (pathEndPos > -1) {
                query = StringKit.subSuf(query, pathEndPos + 1);
                if (StringKit.isBlank(query)) {
                    return this;
                }
            }
        }

        return doParse(query, charset);
    }

    /**
     * 获得查询的Map
     *
     * @return 查询的Map，只读
     */
    public Map<CharSequence, CharSequence> getQueryMap() {
        return MapKit.view(this.query);
    }

    /**
     * 获取查询值
     *
     * @param key 键
     * @return 值
     */
    public CharSequence get(final CharSequence key) {
        if (MapKit.isEmpty(this.query)) {
            return null;
        }
        return this.query.get(key);
    }

    /**
     * 构建URL查询字符串，即将key-value键值对转换为{@code key1=v1&key2=v2&key3=v3}形式。
     * 对于{@code null}处理规则如下：
     * <ul>
     *     <li>如果key为{@code null}，则这个键值对忽略</li>
     *     <li>如果value为{@code null}，只保留key，如key1对应value为{@code null}生成类似于{@code key1&key2=v2}形式</li>
     * </ul>
     *
     * @param charset       encode编码，null表示不做encode编码
     * @return URL查询字符串
     */
    public String build(final Charset charset) {
        switch (this.encodeMode) {
            case FORM_URL_ENCODED:
                return build(ALL, ALL, charset);
            case STRICT:
                return build(RFC3986.QUERY_PARAM_NAME_STRICT, RFC3986.QUERY_PARAM_VALUE_STRICT, charset);
            default:
                return build(RFC3986.QUERY_PARAM_NAME, RFC3986.QUERY_PARAM_VALUE, charset);
        }
    }

    /**
     * 构建URL查询字符串，即将key-value键值对转换为{@code key1=v1&key2=v2&key3=v3}形式。
     * 对于{@code null}处理规则如下：
     * <ul>
     *     <li>如果key为{@code null}，则这个键值对忽略</li>
     *     <li>如果value为{@code null}，只保留key，如key1对应value为{@code null}生成类似于{@code key1&key2=v2}形式</li>
     * </ul>
     *
     * @param keyCoder      键值对中键的编码器
     * @param valueCoder    键值对中值的编码器
     * @param charset       encode编码，null表示不做encode编码
     * @return URL查询字符串
     */
    public String build(final PercentCodec keyCoder, final PercentCodec valueCoder,
                        final Charset charset) {
        if (MapKit.isEmpty(this.query)) {
            return Normal.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        CharSequence name;
        CharSequence value;
        for (final Map.Entry<CharSequence, CharSequence> entry : this.query) {
            name = entry.getKey();
            if (null != name) {
                if (sb.length() > 0) {
                    sb.append(Symbol.AND);
                }
                sb.append(keyCoder.encode(name, charset));
                value = entry.getValue();
                if (null != value) {
                    sb.append("=").append(valueCoder.encode(value, charset));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 生成查询字符串，类似于aaa=111&amp;bbb=222
     * 此方法不对任何特殊字符编码，仅用于输出显示
     *
     * @return 查询字符串
     */
    @Override
    public String toString() {
        return build(null);
    }

    /**
     * 解析URL中的查询字符串
     * 规则见：https://url.spec.whatwg.org/#urlencoded-parsing
     *
     * @param query 查询字符串，类似于key1=v1&amp;key2=&amp;key3=v3
     * @param charset  decode编码，null表示不做decode
     * @return this
     */
    private UrlQuery doParse(final String query, final Charset charset) {
        final int len = query.length();
        String name = null;
        int pos = 0; // 未处理字符开始位置
        int i; // 未处理字符结束位置
        char c; // 当前字符
        for (i = 0; i < len; i++) {
            c = query.charAt(i);
            switch (c) {
                case Symbol.C_EQUAL://键和值的分界符
                    if (null == name) {
                        // name可以是""
                        name = query.substring(pos, i);
                        // 开始位置从分节符后开始
                        pos = i + 1;
                    }
                    // 当=不作为分界符时，按照普通字符对待
                    break;
                case Symbol.C_AND: //键值对之间的分界符
                    addParam(name, query.substring(pos, i), charset);
                    name = null;
                    if (i + 4 < len && "amp;".equals(query.substring(i + 1, i + 5))) {
                        // "&amp;"转义为"&"
                        i += 4;
                    }
                    // 开始位置从分节符后开始
                    pos = i + 1;
                    break;
            }
        }

        // 处理结尾
        addParam(name, query.substring(pos, i), charset);

        return this;
    }

    /**
     * 将键值对加入到值为List类型的Map中,，情况如下：
     * <pre>
     *     1、key和value都不为null，类似于 "a=1"或者"=1"，直接put
     *     2、key不为null，value为null，类似于 "a="，值传""
     *     3、key为null，value不为null，类似于 "1"
     *     4、key和value都为null，忽略之，比如&&
     * </pre>
     *
     * @param key     data，为null则value作为key
     * @param value   value，为null且key不为null时传入""
     * @param charset 编码
     */
    private void addParam(final String key, final String value, final Charset charset) {
        final boolean isFormUrlEncoded = EncodeMode.FORM_URL_ENCODED == this.encodeMode;
        if (null != key) {
            final String actualKey = UrlDecoder.decode(key, charset, isFormUrlEncoded);
            this.query.put(actualKey, StringKit.emptyIfNull(UrlDecoder.decode(value, charset, isFormUrlEncoded)));
        } else if (null != value) {
            // name为空，value作为name，value赋值null
            this.query.put(UrlDecoder.decode(value, charset, isFormUrlEncoded), null);
        }
    }


    /**
     * 编码模式
     * 根据不同场景以及不同环境，对Query中的name和value采用不同的编码策略
     */
    public enum EncodeMode {
        /**
         * 正常模式（宽松模式），这种模式下，部分分隔符无需转义
         */
        NORMAL,
        /**
         * x-www-form-urlencoded模式，此模式下空格会编码为'+'，"~"和"*"会被转义
         */
        FORM_URL_ENCODED,
        /**
         * 严格模式，此模式下，非UNRESERVED的字符都会被转义
         */
        STRICT
    }

}
