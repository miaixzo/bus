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
package org.aoju.bus.image.metric.params;

import java.net.URL;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public abstract class AbstractParams {

    protected final AdvancedParams params;
    protected final boolean bindCallingAet;
    protected final URL transferCapabilityFile;
    protected final String[] acceptedCallingAETitles;

    /**
     * @param bindCallingAet when true it will set the AET of the listener DICOM node. Only requests with matching called AETitle
     *                       will be accepted. If false all the called AETs will be accepted.
     */
    public AbstractParams(boolean bindCallingAet) {
        this(null, bindCallingAet, null);
    }

    /**
     * @param params         optional advanced parameters (proxy, authentication, connection and TLS)
     * @param bindCallingAet when true it will set the AET of the listener DICOM node. Only requests with matching called AETitle
     *                       will be accepted. If false all the called AETs will be accepted.
     */
    public AbstractParams(AdvancedParams params, boolean bindCallingAet) {
        this(params, bindCallingAet, null);
    }

    /**
     * @param params                  optional advanced parameters (proxy, authentication, connection and TLS)
     * @param bindCallingAet          when true it will set the AET of the listener DICOM node. Only requests with matching called AETitle
     *                                will be accepted. If false all the called AETs will be accepted.
     * @param transferCapabilityFile  an URL for getting a file containing the transfer capabilities (sopClasses, roles, transferSyntaxes)
     * @param acceptedCallingAETitles the list of the accepted calling AETitles. Null will accepted all the AETitles.
     */
    public AbstractParams(AdvancedParams params, boolean bindCallingAet, URL transferCapabilityFile,
                          String... acceptedCallingAETitles) {
        this.params = params == null ? new AdvancedParams() : params;
        this.bindCallingAet = bindCallingAet;
        this.transferCapabilityFile = transferCapabilityFile;
        this.acceptedCallingAETitles = acceptedCallingAETitles == null ? new String[0] : acceptedCallingAETitles;
        if (params == null && this.params.getConnectOptions() != null) {
            // Concurrent DICOM operations
            this.params.getConnectOptions().setMaxOpsInvoked(15);
            this.params.getConnectOptions().setMaxOpsPerformed(15);
        }
    }

    public boolean isBindCallingAet() {
        return bindCallingAet;
    }

    public URL getTransferCapabilityFile() {
        return transferCapabilityFile;
    }

    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETitles;
    }

    public AdvancedParams getParams() {
        return params;
    }

}
