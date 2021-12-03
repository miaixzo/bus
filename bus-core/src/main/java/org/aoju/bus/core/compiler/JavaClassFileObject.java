package org.aoju.bus.core.compiler;


import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.UriKit;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Java 字节码文件对象，用于在内存中暂存class字节码，从而可以在ClassLoader中动态加载
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public class JavaClassFileObject extends SimpleJavaFileObject {

    /**
     * 字节码输出流
     */
    private final ByteArrayOutputStream byteArrayOutputStream;

    /**
     * 构造
     *
     * @param className 编译后的class文件的类名
     */
    protected JavaClassFileObject(String className) {
        super(UriKit.getStringURI(className.replace(Symbol.DOT, Symbol.SLASH) + Kind.CLASS.extension), Kind.CLASS);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    /**
     * 获得字节码输入流
     * 编译器编辑源码后，我们将通过此输出流获得编译后的字节码，以便运行时加载类
     *
     * @return 字节码输入流
     */
    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * 获得字节码输出流
     * 编译器编辑源码时，会将编译结果输出到本输出流中
     *
     * @return 字节码输出流
     */
    @Override
    public OutputStream openOutputStream() {
        return this.byteArrayOutputStream;
    }

}