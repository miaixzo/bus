package org.aoju.bus.tracer.consts;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceConsts {

    public static final int DEFAULT_LOG_LENGTH = 1024;

    /**
     * 当前主链ID
     */
    public static final String X_TRACE_ID = "X-Trace_Id";

    /**
     * 调用者ID
     */
    public static final String X_SPAN_ID = "x_span_id";

    /**
     * 被调用者ID
     */
    public static final String X_CHILD_ID = "x_child_Id";

    /**
     * 本地IP
     */
    public static final String X_LOCAL_IP = "x_local_ip";

    /**
     * 远程IP
     */
    public static final String X_REMOTE_IP = "x_remote_ip";

}
