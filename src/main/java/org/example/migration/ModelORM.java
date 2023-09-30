package org.example.migration;

public class ModelORM {

    public ModelORM() {

    }

    public void save() {
        Schema.getInstance().save(this);
    }

    public void create() {
        Schema.getInstance().create(this);
    }

    public void drop() {
        Schema.getInstance().drop(this);
    }

    public void getAllFromSql() {
        Schema.getInstance().getAllFromSql(this);
    }
}
