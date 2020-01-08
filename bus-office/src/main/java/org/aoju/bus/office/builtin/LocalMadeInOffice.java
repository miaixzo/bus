/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.builtin;

import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.bridge.LocalOfficeContextAware;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.filter.FilterChain;
import org.aoju.bus.office.magic.filter.RefreshFilter;
import org.aoju.bus.office.provider.SourceDocumentProvider;
import org.aoju.bus.office.provider.TargetDocumentProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 表示本地转换任务的默认行为.
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class LocalMadeInOffice extends AbstractLocalOffice {

    private final TargetDocumentProvider target;
    private final FilterChain filterChain;
    private final Map<String, Object> storeProperties;

    /**
     * 创建从指定源到指定目标的新转换任务.
     *
     * @param source          转换的源规范.
     * @param target          转换的目标规范.
     * @param loadProperties  加载文档时要应用的加载属性.
     *                        这些属性是在{@code source}参数中指定的文档格式的加载属性之后添加的.
     * @param filterChain     与此任务一起使用的筛选器链.
     * @param storeProperties 存储文档时应用的存储属性.
     *                        这些属性是在{@code target}参数中指定的文档格式的存储属性之后添加的.
     */
    public LocalMadeInOffice(
            final SourceDocumentProvider source,
            final TargetDocumentProvider target,
            final Map<String, Object> loadProperties,
            final FilterChain filterChain,
            final Map<String, Object> storeProperties) {
        super(source, loadProperties);

        this.target = target;
        this.filterChain =
                Optional.ofNullable(filterChain).map(FilterChain::copy).orElse(RefreshFilter.CHAIN);
        this.storeProperties = storeProperties;
    }

    @Override
    public void execute(final Context context) throws InstrumentException {
        Logger.info("Executing local conversion task...");
        final LocalOfficeContextAware localOfficeContextAware = (LocalOfficeContextAware) context;

        // 获取一个可以由office加载的源文件。如果源是一个输入流，
        // 那么将从该流创建一个临时文件。一旦任务完成，临时文件将被删除.
        final File sourceFile = source.getFile();
        try {
            // 获取目标文件(如果输出目标是输出流，则该文件是临时文件).
            final File targetFile = target.getFile();
            XComponent document = null;
            try {
                document = loadDocument(localOfficeContextAware, sourceFile);
                modifyDocument(context, document);
                storeDocument(document, targetFile);

                // onComplete on target将把临时文件复制到OutputStream中，如果输出是OutputStream，则删除临时文件
                target.onComplete(targetFile);
            } catch (InstrumentException officeEx) {
                Logger.error("Local conversion failed.", officeEx);
                target.onFailure(targetFile, officeEx);
                throw officeEx;
            } catch (Exception ex) {
                Logger.error("Local conversion failed.", ex);
                final InstrumentException officeEx = new InstrumentException("Local conversion failed", ex);
                target.onFailure(targetFile, officeEx);
                throw officeEx;
            } finally {
                closeDocument(document);
            }
        } finally {
            // 这里不再需要源文件，因此如果需要，我们可以删除任何已创建的临时文件.
            source.onConsumed(sourceFile);
        }
    }

    /**
     * 获取将转换后的文档保存为输出文件时要应用的office属性
     *
     * @param document 文档
     * @return the map
     * @throws InstrumentException 异常
     */
    private Map<String, Object> getStoreProperties(final XComponent document) throws InstrumentException {

        final Map<String, Object> storeProps = new HashMap<>();
        appendProperties(
                storeProps,
                target.getFormat().getStoreProperties(Builder.getDocumentFamily(document)));
        appendProperties(storeProps, storeProperties);

        return storeProps;
    }

    /**
     * 在加载文档和以新格式保存文档之前修改文档.
     *
     * @param context  上下文
     * @param document 文档
     */
    protected void modifyDocument(final Context context, final XComponent document)
            throws InstrumentException {
        filterChain.doFilter(context, document);
    }

    /**
     * 将转换后的文档存储为输出文件
     *
     * @param document   文档
     * @param targetFile 目标文件
     */
    protected void storeDocument(final XComponent document, final File targetFile)
            throws InstrumentException {
        final Map<String, Object> storeProps = getStoreProperties(document);
        try {
            Lo.qi(XStorable.class, document).storeToURL(Builder.toUrl(targetFile), Builder.toUnoProperties(storeProps));
        } catch (ErrorCodeIOException errorCodeIoEx) {
            throw new InstrumentException(
                    Builder.ERROR_MESSAGE_STORE + targetFile.getName() + "; errorCode: " + errorCodeIoEx.ErrCode,
                    errorCodeIoEx);
        } catch (IOException ioEx) {
            throw new InstrumentException(Builder.ERROR_MESSAGE_STORE + targetFile.getName(), ioEx);
        }
    }

}
