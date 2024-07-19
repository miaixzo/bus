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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.builtin.DeIdentifier;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.File;
import java.io.IOException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Deidentify {

    private final DeIdentifier deidentifier;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;

    public Deidentify(DeIdentifier.Option... options) {
        deidentifier = new DeIdentifier(options);
    }

    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    private void mtranscode(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mtranscode(file, new File(dest, file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, src.getName());
        try {
            transcode(src, dest);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        try (ImageInputStream dis = new ImageInputStream(src)) {
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset();
        }
        deidentifier.deidentify(dataset);
        if (fmi != null)
            fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
        try (ImageOutputStream dos = new ImageOutputStream(dest)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        }
    }

}
