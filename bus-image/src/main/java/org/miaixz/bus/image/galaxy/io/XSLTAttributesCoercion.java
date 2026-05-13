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
package org.miaixz.bus.image.galaxy.io;

import javax.xml.transform.Templates;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.AttributesCoercion;
import org.miaixz.bus.image.galaxy.data.UpdatePolicy;

/**
 * Represents the XSLTAttributesCoercion type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class XSLTAttributesCoercion implements AttributesCoercion {

    /**
     * The templates value.
     */
    private final Templates templates;

    /**
     * The next value.
     */
    private final AttributesCoercion next;

    /**
     * The include name space declaration value.
     */
    private boolean includeNameSpaceDeclaration;

    /**
     * The include keyword value.
     */
    private boolean includeKeyword;

    /**
     * The setup transformer value.
     */
    private SAXTransformer.SetupTransformer setupTransformer;

    /**
     * Creates a new instance.
     *
     * @param templates the templates.
     * @param next      the next.
     */
    public XSLTAttributesCoercion(Templates templates, AttributesCoercion next) {
        this.templates = templates;
        this.next = next;
    }

    /**
     * Executes the remap uid operation.
     *
     * @param uid the uid.
     * @return the operation result.
     */
    @Override
    public String remapUID(String uid) {
        return next != null ? next.remapUID(uid) : uid;
    }

    /**
     * Determines whether include name space declaration.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIncludeNameSpaceDeclaration() {
        return includeNameSpaceDeclaration;
    }

    /**
     * Sets the include name space declaration.
     *
     * @param includeNameSpaceDeclaration the include name space declaration.
     */
    public void setIncludeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        this.includeNameSpaceDeclaration = includeNameSpaceDeclaration;
    }

    /**
     * Executes the include name space declaration operation.
     *
     * @param includeNameSpaceDeclaration the include name space declaration.
     * @return the operation result.
     */
    public XSLTAttributesCoercion includeNameSpaceDeclaration(boolean includeNameSpaceDeclaration) {
        setIncludeNameSpaceDeclaration(includeNameSpaceDeclaration);
        return this;
    }

    /**
     * Determines whether include keyword.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isIncludeKeyword() {
        return includeKeyword;
    }

    /**
     * Sets the include keyword.
     *
     * @param includeKeyword the include keyword.
     */
    public void setIncludeKeyword(boolean includeKeyword) {
        this.includeKeyword = includeKeyword;
    }

    /**
     * Executes the include keyword operation.
     *
     * @param includeKeyword the include keyword.
     * @return the operation result.
     */
    public XSLTAttributesCoercion includeKeyword(boolean includeKeyword) {
        setIncludeKeyword(includeKeyword);
        return this;
    }

    /**
     * Gets the setup transformer.
     *
     * @return the setup transformer.
     */
    public SAXTransformer.SetupTransformer getSetupTransformer() {
        return setupTransformer;
    }

    /**
     * Sets the setup transformer.
     *
     * @param setupTransformer the setup transformer.
     */
    public void setSetupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        this.setupTransformer = setupTransformer;
    }

    /**
     * Sets the up transformer.
     *
     * @param setupTransformer the setup transformer.
     * @return the operation result.
     */
    public XSLTAttributesCoercion setupTransformer(SAXTransformer.SetupTransformer setupTransformer) {
        setSetupTransformer(setupTransformer);
        return this;
    }

    /**
     * Executes the coerce operation.
     *
     * @param attrs    the attrs.
     * @param modified the modified.
     * @throws Exception if the operation cannot be completed.
     */
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
