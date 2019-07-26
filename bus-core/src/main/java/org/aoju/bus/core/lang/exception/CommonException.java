package org.aoju.bus.core.lang.exception;

import org.aoju.bus.core.utils.StringUtils;

/**
 * 自定义异常: 通用异常类封装
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CommonException extends UncheckedException {

    public CommonException() {
        super();
    }

    public CommonException(String msg) {
        super(msg);
    }

    public CommonException(String messageTemplate, Object... params) {
        super(StringUtils.format(messageTemplate, params));
    }

    public CommonException(Throwable throwable) {
        super(throwable);
    }

    public CommonException(String errcode, String errmsg) {
        super(errcode, errmsg);
    }

}