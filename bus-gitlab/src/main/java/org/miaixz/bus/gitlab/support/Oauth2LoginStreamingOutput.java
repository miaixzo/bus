/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
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
package org.miaixz.bus.gitlab.support;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This StreamingOutput implementation is utilized to send a OAuth2 token request
 * in a secure manner.  The password is never copied to a String, instead it is
 * contained in a SecretString that is cleared when an instance of this class is finalized.
 */
public class Oauth2LoginStreamingOutput implements StreamingOutput, AutoCloseable {

    private final String username;
    private final SecretString password;

    public Oauth2LoginStreamingOutput(String username, CharSequence password) {
        this.username = username;
        this.password = new SecretString(password);
    }

    public Oauth2LoginStreamingOutput(String username, char[] password) {
        this.username = username;
        this.password = new SecretString(password);
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {

        Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        writer.write("{ ");
        writer.write("\"grant_type\": \"password\", ");
        writer.write("\"username\": \"" + username + "\", ");
        writer.write("\"password\": ");

        // Output the quoted password
        writer.write('"');
        for (int i = 0, length = password.length(); i < length; i++) {

            char c = password.charAt(i);
            if (c == '"' || c == '\\') {
                writer.write('\\');
            }

            writer.write(c);
        }

        writer.write('"');

        writer.write(" }");
        writer.flush();
        writer.close();
    }

    /**
     * Clears the contained password's data.
     */
    public void clearPassword() {
        password.clear();
    }

    @Override
    public void close() {
        clearPassword();
    }

    @Override
    public void finalize() throws Throwable {
        clearPassword();
        super.finalize();
    }
}
