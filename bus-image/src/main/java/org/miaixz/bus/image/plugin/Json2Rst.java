/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code Json2Rst} class converts JSON schema files into reStructuredText (RST) format, primarily for generating
 * documentation for DICOM-related LDAP schemas. It processes a root JSON schema file and recursively transforms any
 * referenced schemas.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Json2Rst {

    /**
     * A constant string of '=' characters used for underlining titles in RST.
     */
    private static final String UNDERLINE = "===============================================================";
    /**
     * The input directory containing the JSON schema files.
     */
    private final File indir;
    /**
     * The output directory where the RST files will be generated.
     */
    private final File outdir;
    /**
     * A queue of input files to be processed.
     */
    private final LinkedList<File> inFiles = new LinkedList<>();
    /**
     * A set to keep track of all processed references to avoid duplicates.
     */
    private final HashSet<String> totRefs = new HashSet<>();
    /**
     * The format string for the RST tabular columns directive.
     */
    private String tabularColumns = "|p{4cm}|l|p{8cm}|";

    /**
     * Constructs a new {@code Json2Rst} converter.
     *
     * @param inFile The initial JSON schema file to process.
     * @param outdir The directory where the output RST files will be saved.
     */
    public Json2Rst(File inFile, File outdir) {
        this.indir = inFile.getParentFile();
        this.outdir = outdir;
        inFiles.add(inFile);
    }

    /**
     * Sets the format for the tabular columns in the generated RST file.
     *
     * @param tabularColumns A string defining the column specifications (e.g., "|p{4cm}|l|p{8cm}|").
     */
    public void setTabularColumns(String tabularColumns) {
        this.tabularColumns = tabularColumns;
    }

    /**
     * Starts the conversion process. It iterates through the file queue and transforms each file.
     *
     * @throws IOException if an I/O error occurs during file processing.
     */
    private void process() throws IOException {
        while (!inFiles.isEmpty())
            transform(inFiles.remove());
    }

    /**
     * Transforms a single JSON schema file into an RST file.
     *
     * @param inFile The JSON schema file to transform.
     * @throws IOException if an I/O error occurs during file reading or writing.
     */
    private void transform(File inFile) throws IOException {
        String outFileName = inFile.getName().replace(".schema.json", ".rst");
        File outFile = new File(outdir, outFileName);
        System.out.println(inFile + " => " + outFile);
        try (InputStreamReader is = new InputStreamReader(new FileInputStream(inFile));
                PrintStream out = new PrintStream(new FileOutputStream(outFile))) {
            JsonReader reader = Json.createReader(is);
            writeTo(reader.readObject(), out, outFileName);
        }
    }

    /**
     * Writes the content of a JSON document to an RST file.
     *
     * @param doc         The {@link JsonObject} representing the JSON schema.
     * @param out         The {@link PrintStream} for the output RST file.
     * @param outFileName The name of the output file.
     * @throws IOException if an I/O error occurs.
     */
    private void writeTo(JsonObject doc, PrintStream out, String outFileName) throws IOException {
        writeHeader(doc, out, outFileName);
        ArrayList<String> refs = new ArrayList<>();
        writePropertiesTo(doc, out, refs);
        if (!refs.isEmpty())
            writeTocTree(refs, out);
    }

    /**
     * Writes the header section of the RST file, including the title and table directives.
     *
     * @param doc         The JSON document.
     * @param out         The output stream.
     * @param outFileName The name of the output file, used to derive the LDAP object name.
     */
    private void writeHeader(JsonObject doc, PrintStream out, String outFileName) {
        String title = doc.getString("title");
        out.println(title);
        out.println(UNDERLINE.substring(0, title.length()));
        out.println(doc.getString("description"));
        out.println();
        out.print(".. tabularcolumns:: ");
        out.println(tabularColumns);
        out.print(".. csv-table:: ");
        out.print(title);
        out.print(" Attributes (LDAP Object: ");
        int endIndex = outFileName.length() - 4;
        if (outFileName.startsWith("hl7") || outFileName.startsWith("dcm"))
            out.print(outFileName.substring(0, endIndex));
        else if (outFileName.startsWith("id")) {
            out.print("dcmID");
            out.print(outFileName.substring(2, endIndex));
        } else {
            out.print(isDefinedByDicom(outFileName) ? "dicom" : "dcm");
            out.print(Character.toUpperCase(outFileName.charAt(0)));
            out.print(outFileName.substring(1, endIndex));
        }
        out.println(')');
        out.println("    :header: Name, Type, Description (LDAP Attribute)");
        out.println("    :widths: 23, 7, 70");
        out.println();
    }

    /**
     * Checks if the schema is for a standard DICOM-defined object.
     *
     * @param outFileName The name of the output RST file.
     * @return {@code true} if it's a standard DICOM object, {@code false} otherwise.
     */
    private boolean isDefinedByDicom(String outFileName) {
        switch (outFileName) {
            case "device.rst":
            case "networkAE.rst":
            case "networkConnection.rst":
            case "transferCapability.rst":
                return true;

            default:
                return false;
        }
    }

    /**
     * Writes a 'toctree' (table of contents tree) directive for all referenced schemas.
     *
     * @param refs A list of schema references.
     * @param out  The output stream.
     */
    private void writeTocTree(ArrayList<String> refs, PrintStream out) {
        out.println();
        out.println(".. toctree::");
        out.println();
        for (String ref : refs) {
            out.print("    ");
            out.println(ref.substring(0, ref.length() - 12));
        }
    }

    /**
     * Recursively writes the properties defined in a JSON schema object to the RST file.
     *
     * @param doc  The JSON object containing the properties.
     * @param out  The output stream.
     * @param refs A list to collect new schema references found.
     */
    private void writePropertiesTo(JsonObject doc, PrintStream out, ArrayList<String> refs) {
        JsonObject properties = doc.getJsonObject("properties");
        for (String name : properties.keySet()) {
            JsonObject property = properties.getJsonObject(name);
            if (property.containsKey("properties"))
                writePropertiesTo(property, out, refs);
            else
                writePropertyTo(property, name, out, refs);
        }
    }

    /**
     * Writes a single property from the JSON schema to a row in the RST CSV table.
     *
     * @param property The JSON object for the property.
     * @param name     The name of the property.
     * @param out      The output stream.
     * @param refs     A list to collect new schema references.
     */
    private void writePropertyTo(JsonObject property, String name, PrintStream out, ArrayList<String> refs) {
        JsonObject items = property.getJsonObject("items");
        JsonObject typeObj = items == null ? property : items;
        out.print("    \"");
        boolean isObj = typeObj.containsKey("$ref");
        if (isObj) {
            String ref = typeObj.getString("$ref");
            out.print(":doc:`");
            out.print(ref.substring(0, ref.length() - 12));
            out.print("` ");
            if (items != null)
                out.print("(s)");
            if (totRefs.add(ref)) {
                refs.add(ref);
                inFiles.add(new File(indir, ref));
            }
        } else {
            out.println();
            out.print("    .. _");
            out.print(name);
            out.println(':');
            out.println();
            out.print("    :ref:`");
            out.print(property.getString("title"));
            if (items != null)
                out.print("(s)");
            out.print(" <");
            out.print(name);
            out.print(">`");
        }
        out.print("\",");
        out.print(isObj ? "object" : typeObj.getString("type"));
        out.print(",\"");
        out.print(
                ensureNoUndefinedSubstitutionReferenced(
                        formatURL(property.getString("description")).replace("\"", "\"\"").replaceAll("<br>", "\n\n\t")
                                .replaceAll("\\(hover on options to see their descriptions\\)", "")));
        JsonArray anEnum = typeObj.getJsonArray("enum");
        if (anEnum != null) {
            out.println();
            out.println();
            out.print("    ");
            out.print("Enumerated values:");
            int last = anEnum.size() - 1;
            for (int i = 0; i <= last; i++) {
                out.println();
                out.println();
                out.print("    ");
                String enumOption = anEnum.get(i).toString().replace("\"", "");
                out.print(enumOption.contains("|") ? enumOption.replaceAll("\\|", " (= ") + ")" : enumOption);
            }
        }
        if (!isObj) {
            out.println();
            out.println();
            out.print("    (");
            out.print(name);
            out.print(')');
        }
        out.println('"');
    }

    /**
     * Formats HTML anchor tags in the description into RST-style links.
     *
     * @param desc The description string.
     * @return The formatted description string.
     */
    private String formatURL(String desc) {
        int urlIndex = desc.indexOf("<a href");
        if (urlIndex == -1)
            return desc;

        String url = desc.substring(urlIndex + 9, desc.indexOf("\" target"));
        String placeholder = desc.substring(desc.indexOf("target=\"_blank\">") + 16, desc.indexOf("</a>"));
        String desc2 = desc.substring(0, urlIndex) + '`' + placeholder + " <" + url + ">`_"
                + desc.substring(desc.indexOf("</a>") + 4);
        return desc2.contains("<a href") ? formatURL(desc2) : desc2;
    }

    /**
     * Ensures that any substitution references in the description are properly escaped for RST.
     *
     * @param desc The description string.
     * @return The escaped description string.
     */
    private String ensureNoUndefinedSubstitutionReferenced(String desc) {
        if (!desc.contains("|"))
            return desc;

        StringBuffer sb = new StringBuffer(desc.length());
        Matcher matcher = Pattern.compile(" \\|([^ ]*?)\\|").matcher(desc);
        while (matcher.find()) {
            matcher.appendReplacement(sb, " `|" + matcher.group(1) + "|`");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
