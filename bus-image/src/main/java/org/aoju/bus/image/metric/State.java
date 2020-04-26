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
package org.aoju.bus.image.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.image.metric.internal.pdu.AAbort;
import org.aoju.bus.image.metric.internal.pdu.AAssociateAC;
import org.aoju.bus.image.metric.internal.pdu.AAssociateRJ;
import org.aoju.bus.image.metric.internal.pdu.AAssociateRQ;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public enum State {
    Sta1("Sta1 - Idle") {
        @Override
        void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        void closeSocket(Association as) {
            // NO OP
        }

        @Override
        void closeSocketDelayed(Association as) {
            // NO OP
        }
    },
    Sta2("Sta2 - Transport connection open") {
        @Override
        void onAAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.handle(rq);
        }
    },
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU") {
        @Override
        void onAAssociateAC(Association as, AAssociateAC ac)
                throws IOException {
            as.handle(ac);
        }

        @Override
        void onAAssociateRJ(Association as, AAssociateRJ rj)
                throws IOException {
            as.handle(rj);
        }
    },
    Sta6("Sta6 - Association established and ready for data transfer") {
        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQ();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }

        @Override
        void writeAReleaseRQ(Association as) throws IOException {
            as.writeAReleaseRQ();
        }

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    Sta7("Sta7 - Awaiting A-RELEASE-RP PDU") {
        @Override
        public void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            as.handleAReleaseRQCollision();
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }
    },
    Sta8("Sta8 - Awaiting local A-RELEASE response primitive") {
        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
    Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU") {
        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRPCollision();
        }
    },
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU") {
        @Override
        void onAReleaseRP(Association as) throws IOException {
            as.handleAReleaseRP();
        }
    },
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    Sta13("Sta13 - Awaiting Transport Connection Close Indication") {
        @Override
        public void onAReleaseRP(Association as) throws IOException {
            // NO OP
        }

        @Override
        void onAReleaseRQ(Association as) throws IOException {
            // NO OP
        }

        @Override
        void onPDataTF(Association as) throws IOException {
            // NO OP
        }

        @Override
        void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        void closeSocketDelayed(Association as) {
            // NO OP
        }
    };

    private String name;

    State(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    void onAAssociateRQ(Association as, AAssociateRQ rq)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    void onAAssociateAC(Association as, AAssociateAC ac)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    void onAAssociateRJ(Association as, AAssociateRJ rj)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    void onPDataTF(Association as) throws IOException {
        as.unexpectedPDU("P-DATA-TF");
    }

    void onAReleaseRQ(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    void onAReleaseRP(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    void writeAReleaseRQ(Association as) throws IOException {
        throw new InstrumentException(this.toString());
    }

    void write(Association as, AAbort aa) throws IOException {
        as.write(aa);
    }

    public void writePDataTF(Association as) throws IOException {
        throw new InstrumentException(this.toString());
    }

    void closeSocket(Association as) {
        as.doCloseSocket();
    }

    void closeSocketDelayed(Association as) {
        as.doCloseSocketDelayed();
    }
}
