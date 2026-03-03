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
package org.miaixz.bus.image.metric.ldap;

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
            config.createSubcontext(
                    LdapBuilder.dnOf("cn", rule.getCommonName(), parentDN),
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
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges
                        .addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                config.createSubcontext(dn, storeTo(rule, new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj = ConfigurationChanges
                        .addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn, storeDiffs(ldapObj, prevRule, rule, new ArrayList<>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(
            ConfigurationChanges.ModifiedObject ldapObj,
            CompressionRule prev,
            CompressionRule rule,
            List<ModificationItem> mods) {
        LdapBuilder.storeDiff(
                ldapObj,
                mods,
                "dcmPhotometricInterpretation",
                prev.getPhotometricInterpretations(),
                rule.getPhotometricInterpretations());
        LdapBuilder.storeDiffObject(ldapObj, mods, "dcmBitsStored", prev.getBitsStored(), rule.getBitsStored(), null);
        LdapBuilder.storeDiff(
                ldapObj,
                mods,
                "dcmPixelRepresentation",
                prev.getPixelRepresentation(),
                rule.getPixelRepresentation(),
                -1);
        LdapBuilder.storeDiff(ldapObj, mods, "dcmAETitle", prev.getAETitles(), rule.getAETitles());
        LdapBuilder.storeDiff(ldapObj, mods, "dcmSOPClass", prev.getSOPClasses(), rule.getSOPClasses());
        LdapBuilder.storeDiff(
                ldapObj,
                mods,
                "dcmBodyPartExamined",
                prev.getBodyPartExamined(),
                rule.getBodyPartExamined());
        LdapBuilder.storeDiffObject(
                ldapObj,
                mods,
                "dicomTransferSyntax",
                prev.getTransferSyntax(),
                rule.getTransferSyntax(),
                null);
        LdapBuilder
                .storeDiff(ldapObj, mods, "dcmImageWriteParam", prev.getImageWriteParams(), rule.getImageWriteParams());
        return mods;
    }

}
