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

import lombok.Data;
import org.aoju.bus.image.metric.Connection;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@Data
public class ConnectOptions {

    /* Maximum number of operations this AE may perform asynchronously, unlimited is 0 and not asynchronously is 1 */
    private int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;
    private int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;

    private int maxPdulenRcv = Connection.DEF_MAX_PDU_LENGTH;
    private int maxPdulenSnd = Connection.DEF_MAX_PDU_LENGTH;

    private boolean packPDV = true;
    private int backlog = Connection.DEF_BACKLOG;
    private int connectTimeout = Connection.NO_TIMEOUT;
    private int requestTimeout = Connection.NO_TIMEOUT;
    private int acceptTimeout = Connection.NO_TIMEOUT;
    private int releaseTimeout = Connection.NO_TIMEOUT;
    private int responseTimeout = Connection.NO_TIMEOUT;
    private int retrieveTimeout = Connection.NO_TIMEOUT;
    private int idleTimeout = Connection.NO_TIMEOUT;
    private int socloseDelay = Connection.DEF_SOCKETDELAY;
    private int sosndBuffer = Connection.DEF_BUFFERSIZE;
    private int sorcvBuffer = Connection.DEF_BUFFERSIZE;
    private boolean tcpNoDelay = true;

}
