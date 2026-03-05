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
