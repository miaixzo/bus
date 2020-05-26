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
package org.aoju.bus.http;

import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.http.magic.RealResult;
import org.aoju.bus.http.metric.Download;
import org.aoju.bus.http.metric.TaskExecutor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 执行结果
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public interface Results {

    /**
     * 构造一个 Results
     * 此方法构造的 Results 不可设置进度回调，不可进行下载操作！
     * 若需要，请使用方法： {@link #of(Response, TaskExecutor)}
     *
     * @param response Response
     * @return Results
     */
    static Results of(Response response) {
        return of(response, null);
    }

    /**
     * 构造一个 Results
     *
     * @param response     Response
     * @param taskExecutor 任务执行器, 可通过方法 {@link Httpv#executor()} 获得
     * @return Results
     */
    static Results of(Response response, TaskExecutor taskExecutor) {
        if (response != null) {
            return new RealResult(null, response, taskExecutor);
        }
        throw new IllegalArgumentException("Response cannot be empty!");
    }

    /**
     * @return 执行状态
     */
    State getState();

    /**
     * @return HTTP状态码
     */
    int getStatus();

    /**
     * @return 是否响应成功，状态码在 [200..300) 之间
     */
    boolean isSuccessful();

    /**
     * @return 响应头
     */
    Headers getHeaders();

    /**
     * @param name 头名称
     * @return 响应头
     */
    List<String> getHeaders(String name);

    /**
     * @param name 头名称
     * @return 响应头
     */
    String getHeader(String name);

    /**
     * 获取响应报文体长度（从请求头内提取）
     * 在 HEAD 请求时，该方法返回不为 0，但{@link Body#getLength()} 将返回 0
     *
     * @return 长度
     */
    long getContentLength();

    /**
     * @return 响应报文体
     */
    Body getBody();

    /**
     * @return 执行中发生的异常
     */
    IOException getError();

    /**
     * 关闭报文
     * 未对报文体做任何消费时使用，比如只读取报文头
     *
     * @return HttpResult
     */
    Results close();

    enum State {

        /**
         * 执行异常
         */
        EXCEPTION,

        /**
         * 请求被取消
         */
        CANCELED,

        /**
         * 请求已响应
         */
        RESPONSED,

        /**
         * 网络超时
         */
        TIMEOUT,

        /**
         * 网络出错
         */
        NETWORK_ERROR

    }

    /**
     * HTTP响应报文体
     */
    interface Body extends Toable {

        /**
         * @return 媒体类型
         */
        MediaType getType();

        /**
         * @return 报文体字节长度
         */
        long getLength();

        /**
         * 在IO线程执行
         *
         * @return Body
         */
        Body nextOnIO();

        /**
         * 设置报文体接收进度回调
         *
         * @param onProcess 进度回调函数
         * @return Body
         */
        Body setOnProcess(OnBack<Process> onProcess);

        /**
         * 设置进度回调的步进字节，默认 8K（8192）
         * 表示每接收 stepBytes 个字节，执行一次进度回调
         *
         * @param stepBytes 步进字节
         * @return Body
         */
        Body stepBytes(long stepBytes);

        /**
         * 设置进度回调的步进比例
         * 表示每接收 stepRate 比例，执行一次进度回调
         *
         * @param stepRate 步进比例
         * @return Body
         */
        Body stepRate(double stepRate);

        /**
         * 设置进度回调忽略响应的Range头信息，即进度回调会从0开始
         *
         * @return Body
         */
        Body setRangeIgnored();

        /**
         * 下载到指定路径
         * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param filePath 目标路径
         * @return 下载过程 #Download
         */
        Download toFile(String filePath);

        /**
         * 下载到指定文件
         * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param file 目标文件
         * @return 下载过程 #Download
         */
        Download toFile(File file);

        /**
         * 下载到指定文件夹
         * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param dirPath 目标目录
         * @return 下载过程 #Download
         */
        Download toFolder(String dirPath);

        /**
         * 下载到指定文件夹
         * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param dir 目标目录
         * @return 下载过程 #Download
         */
        Download toFolder(File dir);

        /**
         * 缓存自己，缓存后可 重复使用 toXXX 类方法
         *
         * @return Body
         */
        Body cache();

        /**
         * 关闭报文体
         * 未对报文体做任何消费时使用，比如只读取长度
         *
         * @return Body
         */
        Body close();

    }

}
