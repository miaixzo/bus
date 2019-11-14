package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.List;

public class Discussion {

    private String id;
    private Boolean individualNote;
    private List<org.aoju.bus.gitlab.models.Note> notes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIndividualNote() {
        return individualNote;
    }

    public void setIndividualNote(Boolean individualNote) {
        this.individualNote = individualNote;
    }

    public List<org.aoju.bus.gitlab.models.Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
