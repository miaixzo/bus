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
package org.miaixz.bus.image.nimble.extend;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.miaixz.bus.image.builtin.ldap.LdapBuilder;
import org.miaixz.bus.image.builtin.ldap.LdapDicomConfiguration;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;
import org.miaixz.bus.image.nimble.codec.CompressionRule;
import org.miaixz.bus.image.nimble.codec.CompressionRules;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapCompressionRulesConfiguration {

    private final LdapDicomConfiguration config;

    public LdapCompressionRulesConfiguration(LdapDicomConfiguration config) {
        this.config = config;
    }

    private static Attributes storeTo(CompressionRule rule, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmCompressionRule");
        attrs.put("cn", rule.getCommonName());
        LdapBuilder.storeNotEmpty(attrs, "dcmPhotometricInterpretation", rule.getPhotometricInterpretations());
        LdapBuilder.storeNotEmpty(attrs, "dcmBitsStored", rule.getBitsStored());
        LdapBuilder.storeNotDef(attrs, "dcmPixelRepresentation", rule.getPixelRepresentation(), -1);
        LdapBuilder.storeNotEmpty(attrs, "dcmAETitle", rule.getAETitles());
        LdapBuilder.storeNotEmpty(attrs, "dcmSOPClass", rule.getSOPClasses());
        LdapBuilder.storeNotEmpty(attrs, "dcmBodyPartExamined", rule.getBodyPartExamined());
        attrs.put("dicomTransferSyntax", rule.getTransferSyntax());
        LdapBuilder.storeNotEmpty(attrs, "dcmImageWriteParam", rule.getImageWriteParams());
        return attrs;
    }

    public void store(CompressionRules rules, String parentDN) throws NamingException {
        for (CompressionRule rule : rules)
            config.createSubcontext(LdapBuilder.dnOf("cn", rule.getCommonName(), parentDN),
                    storeTo(rule, new BasicAttributes(true)));
    }

    public void load(CompressionRules rules, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne = config.search(dn, "(objectclass=dcmCompressionRule)");
        try {
            while (ne.hasMore())
                rules.add(compressionRule(ne.next().getAttributes()));
        } finally {
            LdapBuilder.safeClose(ne);
        }
    }

    private CompressionRule compressionRule(Attributes attrs) throws NamingException {
        return new CompressionRule(LdapBuilder.stringValue(attrs.get("cn"), null),
                LdapBuilder.stringArray(attrs.get("dcmPhotometricInterpretation")),
                LdapBuilder.intArray(attrs.get("dcmBitsStored")),
                LdapBuilder.intValue(attrs.get("dcmPixelRepresentation"), -1),
                LdapBuilder.stringArray(attrs.get("dcmAETitle")), LdapBuilder.stringArray(attrs.get("dcmSOPClass")),
                LdapBuilder.stringArray(attrs.get("dcmBodyPartExamined")),
                LdapBuilder.stringValue(attrs.get("dicomTransferSyntax"), null),
                LdapBuilder.stringArray(attrs.get("dcmImageWriteParam")));
    }

    public void merge(ConfigurationChanges diffs, CompressionRules prevRules, CompressionRules rules, String parentDN)
            throws NamingException {
        for (CompressionRule prevRule : prevRules) {
            String cn = prevRule.getCommonName();
            if (rules == null || rules.findByCommonName(cn) == null) {
                String dn = LdapBuilder.dnOf("cn", cn, parentDN);
                config.destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (CompressionRule rule : rules) {
            String cn = rule.getCommonName();
            String dn = LdapBuilder.dnOf("cn", cn, parentDN);
            CompressionRule prevRule = prevRules != null ? prevRules.findByCommonName(cn) : null;
            if (prevRule == null) {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, dn,
                        ConfigurationChanges.ChangeType.C);
                config.createSubcontext(dn, storeTo(rule, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges.addModifiedObject(diffs, dn,
                        ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn, storeDiffs(ldapObj, prevRule, rule, new ArrayList<>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, CompressionRule prev,
            CompressionRule rule, List<ModificationItem> mods) {
        LdapBuilder.storeDiff(ldapObj, mods, "dcmPhotometricInterpretation", prev.getPhotometricInterpretations(),
                rule.getPhotometricInterpretations());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmBitsStored", prev.getBitsStored(), rule.getBitsStored(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmPixelRepresentation", prev.getPixelRepresentation(),
                rule.getPixelRepresentation(), -1);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAETitle", prev.getAETitles(), rule.getAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmSOPClass", prev.getSOPClasses(), rule.getSOPClasses());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmBodyPartExamined", prev.getBodyPartExamined(),
                rule.getBodyPartExamined());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dicomTransferSyntax", prev.getTransferSyntax(),
                rule.getTransferSyntax(), null);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmImageWriteParam", prev.getImageWriteParams(),
                rule.getImageWriteParams());
        return mods;
    }

}
