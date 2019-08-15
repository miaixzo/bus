package org.aoju.bus.trace4j.consts;

import org.aoju.bus.core.consts.Normal;

import javax.xml.namespace.QName;

public final class TraceConsts {

    public static final String TPIC_HEADER = "TPIC";

    public static final String SOAP_HEADER_NAMESPACE = "http://Trace.io/tpic/1.0";

    public static final QName SOAP_HEADER_QNAME = new QName(SOAP_HEADER_NAMESPACE, TPIC_HEADER);

    public static final String SESSION_ID_KEY = "TPIC.sessionId";
    public static final String INVOCATION_ID_KEY = "TPIC.invocationId";
    public static final char[] ALPHANUMERICS = Normal.LETTER_UPPER_NO.toCharArray();

    public static final String DEFAULT = "default";
    public static final String HIDE_INBOUND = "HideInbound";
    public static final String HIDE_OUTBOUND = "HideOutbound";
    public static final String DISABLE_INBOUND = "DisableInbound";
    public static final String DISABLE_OUTBOUND = "DisableOutbound";
    public static final String DISABLED = "Disabled";

}
