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
package org.miaixz.bus.image.galaxy.io;

import javax.xml.transform.Templates;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.AttributesCoercion;
import org.miaixz.bus.image.galaxy.data.UpdatePolicy;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class XSLTAttributesCoercion implements AttributesCoercion {

    private final Templates templates;
    private final AttributesCoercion next;
    private boolean includeNameSpaceDeclaration;
    private boolean includeKeyword;
    private SAXTransformer.SetupTransformer setupTransformer;

    public XSLTAttributesCoercion(Templates templates, AttributesCoercion next) {
        this.templates = templates;
        this.next = next;
    }

    @Override
    public String remapUID(String uid) {
        return next != null ? next.remapUID(uid) : uid;
    }

    public boolean isIncludeNameSpaceDeclaration() {
        return includeNameSpaceDeclaration;
    }

    public void setIncludeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.includeNameSpaceDeclaration = includeNameSpaceDeclaration;
    }

    public XSLTAttributesCoercion includeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        setIncludeNameSpaceDeclaration(includeNameSpaceDeclaration);
        return this;
    }

    public boolean isIncludeKeyword() {
        return includeKeyword;
    }

    public void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    public XSLTAttributesCoercion includeKeyword(boolean includeKeyword) {
        setIncludeKeyword(includeKeyword);
        return this;
    }

    public SAXTransformer.SetupTransformer getSetupTransformer() {
        return setupTransformer;
    }

    public void setSetupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        this.setupTransformer = setupTransformer;
    }

    public XSLTAttributesCoercion setupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        setSetupTransformer(setupTransformer);
        return this;
    }

    @Override
    public void coerce(Attributes attrs, Attributes modified) throws Exception {
        Attributes newAttrs;
        try {
            newAttrs = SAXTransformer
                    .transform(attrs, templates, includeNameSpaceDeclaration, includeKeyword, setupTransformer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (modified != null) {
            attrs.update(UpdatePolicy.OVERWRITE, newAttrs, modified);
        } else {
            attrs.addAll(newAttrs);
        }
        if (next != null)
            next.coerce(attrs, modified);
    }

}
