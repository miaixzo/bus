/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.metric.net;

import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.pdu.AAbort;
import org.miaixz.bus.image.metric.pdu.AAssociateAC;
import org.miaixz.bus.image.metric.pdu.AAssociateRJ;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum State {
    Sta1("Sta1 - Idle") {
        @Override
        public void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        public void closeSocket(Association as) {
            // NO OP
        }

        @Override
        public void closeSocketDelayed(Association as) {
            // NO OP
        }
    },
    Sta2("Sta2 - Transport connection open") {
        @Override
        public void onAAssociateRQ(Association as, AAssociateRQ rq)
                throws IOException {
            as.handle(rq);
        }

        @Override
        public void write(Association as, AAbort aa) {
            as.doCloseSocket();
        }
    },
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    Sta5("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU") {
        @Override
        public void onAAssociateAC(Association as, AAssociateAC ac) {
            as.handle(ac);
        }

        @Override
        public void onAAssociateRJ(Association as, AAssociateRJ rj) {
            as.handle(rj);
        }
    },
    Sta6("Sta6 - Association established and ready for data transfer") {
        @Override
        public void onAReleaseRQ(Association as) {
            as.handleAReleaseRQ();
        }

        @Override
        public void onPDataTF(Association as) throws IOException {
            as.handlePDataTF();
        }

        @Override
        public void writeAReleaseRQ(Association as) throws IOException {
            as.writeAReleaseRQ();
        }

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    Sta7("Sta7 - Awaiting A-RELEASE-RP PDU") {
        @Override
        public void onAReleaseRP(Association as) {
            as.handleAReleaseRP();
        }

        @Override
        public void onAReleaseRQ(Association as) {
            as.handleAReleaseRQCollision();
        }

        @Override
        public void onPDataTF(Association as) throws IOException {
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
        public void onAReleaseRP(Association as) {
            as.handleAReleaseRPCollision();
        }
    },
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU") {
        @Override
        public void onAReleaseRP(Association as) {
            as.handleAReleaseRP();
        }
    },
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    Sta13("Sta13 - Awaiting Transport Connection Close Indication") {
        @Override
        public void onAReleaseRP(Association as) {
            // NO OP
        }

        @Override
        public void onAReleaseRQ(Association as) {
            // NO OP
        }

        @Override
        public void onPDataTF(Association as) {
            // NO OP
        }

        @Override
        public void write(Association as, AAbort aa) {
            // NO OP
        }

        @Override
        public void closeSocketDelayed(Association as) {
            // NO OP
        }
    };

    private final String name;

    State(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void onAAssociateRQ(Association as, AAssociateRQ rq)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    public void onAAssociateAC(Association as, AAssociateAC ac)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    public void onAAssociateRJ(Association as, AAssociateRJ rj)
            throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    public void onPDataTF(Association as) throws IOException {
        as.unexpectedPDU("P-DATA-TF");
    }

    public void onAReleaseRQ(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    public void onAReleaseRP(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    public void writeAReleaseRQ(Association as) throws IOException {

    }

    public void write(Association as, AAbort aa) {
        as.write(aa);
    }

    public void writePDataTF(Association as) throws IOException {

    }

    public void closeSocket(Association as) {
        as.doCloseSocket();
    }

    public void closeSocketDelayed(Association as) {
        as.doCloseSocketDelayed();
    }

}
