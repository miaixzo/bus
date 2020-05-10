/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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

import com.sun.star.io.IOException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.task.ErrorCodeIOException;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.XCloseable;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.bridge.LocalOfficeContextAware;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.provider.LocalOfficeProvider;
import org.aoju.bus.office.provider.SourceDocumentProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 所有本地office任务实现的基类.
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public abstract class AbstractLocalOffice extends AbstractOffice {

    private static final String ERROR_MESSAGE_LOAD = "Could not open document: ";
    protected final Map<String, Object> loadProperties;

    /**
     * 使用指定的源文档创建新任务.
     *
     * @param source 文档的源规范.
     */
    public AbstractLocalOffice(final SourceDocumentProvider source) {
        this(source, null);
    }

    /**
     * 使用指定的源文档创建新任务.
     *
     * @param source         文档的源规范.
     * @param loadProperties 加载文档时要应用的加载属性.
     *                       这些属性是在{@code source}参数中指定的文档格式的加载属性之前添加的.
     */
    public AbstractLocalOffice(
            final SourceDocumentProvider source, final Map<String, Object> loadProperties) {
        super(source);
        this.loadProperties = loadProperties;
    }

    protected static void appendProperties(
            final Map<String, Object> properties, final Map<String, Object> toAddProperties) {
        Optional.ofNullable(toAddProperties).ifPresent(properties::putAll);
    }

    /**
     * 获取要在加载输入文件时应用的office属性
     *
     * @return the map
     */
    protected Map<String, Object> getLoadProperties() {
        final Map<String, Object> loadProps =
                new HashMap<>(Optional.ofNullable(loadProperties).orElse(LocalOfficeProvider.DEFAULT_LOAD_PROPERTIES));
        Optional.ofNullable(source.getFormat())
                .ifPresent(fmt -> appendProperties(loadProps, fmt.getLoadProperties()));
        return loadProps;
    }

    /**
     * 从指定的源文件加载文档
     *
     * @param context    上下文
     * @param sourceFile 源文件
     * @return 文档信息
     */
    protected XComponent loadDocument(final LocalOfficeContextAware context, final File sourceFile)
            throws InstrumentException {
        try {
            final XComponent document = context.getComponentLoader()
                    .loadComponentFromURL(
                            Builder.toUrl(sourceFile), "_blank", 0,
                            Builder.toUnoProperties(getLoadProperties()));
            return document;
        } catch (ErrorCodeIOException exception) {
            throw new InstrumentException(
                    ERROR_MESSAGE_LOAD + sourceFile.getName() + "; errorCode: " + exception.ErrCode,
                    exception);
        } catch (IllegalArgumentException | IOException exception) {
            throw new InstrumentException(ERROR_MESSAGE_LOAD + sourceFile.getName(), exception);
        }
    }

    /**
     * 关闭指定文档
     *
     * @param document 文档信息
     */
    protected void closeDocument(final XComponent document) {
        if (document != null) {
            // 关闭转换后的文档。使用XCloseable。如果支持该接口，则关闭，否则使用XComponent.dispose
            final XCloseable closeable = Lo.qi(XCloseable.class, document);
            if (ObjectUtils.isEmpty(closeable)) {
                // 如果此模型不支持close，请尝试处理它.
                Lo.qi(XComponent.class, document).dispose();
            } else {
                try {
                    // 布尔参数deliverOwnership告诉否决关闭进程的对象，
                    // 如果它们通过抛出CloseVetoException来反对关闭进程，
                    // 那么它们可以假定拥有该进程。这里我们放弃了所有权
                    // 为了安全起见，还是要抓住可能出现的否决例外.
                    closeable.close(true);
                } catch (CloseVetoException closeVetoEx) {
                    // 无论谁提出否决，都应结束该文件
                    throw new InstrumentException(closeVetoEx);
                }
            }
        }
    }

}
