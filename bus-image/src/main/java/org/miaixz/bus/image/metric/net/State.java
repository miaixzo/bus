/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.net;

import java.io.IOException;

import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.pdu.AAbort;
import org.miaixz.bus.image.metric.pdu.AAssociateAC;
import org.miaixz.bus.image.metric.pdu.AAssociateRJ;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;

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
        public void onAAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
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

    public void onAAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    public void onAAssociateAC(Association as, AAssociateAC ac) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    public void onAAssociateRJ(Association as, AAssociateRJ rj) throws IOException {
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
