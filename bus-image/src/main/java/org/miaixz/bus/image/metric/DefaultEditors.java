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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.Editors;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultEditors implements Editors {

    private final boolean generateUIDs;
    private final Attributes tagToOverride;
    private final Map<String, String> uidMap;

    public DefaultEditors(Attributes tagToOverride) {
        this(false, tagToOverride);
    }

    /**
     * @param generateUIDs  生成用于研究，系列和实例的新UIDS
     * @param tagToOverride 要覆盖的DICOM属性列表
     */
    public DefaultEditors(boolean generateUIDs, Attributes tagToOverride) {
        this.generateUIDs = generateUIDs;
        this.tagToOverride = tagToOverride;
        this.uidMap = generateUIDs ? new HashMap<>() : null;
    }

    @Override
    public boolean apply(Attributes data, AttributeContext context) {
        if (null != data) {
            boolean update = false;
            if (generateUIDs) {
                if ("2.25".equals(UID.getRoot())) {
                    UID.setRoot("2.25.35");
                }
                // New Study UID
                String oldStudyUID = data.getString(Tag.StudyInstanceUID);
                String studyUID = uidMap.computeIfAbsent(oldStudyUID, k -> UID.createUID());
                data.setString(Tag.StudyInstanceUID, VR.UI, studyUID);

                // New Series UID
                String oldSeriesUID = data.getString(Tag.SeriesInstanceUID);
                String seriesUID = uidMap.computeIfAbsent(oldSeriesUID, k -> UID.createUID());
                data.setString(Tag.SeriesInstanceUID, VR.UI, seriesUID);

                // New Sop UID
                String iuid = UID.createUID();
                data.setString(Tag.SOPInstanceUID, VR.UI, iuid);
                update = true;
            }
            if (null != tagToOverride && !tagToOverride.isEmpty()) {
                data.update(Attributes.UpdatePolicy.OVERWRITE, tagToOverride, null);
                update = true;
            }
            return update;
        }
        return false;
    }

}
