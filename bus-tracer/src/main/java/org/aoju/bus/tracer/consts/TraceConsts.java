/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.tracer.consts;

import org.aoju.bus.core.lang.Normal;

import javax.xml.namespace.QName;

/**
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public final class TraceConsts {

    public static final String TPIC_HEADER = "TPIC";

    public static final String SOAP_HEADER_NAMESPACE = "https://www.aoju.org/tpic/1.0";

    public static final QName SOAP_HEADER_QNAME = new QName(SOAP_HEADER_NAMESPACE, TPIC_HEADER);

    public static final String SESSION_ID_KEY = "TPIC.sessionId";
    public static final String INVOCATION_ID_KEY = "TPIC.invocationId";
    public static final char[] ALPHANUMERICS = Normal.UPPER_LOWER_NUMBER.toCharArray();

    public static final String DEFAULT = "default";
    public static final String HIDE_INBOUND = "HideInbound";
    public static final String HIDE_OUTBOUND = "HideOutbound";
    public static final String DISABLE_INBOUND = "DisableInbound";
    public static final String DISABLE_OUTBOUND = "DisableOutbound";
    public static final String DISABLED = "Disabled";

}
