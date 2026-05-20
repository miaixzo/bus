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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The discussion class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Discussion implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251836760L;

    private String id;
    private Boolean individualNote;
    private List<Note> notes;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public String getId() {
        return id;
    }

    /**
     * Returns the individual note.
     *
     * @return the result
     */

    public Boolean getIndividualNote() {
        return individualNote;
    }

    /**
     * Returns the notes.
     *
     * @return the result
     */

    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the individual note.
     *
     * @param individualNote the individual note value
     */

    public void setIndividualNote(Boolean individualNote) {
        this.individualNote = individualNote;
    }

    /**
     * Sets the notes.
     *
     * @param notes the notes value
     */

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
