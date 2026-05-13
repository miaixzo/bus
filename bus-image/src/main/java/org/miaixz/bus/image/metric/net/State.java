/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.net;

import java.io.IOException;

import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.image.metric.pdu.AAbort;
import org.miaixz.bus.image.metric.pdu.AAssociateAC;
import org.miaixz.bus.image.metric.pdu.AAssociateRJ;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;

/**
 * Defines the State values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum State {

    /**
     * The sta1 value.
     */
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
    /**
     * The sta2 value.
     */
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
    /**
     * The sta3 value.
     */
    Sta3("Sta3 - Awaiting local A-ASSOCIATE response primitive"),
    /**
     * The sta4 value.
     */
    Sta4("Sta4 - Awaiting transport connection opening to complete"),
    /**
     * The sta5 value.
     */
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
    /**
     * The sta6 value.
     */
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
    /**
     * The sta7 value.
     */
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
    /**
     * The sta8 value.
     */
    Sta8("Sta8 - Awaiting local A-RELEASE response primitive") {

        @Override
        public void writePDataTF(Association as) throws IOException {
            as.doWritePDataTF();
        }
    },
    /**
     * The sta9 value.
     */
    Sta9("Sta9 - Release collision requestor side; awaiting A-RELEASE response"),
    /**
     * The sta10 value.
     */
    Sta10("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU") {

        @Override
        public void onAReleaseRP(Association as) {
            as.handleAReleaseRPCollision();
        }
    },
    /**
     * The sta11 value.
     */
    Sta11("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU") {

        @Override
        public void onAReleaseRP(Association as) {
            as.handleAReleaseRP();
        }
    },
    /**
     * The sta12 value.
     */
    Sta12("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive"),
    /**
     * The sta13 value.
     */
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

    /**
     * The name value.
     */
    private final String name;

    /**
     * Creates a new instance.
     *
     * @param name the name.
     */
    State(String name) {
        this.name = name;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Executes the on a associate rq operation.
     *
     * @param as the as.
     * @param rq the rq.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateRQ(Association as, AAssociateRQ rq) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RQ");
    }

    /**
     * Executes the on a associate ac operation.
     *
     * @param as the as.
     * @param ac the ac.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateAC(Association as, AAssociateAC ac) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-AC");
    }

    /**
     * Executes the on a associate rj operation.
     *
     * @param as the as.
     * @param rj the rj.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAAssociateRJ(Association as, AAssociateRJ rj) throws IOException {
        as.unexpectedPDU("A-ASSOCIATE-RJ");
    }

    /**
     * Executes the on p data tf operation.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void onPDataTF(Association as) throws IOException {
        as.unexpectedPDU("P-DATA-TF");
    }

    /**
     * Executes the on a release rq operation.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAReleaseRQ(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RQ");
    }

    /**
     * Executes the on a release rp operation.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void onAReleaseRP(Association as) throws IOException {
        as.unexpectedPDU("A-RELEASE-RP");
    }

    /**
     * Writes the a release rq.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeAReleaseRQ(Association as) throws IOException {
        throw new IOException(toString());
    }

    /**
     * Executes the write operation.
     *
     * @param as the as.
     * @param aa the aa.
     */
    public void write(Association as, AAbort aa) {
        as.write(aa);
    }

    /**
     * Writes the p data tf.
     *
     * @param as the as.
     * @throws IOException if the operation cannot be completed.
     */
    public void writePDataTF(Association as) throws IOException {
        throw new IOException(toString());
    }

    /**
     * Closes the socket.
     *
     * @param as the as.
     */
    public void closeSocket(Association as) {
        as.doCloseSocket();
    }

    /**
     * Closes the socket delayed.
     *
     * @param as the as.
     */
    public void closeSocketDelayed(Association as) {
        as.doCloseSocketDelayed();
    }

}
