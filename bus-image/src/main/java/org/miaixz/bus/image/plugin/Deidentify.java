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
 * The {@code Deidentify} class provides functionality to de-identify DICOM files. It reads a DICOM file, applies
 * de-identification rules using the {@link DeIdentifier}, and writes the modified dataset to a new file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Deidentify {

    /**
     * The de-identifier instance with configured options.
     */
    private final DeIdentifier deidentifier;
    /**
     * Encoding options for writing the de-identified DICOM file.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;

    /**
     * Constructs a new {@code Deidentify} object with the specified de-identification options.
     *
     * @param options The de-identification options to apply.
     */
    public Deidentify(DeIdentifier.Option... options) {
        deidentifier = new DeIdentifier(options);
    }

    /**
     * Sets the encoding options for the output DICOM file.
     *
     * @param encOpts The encoding options.
     */
    public void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    /**
     * Recursively processes a source directory or a single file for de-identification.
     *
     * @param src  The source file or directory.
     * @param dest The destination file or directory.
     */
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

    /**
     * Reads a DICOM file, de-identifies its dataset, and writes the result to a destination file.
     *
     * @param src  The source DICOM file.
     * @param dest The destination file for the de-identified DICOM object.
     * @throws IOException if an I/O error occurs during file reading or writing.
     */
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
