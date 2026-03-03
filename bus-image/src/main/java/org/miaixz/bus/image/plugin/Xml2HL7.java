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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.metric.hl7.HL7ContentHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.OutputStreamWriter;

/**
 * The {@code Xml2HL7} class provides a utility to convert an XML representation of an HL7 message back into the
 * traditional HL7 ER7 (pipe and hat) format. It uses a SAX parser and an {@link HL7ContentHandler} to perform the
 * transformation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Xml2HL7 {

    /**
     * Parses an XML file containing an HL7 message and prints the ER7 formatted message to standard output.
     *
     * @param fname The name of the XML file, or "-" to read from standard input.
     * @throws Exception if a parsing error occurs.
     */
    private static void parseXML(String fname) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = f.newSAXParser();
        HL7ContentHandler ch = new HL7ContentHandler(new OutputStreamWriter(System.out));
        if (fname.equals(Symbol.MINUS)) {
            p.parse(System.in, ch);
        } else {
            p.parse(new File(fname), ch);
        }
    }

    /**
     * Parses command-line arguments to get the input filename and handle help/version options. This method is intended
     * for a command-line interface and contains process-exiting calls.
     *
     * @param args The command-line arguments.
     * @return The filename to process.
     */
    private static String fname(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: xml2hl7 [options] <xml-file> | -");
            System.exit(2);
        }
        if (args.length > 1) {
            System.err.println("Error: Only one file argument is accepted.");
            System.exit(2);
        }
        String arg0 = args[0];
        if (arg0.equals("-h") || arg0.equals("--help")) {
            System.out.println("Usage: xml2hl7 [options] <xml-file> | -");
            System.out.println("Converts XML presentation of HL7 message to pipe syntax.");
            System.out.println("Options:");
            System.out.println("  -h, --help     display this help and exit");
            System.out.println("  -V, --version  output version information and exit");
            System.exit(0);
        }
        if (arg0.equals("-V") || arg0.equals("--version")) {
            Package p = Xml2HL7.class.getPackage();
            String s = p.getName();
            System.out.println(s.substring(s.lastIndexOf('.') + 1) + ": " + p.getImplementationVersion());
            System.exit(0);
        }
        if (arg0.startsWith("-") && arg0.length() > 1 && !arg0.equals("-")) {
            System.err.println("Error: Unknown option " + arg0);
            System.exit(2);
        }
        return arg0;
    }

}
