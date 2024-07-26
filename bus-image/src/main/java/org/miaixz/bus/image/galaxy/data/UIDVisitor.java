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
package org.miaixz.bus.image.galaxy.data;

import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class UIDVisitor implements Visitor {

    private final Map<String, String> uidMap;
    private final Attributes modified;
    public int replaced;
    private int rootSeqTag;

    public UIDVisitor(Map<String, String> uidMap, Attributes modified) {
        this.uidMap = uidMap;
        this.modified = modified;
    }

    @Override
    public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
        if (vr != VR.UI || val == Value.NULL) {
            if (attrs.isRoot())
                rootSeqTag = vr == VR.SQ ? tag : 0;
            return true;
        }

        String[] ss;
        if (val instanceof byte[]) {
            ss = attrs.getStrings(tag);
            val = ss.length == 1 ? ss[0] : ss;
        }
        if (val instanceof String[]) {
            ss = (String[]) val;
            for (int i = 0, c = 0; i < ss.length; i++) {
                String uid = uidMap.get(ss[i]);
                if (uid != null) {
                    if (c++ == 0)
                        modified(attrs, tag, vr, ss.clone());
                    ss[i] = uid;
                    replaced++;
                }
            }
        } else {
            String uid = uidMap.get(val);
            if (uid != null) {
                modified(attrs, tag, vr, val);
                attrs.setString(tag, VR.UI, uid);
                replaced++;
            }
        }
        return true;
    }

    private void modified(Attributes attrs, int tag, VR vr, Object val) {
        if (modified == null)
            return;

        if (attrs.isRoot()) {
            modified.setValue(tag, vr, val);
        } else if (!modified.contains(rootSeqTag)) {
            Sequence src = attrs.getRoot().getSequence(rootSeqTag);
            Sequence dst = modified.newSequence(rootSeqTag, src.size());
            for (Attributes item : src) {
                dst.add(new Attributes(item));
            }
        }
    }

}
