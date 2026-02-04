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
package org.miaixz.bus.image.galaxy.data;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Abstract base class for DICOM Element Dictionaries. Implementations provide mappings between DICOM tags, their Value
 * Representations (VRs), and keywords. This class supports both standard and private DICOM elements.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ElementDictionary {

    /**
     * Service loader for discovering {@code ElementDictionary} implementations.
     */
    private static final ServiceLoader<ElementDictionary> loader = ServiceLoader.load(ElementDictionary.class);
    /**
     * Cache for {@code ElementDictionary} instances, mapped by private creator ID.
     */
    private static final Map<String, ElementDictionary> map = new HashMap<>();
    /**
     * The private creator ID associated with this dictionary, or {@code null} for the standard dictionary.
     */
    private final String privateCreator;
    /**
     * The class containing static final fields for DICOM tags, used for keyword lookup.
     */
    private final Class<?> tagClass;

    /**
     * Constructs an {@code ElementDictionary} with the specified private creator and tag class.
     * 
     * @param privateCreator The private creator ID, or {@code null} for the standard dictionary.
     * @param tagClass       The class containing static final fields for DICOM tags.
     */
    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    /**
     * Returns the standard DICOM Element Dictionary.
     * 
     * @return The standard {@code ElementDictionary} instance.
     */
    public static ElementDictionary getStandardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    /**
     * Returns the {@code ElementDictionary} for the given private creator ID. If no specific dictionary is found, the
     * standard dictionary is returned. Dictionaries are loaded via {@link ServiceLoader}.
     * 
     * @param privateCreator The private creator ID, or {@code null} for the standard dictionary.
     * @return The appropriate {@code ElementDictionary} instance.
     */
    public static ElementDictionary getElementDictionary(String privateCreator) {
        if (privateCreator != null) {
            ElementDictionary dict1 = map.get(privateCreator);
            if (dict1 != null)
                return dict1;
            if (!map.containsKey(privateCreator))
                synchronized (loader) {
                    for (ElementDictionary dict : loader) {
                        map.putIfAbsent(dict.getPrivateCreator(), dict);
                        if (privateCreator.equals(dict.getPrivateCreator()))
                            return dict;
                    }
                    map.put(privateCreator, null);
                }
        }
        return getStandardElementDictionary();
    }

    /**
     * Reloads all {@code ElementDictionary} implementations using the {@link ServiceLoader}. This clears the internal
     * cache and re-discovers available dictionaries.
     */
    public static void reload() {
        synchronized (loader) {
            loader.reload();
        }
    }

    /**
     * Returns the Value Representation (VR) for the given DICOM tag and private creator.
     * 
     * @param tag            The DICOM tag.
     * @param privateCreator The private creator ID, or {@code null} for standard elements.
     * @return The {@link VR} for the specified tag.
     */
    public static VR vrOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).vrOf(tag);
    }

    /**
     * Returns the keyword for the given DICOM tag and private creator.
     * 
     * @param tag            The DICOM tag.
     * @param privateCreator The private creator ID, or {@code null} for standard elements.
     * @return The keyword for the specified tag.
     */
    public static String keywordOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).keywordOf(tag);
    }

    /**
     * Returns the DICOM tag for the given keyword and private creator ID.
     * 
     * @param keyword          The keyword to look up.
     * @param privateCreatorID The private creator ID, or {@code null} for standard elements.
     * @return The DICOM tag, or -1 if not found.
     */
    public static int tagForKeyword(String keyword, String privateCreatorID) {
        return getElementDictionary(privateCreatorID).tagForKeyword(keyword);
    }

    /**
     * Returns the private creator ID associated with this dictionary.
     * 
     * @return The private creator ID, or {@code null} if this is the standard dictionary.
     */
    public final String getPrivateCreator() {
        return privateCreator;
    }

    /**
     * Abstract method to be implemented by concrete dictionary classes to return the Value Representation (VR) for a
     * given DICOM tag.
     * 
     * @param tag The DICOM tag.
     * @return The {@link VR} for the specified tag.
     */
    public abstract VR vrOf(int tag);

    /**
     * Abstract method to be implemented by concrete dictionary classes to return the keyword for a given DICOM tag.
     * 
     * @param tag The DICOM tag.
     * @return The keyword for the specified tag.
     */
    public abstract String keywordOf(int tag);

    /**
     * Returns the tag of the corresponding Time (TM) attribute for a given Date (DA) attribute tag. This is typically
     * used for combined Date-Time attributes.
     * 
     * @param daTag The tag of the Date (DA) attribute.
     * @return The tag of the corresponding Time (TM) attribute, or 0 if not applicable.
     */
    public int tmTagOf(int daTag) {
        return 0;
    }

    /**
     * Returns the tag of the corresponding Date (DA) attribute for a given Time (TM) attribute tag. This is typically
     * used for combined Date-Time attributes.
     * 
     * @param tmTag The tag of the Time (TM) attribute.
     * @return The tag of the corresponding Date (DA) attribute, or 0 if not applicable.
     */
    public int daTagOf(int tmTag) {
        return 0;
    }

    /**
     * Returns the DICOM tag for a given keyword. This method uses reflection on the {@code tagClass} to find a static
     * final field matching the keyword.
     * 
     * @param keyword The keyword to look up.
     * @return The DICOM tag, or -1 if not found.
     */
    public int tagForKeyword(String keyword) {
        if (tagClass != null)
            try {
                return tagClass.getField(keyword).getInt(null);
            } catch (Exception ignore) {
            }
        return -1;
    }

}
