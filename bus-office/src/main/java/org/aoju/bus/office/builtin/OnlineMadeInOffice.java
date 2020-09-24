/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.builtin;

import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpz;
import org.aoju.bus.http.bodys.MultipartBody;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.bridge.OnlineOfficeContextAware;
import org.aoju.bus.office.metric.RequestBuilder;
import org.aoju.bus.office.provider.SourceDocumentProvider;
import org.aoju.bus.office.provider.TargetDocumentProvider;

import java.io.File;
import java.util.Map;

/**
 * 表示在线转换任务的默认行为.
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public class OnlineMadeInOffice extends AbstractOnlineOffice {

    private final TargetDocumentProvider target;

    /**
     * 创建从指定源到指定目标的新转换任务.
     *
     * @param source 转换的源规范.
     * @param target 转换的目标规范.
     */
    public OnlineMadeInOffice(final SourceDocumentProvider source, final TargetDocumentProvider target) {
        super(source);

        this.target = target;
    }

    private void addPropertiesToBuilder(
            final StringBuilder urlBuilder,
            final Map<String, Object> properties,
            final String parameterPrefix) {

        if (properties != null && !properties.isEmpty()) {
            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                // 首先，检查是否正在处理FilterData属性
                if (Builder.FILTER_DATA.equalsIgnoreCase(key) && Map.class.isInstance(value)) {
                    // 添加所有FilterData属性
                    for (final Map.Entry<String, Object> fdentry : ((Map<String, Object>) value).entrySet()) {
                        urlBuilder.append(parameterPrefix + Builder.FILTER_DATA_PREFIX_PARAM + fdentry.getKey())
                                .append(Symbol.EQUAL)
                                .append(fdentry.getValue().toString())
                                .append(Symbol.AND);
                    }
                } else if (value instanceof String || value.getClass().isPrimitive()) {
                    urlBuilder.append(parameterPrefix + key)
                            .append(Symbol.EQUAL)
                            .append(value.toString())
                            .append(Symbol.AND);
                }
            }
        }
    }

    @Override
    public void execute(final Context context) throws InstrumentException {

        Logger.info("Executing online conversion task...");
        final OnlineOfficeContextAware onlineOfficeContextAware = (OnlineOfficeContextAware) context;

        // 获取一个可以由office加载的源文件。
        // 如果源是一个输入流， 那么将从该流创建一个临时文件
        // 一旦任务完成，临时文件将被删除.
        final File sourceFile = source.getFile();
        try {
            // 获取目标文件(如果输出目标是输出流，则该文件是临时文件).
            final File targetFile = target.getFile();
            try {
                MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
                if (targetFile != null) {
                    // MediaType.valueOf() 里面是上传的文件类型。
                    RequestBody body = RequestBody.create(MediaType.valueOf("image/*"), sourceFile);
                    // 参数分别为， 请求key ，文件名称 ， RequestBody
                    requestBody.addFormDataPart("data", targetFile.getName(), body);
                }

                // 将响应保存到目标文件中.
                final RequestBuilder requestBuilder = onlineOfficeContextAware.getRequestBuilder();
                StringBuilder urlBuilder = new StringBuilder(buildUrl(requestBuilder.getUrl())).append(Symbol.QUESTION_MARK);

                // 我们假设服务器支持自定义加载属性，但是LibreOffice不支持自定义加载属性，只有示例web服务支持.
                addPropertiesToBuilder(
                        urlBuilder, target.getFormat().getLoadProperties(), Builder.LOAD_PROPERTIES_PREFIX_PARAM);

                // 我们假设服务器支持自定义存储属性，但是LibreOffice不支持自定义存储属性，只有样例web服务支持.
                addPropertiesToBuilder(
                        urlBuilder,
                        target.getFormat().getStoreProperties(source.getFormat().getInputFamily()),
                        Builder.STORE_PROPERTIES_PREFIX_PARAM);

                Httpz.post()
                        .url(urlBuilder.toString())
                        .multipartBody(requestBody.build())
                        .tag(context)
                        .build().execute();

                // onComplete on target将把临时文件复制到/ OutputStream中，如果输出是OutputStream，则删除临时文件
                target.onComplete(targetFile);

            } catch (Exception ex) {
                Logger.error("Online conversion failed.", ex);
                final InstrumentException officeEx = new InstrumentException("Online conversion failed", ex);
                target.onFailure(targetFile, officeEx);
                throw officeEx;
            }
        } finally {
            // 这里不再需要源文件，因此如果需要，我们可以删除任何已创建的临时文件.
            source.onConsumed(sourceFile);
        }
    }

    private String buildUrl(final String connectionUrl) {
        return StringKit.appendIfMissing(connectionUrl, Symbol.SLASH) + target.getFormat().getExtension();
    }

}
