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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 消息转换器接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Convertor {

    /**
     * 消息的媒体类型
     *
     * @return 媒体类型
     */
    String mediaType();

    /**
     * 解析 Mapper
     *
     * @param in      JSON 输入流
     * @param charset 编码格式
     * @return Mapper
     */
    CoverWapper toMapper(InputStream in, Charset charset);

    /**
     * 解析 Array
     *
     * @param in      JSON 输入流
     * @param charset 编码格式
     * @return Array
     */
    CoverArray toArray(InputStream in, Charset charset);

    /**
     * 将 Java 对象序列化为字节数组
     *
     * @param object  Java 对象
     * @param charset 编码格式
     * @return 字节数组
     */
    byte[] serialize(Object object, Charset charset);

    /**
     * 将 Java 对象序列化为字节数组
     *
     * @param object     Java 对象
     * @param dateFormat 日期类的处理格式
     * @param charset    编码格式
     * @return 字节数组
     */
    byte[] serialize(Object object, String dateFormat, Charset charset);

    /**
     * 解析 Java Bean
     *
     * @param <T>     目标泛型
     * @param type    目标类型
     * @param in      JSON 输入流
     * @param charset 编码格式
     * @return Java Bean
     */
    <T> T toBean(Class<T> type, InputStream in, Charset charset);

    /**
     * 解析为 Java List
     *
     * @param <T>     目标泛型
     * @param type    目标类型
     * @param in      JSON 输入流
     * @param charset 编码格式
     * @return Java List
     */
    <T> List<T> toList(Class<T> type, InputStream in, Charset charset);

    /**
     * 表单转换器，可用于自动系列化表单参数
     */
    class FormConvertor implements Convertor {

        private Convertor convertor;

        public FormConvertor(Convertor convertor) {
            this.convertor = convertor;
        }

        @Override
        public String mediaType() {
            return MediaType.APPLICATION_FORM_URLENCODED;
        }

        @Override
        public CoverWapper toMapper(InputStream in, Charset charset) {
            return convertor.toMapper(in, charset);
        }

        @Override
        public CoverArray toArray(InputStream in, Charset charset) {
            return convertor.toArray(in, charset);
        }

        @Override
        public byte[] serialize(Object object, Charset charset) {
            return serialize(object, null, charset);
        }

        @Override
        public byte[] serialize(Object object, String dateFormat, Charset charset) {
            byte[] data = convertor.serialize(object, dateFormat, charset);
            CoverWapper coverWapper = convertor.toMapper(new ByteArrayInputStream(data), charset);
            StringBuilder sb = new StringBuilder();
            for (String key : coverWapper.keySet()) {
                sb.append(key).append(Symbol.C_EQUAL).append(coverWapper.getString(key)).append(Symbol.C_AND);
            }
            if (sb.length() > 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString().getBytes(charset);
        }

        @Override
        public <T> T toBean(Class<T> type, InputStream in, Charset charset) {
            return convertor.toBean(type, in, charset);
        }

        @Override
        public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
            return convertor.toList(type, in, charset);
        }

    }

}
