package com.cloudogu.scmmanager.scm.api;

import java.util.Date;
import java.util.Objects;

public class Changeset {

    private String id;
    private Date date;

    Changeset() {}

    public Changeset(String id, Date date) {
        this.id = id;
        this.date = new Date(date.getTime());
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Changeset changeset = (Changeset) o;
        return Objects.equals(id, changeset.id) && Objects.equals(date, changeset.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date);
    }
}
