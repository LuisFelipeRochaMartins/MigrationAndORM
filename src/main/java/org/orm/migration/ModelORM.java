package org.orm.migration;

import java.util.List;

public class ModelORM {

    public ModelORM() {

    }

    /**
     * Create table.
     */
    public void create() {
        Schema.getInstance().create(this);
    }

    /**
     * Drop Table.
     */
    public void drop() {
        Schema.getInstance().drop(this);
    }

    /**
     * Returns the full value of the table.
     *
     * @return List<Object>
     */
    public List<Object> getAllFromSql() {
        return Schema.getInstance().getAllFromSql(this);
    }

    /**
     * Returns the object based on the Model primary keys.
     *
     * @return Object
     */
    public Object refresh() {
        return Schema.getInstance().getObjectFromKeys(this);
    }
    /**
     * Save the current Model in the table.
     */
    public void save() {
        if (exists()) {
            return;
        }
        Schema.getInstance().save(this);
    }

    /**
     * Verifies if the register exists based on Model primary keys.
     *
     * @return boolean
     */
    public boolean exists(){
        return Schema.getInstance().existsById(this);
    }

    /**
     * Delete a register of the database.
     *
     * @return boolean
     */
    public boolean delete() {
        if (exists()) {
            Schema.getInstance().deleteById(this);
            return true;
        }
        return false;
    }

    /**
     * Updates a register in the database.
     *
     * @return boolean
     */
    public boolean update() {
        if (exists()) {
            Schema.getInstance().update(this);
            return true;
        }
        return false;
    }
}
