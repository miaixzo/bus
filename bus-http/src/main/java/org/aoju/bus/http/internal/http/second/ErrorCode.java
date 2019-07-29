package org.aoju.bus.http.internal.http.second;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum ErrorCode {
    /**
     * Not an error!
     */
    NO_ERROR(0),

    PROTOCOL_ERROR(1),

    INTERNAL_ERROR(2),

    FLOW_CONTROL_ERROR(3),

    REFUSED_STREAM(7),

    CANCEL(8),

    COMPRESSION_ERROR(9),

    CONNECT_ERROR(0xa),

    ENHANCE_YOUR_CALM(0xb),

    INADEQUATE_SECURITY(0xc),

    HTTP_1_1_REQUIRED(0xd);

    public final int httpCode;

    ErrorCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public static ErrorCode fromHttp2(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.httpCode == code) return errorCode;
        }
        return null;
    }

}
