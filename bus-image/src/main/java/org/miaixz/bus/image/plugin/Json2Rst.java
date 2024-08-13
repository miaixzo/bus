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
package org.miaixz.bus.image.plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Json2Rst {

    private static final String UNDERLINE = "===============================================================";
    private final File indir;
    private final File outdir;
    private final LinkedList<File> inFiles = new LinkedList<>();
    private final HashSet<String> totRefs = new HashSet<>();
    private String tabularColumns = "|p{4cm}|l|p{8cm}|";

    public Json2Rst(File inFile, File outdir) {
        this.indir = inFile.getParentFile();
        this.outdir = outdir;
        inFiles.add(inFile);
    }

    public void setTabularColumns(String tabularColumns) {
        this.tabularColumns = tabularColumns;
    }

    private void process() throws IOException {
        while (!inFiles.isEmpty())
            transform(inFiles.remove());
    }

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

    private void writeTo(JsonObject doc, PrintStream out, String outFileName) throws IOException {
        writeHeader(doc, out, outFileName);
        ArrayList<String> refs = new ArrayList<>();
        writePropertiesTo(doc, out, refs);
        if (!refs.isEmpty())
            writeTocTree(refs, out);
    }

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

    private boolean isDefinedByDicom(String outFileName) {
        switch (outFileName) {
        case "device.rst":
        case "networkAE.rst":
        case "networkConnection.rst":
        case "transferCapability.rst":
            return true;
        }
        return false;
    }

    private void writeTocTree(ArrayList<String> refs, PrintStream out) {
        out.println();
        out.println(".. toctree::");
        out.println();
        for (String ref : refs) {
            out.print("    ");
            out.println(ref.substring(0, ref.length() - 12));
        }
    }

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
        out.print(ensureNoUndefinedSubstitutionReferenced(
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
