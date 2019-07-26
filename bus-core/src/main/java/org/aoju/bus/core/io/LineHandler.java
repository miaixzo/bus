package org.aoju.bus.core.io;

/**
 * 行处理器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface LineHandler {
    /**
     * 处理一行数据，可以编辑后存入指定地方
     *
     * @param line 行
     */
    void handle(String line);
}
