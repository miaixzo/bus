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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.MLLPConnection;
import org.miaixz.bus.image.metric.hl7.MLLPRelease;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * The {@code HL7Snd} class is a client for sending HL7 messages over an MLLP (Minimal Lower Layer Protocol) connection.
 * It can send messages from files, directories, or standard input.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HL7Snd extends Device {

    /**
     * The local network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The remote network connection configuration.
     */
    private final Connection remote = new Connection();

    /**
     * The MLLP release protocol version.
     */
    private MLLPRelease mllpRelease;
    /**
     * The underlying socket for the connection.
     */
    private Socket sock;
    /**
     * The MLLP connection handler.
     */
    private MLLPConnection mllp;

    /**
     * Constructs a new {@code HL7Snd} device.
     */
    public HL7Snd() {
        super("hl7snd");
        addConnection(conn);
    }

    /**
     * Sets the MLLP release protocol to be used.
     *
     * @param mllpRelease The MLLP release version.
     */
    public void setMLLPRelease(MLLPRelease mllpRelease) {
        this.mllpRelease = mllpRelease;
    }

    /**
     * Opens the MLLP connection to the remote peer.
     *
     * @throws IOException              if an I/O error occurs.
     * @throws InternalException        if a configuration error occurs.
     * @throws GeneralSecurityException if a security error occurs.
     */
    public void open() throws IOException, InternalException, GeneralSecurityException {
        sock = conn.connect(remote);
        sock.setSoTimeout(conn.getResponseTimeout());
        mllp = new MLLPConnection(sock, mllpRelease);
    }

    /**
     * Closes the MLLP connection.
     */
    public void close() {
        conn.close(sock);
    }

    /**
     * Sends HL7 messages from a list of file paths. If a path is a directory, it is traversed recursively. A path of
     * "-" signifies that the message should be read from standard input.
     *
     * @param pathnames A list of file or directory paths.
     * @throws IOException if an I/O error occurs.
     */
    public void sendFiles(List<String> pathnames) throws IOException {
        for (String pathname : pathnames)
            if (pathname.equals("-"))
                send(readFromStdIn());
            else {
                Path path = Paths.get(pathname);
                if (Files.isDirectory(path)) {
                    Files.walkFileTree(path, new HL7Send());
                } else
                    send(readFromFile(path));
            }
    }

    /**
     * Sends a single HL7 message and waits for a response.
     *
     * @param data The byte array containing the HL7 message.
     * @throws IOException if the connection is closed by the receiver or another I/O error occurs.
     */
    private void send(byte[] data) throws IOException {
        mllp.writeMessage(data);
        if (mllp.readMessage() == null)
            throw new IOException("Connection closed by receiver");
    }

    /**
     * Reads an HL7 message from standard input.
     *
     * @return A byte array containing the message data.
     */
    private byte[] readFromStdIn() {
        try (FileInputStream in = new FileInputStream(FileDescriptor.in);
                ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            IoKit.copy(in, buf);
            return buf.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the content of a file into a byte array.
     *
     * @param path The path to the file.
     * @return A byte array containing the file's content.
     * @throws IOException if an I/O error occurs.
     */
    private byte[] readFromFile(Path path) throws IOException {
        File f = path.toFile();
        try (FileInputStream in = new FileInputStream(f)) {
            byte[] b = new byte[(int) f.length()];
            StreamKit.readFully(in, b, 0, b.length);
            return b;
        }
    }

    /**
     * A {@link SimpleFileVisitor} that sends each visited file as an HL7 message.
     */
    class HL7Send extends SimpleFileVisitor<Path> {

        /**
         * Called for each file visited. Sends the file content as an HL7 message.
         *
         * @param file  The path to the visited file.
         * @param attrs The file's basic attributes.
         * @return {@link FileVisitResult#CONTINUE} to continue the walk.
         * @throws IOException if an I/O error occurs during sending.
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            send(readFromFile(file));
            return FileVisitResult.CONTINUE;
        }
    }

}
