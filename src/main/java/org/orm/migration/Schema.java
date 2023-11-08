package org.orm.migration;

import org.orm.connection.PostgresSQL;
import org.orm.migration.annotations.Column;
import org.orm.migration.annotations.OneToOne;
import org.orm.migration.annotations.PrimaryKey;
import org.orm.migration.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Schema {

    private static Schema schema;

    private Schema() {

    }

    public static Schema getInstance() {
        if (schema == null) {
            schema = new Schema();
        }
        return schema;
    }

    /**
     * Get the class name for table
     *
     * @param o Object
     * @return String
     */
    private String getClassNameForTable(Object o) {
        if (o.getClass().isAnnotationPresent(Table.class) && !o.getClass().getAnnotation(Table.class).name().isEmpty()) {
            return o.getClass().getAnnotation(Table.class).name();
        }
        String[] classname = o.getClass().getName().split("\\.");
        return classname[classname.length - 1].toLowerCase();
    }

    /**
     * Get fields and it's type.
     *
     * @param o Object
     * @return String
      */
    private String getFieldNameAndType(Object o) {
        StringBuilder sb = new StringBuilder("\n ");

        Field[] fields = o.getClass().getDeclaredFields();

        for (Field field : fields) {
            sb.append(getFieldName(field));
            sb.append(getType(field)).append(", ");
        }
        int index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);

        return sb.toString();
    }

    /**
     * Returns the primary keys fields.
     *
     * @param o Object
     * @return String
     */
    private String getPrimaryKeyFields(Object o) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = o.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).name().isEmpty()) {
                    sb.append(field.getAnnotation(Column.class).name()).append(",");
                    continue;
                }
                sb.append(field.getName()).append(",");
            }
        }
        if (sb.toString().contains(",")) {
            int index = sb.lastIndexOf(",");
            sb.deleteCharAt(index);
        }

        return sb.toString();
    }

    public HashMap<String, String> getForeignTableAndColumns(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        HashMap<String, String> foreign = new HashMap<>();

        for(Field field : fields) {
            if (field.isAnnotationPresent(OneToOne.class) && !field.getAnnotation(OneToOne.class).column().isEmpty()) {
                Object object = field.getAnnotation(OneToOne.class).target();
                for (Field newField : object.getClass().getDeclaredFields()) {
                    if (newField.getName().equals(field.getAnnotation(OneToOne.class).column())) {
                        foreign.put(getClassNameForTable(object), getFieldName(newField));
                    }
                }
            }
        }
        return foreign;
    }
    /**
     * Create the insert SQL.
     *
     * @param o Object
     * @return String
     */
    public String getInsertSql(Object o) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = o.getClass().getDeclaredFields();

        sb.append(getClassNameForTable(o));
        sb.append("(");

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).name() != null) {
                if (field.getAnnotation(Column.class).unique()) {
                    sb.append(field.getAnnotation(Column.class).name()).append(" UNIQUE, ");
                } else {
                    sb.append(field.getAnnotation(Column.class).name()).append(",");
                }
            } else {
                sb.append(field.getName()).append(",");
            }
        }
        int index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);

        sb.append(") VALUES (");

        for (Field field : fields) {
            try {
                Method method;
                Object value;
                if (field.isAnnotationPresent(OneToOne.class) && !field.getAnnotation(OneToOne.class).column().isEmpty()) {
                    method = o.getClass().getMethod("get" + toUpperCamelCase(field.getName()));
                    Object object = method.invoke(o);
                    method = object.getClass().getMethod("get" + toUpperCamelCase(field.getAnnotation(OneToOne.class).column()));
                    value = method.invoke(object);
                } else {
                    method = o.getClass().getMethod("get" + toUpperCamelCase(field.getName()));
                    value = method.invoke(o);
                }
                if (value == null) {
                    sb.append("null");
                } else if (field.getType().getSimpleName().equals("String")) {
                    sb.append('\'').append(value).append('\'');
                } else {
                    sb.append(value);
                }
                sb.append(',');
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);
        sb.append(")");

        return sb.toString();
    }

    public String foreignKeysAlterTable(Object o) {
        StringBuilder sb = new StringBuilder();
        String tableName = getClassNameForTable(o);

        HashMap<String, String> foreign = getForeignTableAndColumns(o);
        for(Field field : o.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToOne.class)) {
                for (Map.Entry<String, String> entry : foreign.entrySet()) {
                    sb.append("ALTER TABLE ");
                    sb.append(tableName);

                    sb.append(" ADD CONSTRAINT ").append("FK_").append(tableName);
                    sb.append(" FOREIGN KEY ");

                    sb.append("(").append(field.getName()).append(")");
                    sb.append(" REFERENCES ").append(entry.getKey()).append("(").append(entry.getValue()).append(");");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns a String in UpperCamelCase
     *
     * @param input String
     * @return      String
     */
    private String toUpperCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            } else {
                capitalizeNext = true;
            }
        }

        return result.toString();
    }

    /**
     * Gets the value corresponding to the database
     *
     * @param field Field
     * @return String
     */
    private String getType(Field field) {
        StringBuilder sb = new StringBuilder();
        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).target() != void.class) {
            try {
                Object o = field.getAnnotation(OneToOne.class).target().getDeclaredConstructor().newInstance();
                for (Field field1 : o.getClass().getDeclaredFields()) {
                    if (field1.getName().equals(field.getAnnotation(OneToOne.class).column())) {
                        sb.append(DatabaseTypes.types.get(field1.getType().getSimpleName()));
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return sb.toString();
        } else if (field.getType().getSimpleName().equals("String")) {
            sb.append(DatabaseTypes.types.get(field.getType().getSimpleName()));
            if (field.getClass().isAnnotationPresent(Column.class)) {
                sb.append("(").append(field.getClass().getAnnotation(Column.class).length()).append(")");
            }
            return sb.toString();
        } else {
            sb.append(DatabaseTypes.types.get(field.getType().getSimpleName()));
            return sb.toString();
        }
    }

    /**
     * Get the field name based if it has or not an annotation
     * @param  field Field
     * @return String
     */
    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).name().isEmpty()) {
            return field.getAnnotation(Column.class).name() + " ";
        } else {
            return field.getName() + " ";
        }
    }

    /**
     * Create a table based on Model.
     *
     * @param o Object
     */
    public void create(Object o) {
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().createTableIfNotExists(getClassNameForTable(o) + "(" + getFieldNameAndType(o)));
        addPrimaryKey(o);
    }

    /**
     * Add primary key constraint.
     *
     * @param o Object
     */
    private void addPrimaryKey(Object o) {
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().alterTableAddPrimaryKeys(getClassNameForTable(o), getPrimaryKeyFields(o)));
    }

    public void addForeignKey(Object o) {
        PostgresSQL.getInstance().executeQuery(foreignKeysAlterTable(o));
    }

    /**
     * Drop the table.
     *
     * @param o Object
     */
    public void drop(Object o) {
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().dropTableIfExists(getClassNameForTable(o)));
    }

    /**
     * Save a new register in the database.
     *
     * @param o Object
     */
    public void save(Object o) {
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().save(getInsertSql(o)));
    }

    /**
     * Return all values from the database table.
     *
     * @param o Object
     * @return List<Object>
     */
    public List<Object> getAllFromSql(Object o) {
        ResultSet rs = PostgresSQL.getInstance().getAllFromSql(getClassNameForTable(o));
        List<Object> objects = new ArrayList<>();

        try {
            while (rs.next()) {
                Object object = o.getClass().getDeclaredConstructor().newInstance();
                Field[] fields = o.getClass().getDeclaredFields();

                for (Field field : fields) {
                    Method method = o.getClass().getMethod("set" + toUpperCamelCase(field.getName()), field.getType());

                    Object value;
                    if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).name() != null) {
                        value = rs.getObject(field.getAnnotation(Column.class).name());
                    } else {
                        value = rs.getObject(field.getName());
                    }

                    if (value != null) {
                        method.invoke(object, value);
                    }
                }
                objects.add(object);
            }
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return objects;
    }

    /**
     * Load a Model based on it's primary keys.
     *
     * @param o Object
     * @return Object
     */
    public Object getObjectFromKeys(Object o) {
        Object object;
        try {
            object = o.getClass().getDeclaredConstructor().newInstance();

            Field[] fields = o.getClass().getDeclaredFields();
            String[] fieldName = new String[fields.length];
            String[] values    = new String[fields.length];

            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isAnnotationPresent(PrimaryKey.class)) {
                    if (fields[i].isAnnotationPresent(Column.class) && !fields[i].getAnnotation(Column.class).name().isEmpty()) {
                        fieldName[i] = fields[i].getAnnotation(Column.class).name();
                    } else {
                        fieldName[i] = fields[i].getName();
                    }

                    Method method = o.getClass().getDeclaredMethod("get" + toUpperCamelCase(fields[i].getName()));

                    values[i] = String.valueOf(method.invoke(o));
                }
            }

            object = fetch(o, fieldName, values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    /**
     * Fetch a new Model from the database's return.
     *
     * @param o          Object
     * @param fieldNames String[]
     * @param values     Object[]
     * @return Object
     */
    private Object fetch(Object o, String[] fieldNames, Object[] values) {
        ResultSet rs = PostgresSQL.getInstance().getModelFromSql(getClassNameForTable(o), fieldNames, values);

        Object object = null;
        try {
            object = o.getClass().getConstructor().newInstance();
            try {
                while (rs.next()) {
                    Field[] fields = object.getClass().getDeclaredFields();

                    for (Field field : fields) {
                        Method method;
                        Object value;
                        if (field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).target() != void.class){
                            Object[] dbValue = {field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).name().isEmpty() ? rs.getObject(field.getAnnotation(Column.class).name()) : rs.getObject(field.getName())};
                            value = fetch(field.getAnnotation(OneToOne.class).target(), new String[]{field.getAnnotation(OneToOne.class).column().toString()}, dbValue);
                            method = object.getClass().getMethod("set" + toUpperCamelCase(field.getAnnotation(OneToOne.class).column()));

                        } else {
                            method = object.getClass().getMethod("set" + toUpperCamelCase(field.getName()), field.getType());
                            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).name() != null) {
                                value = rs.getObject(field.getAnnotation(Column.class).name());
                            }  else {
                                value = rs.getObject(field.getName());
                            }
                        }

                        if (value != null) {
                            method.invoke(object, value);
                        }
                    }
                }
            } catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return object;
    }

    /**
     * Verifies if a register exists in the database.
     *
     * @param o Object
     * @return boolean
     */
    public boolean existsById(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        Object[] values = new Object[fields.length];
        String[] fieldName = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(PrimaryKey.class)) {
                if (fields[i].isAnnotationPresent(Column.class) && !fields[i].getAnnotation(Column.class).name().isEmpty()) {
                    fieldName[i] = fields[i].getAnnotation(Column.class).name();
                } else {
                    fieldName[i] = fields[i].getName();
                }

                try {
                    Method method = o.getClass().getMethod("get" + toUpperCamelCase(fields[i].getName()));
                    values[i] = method.invoke(o);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            ResultSet rs = PostgresSQL.getInstance().existsByPrimaryKey(getClassNameForTable(o), fieldName, values);
            while (rs.next()) {
                return rs.getLong("COUNT") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Delete a register based on it's primary keys values.
     *
     * @param o Object
     * @return boolean
     */
    public boolean deleteById(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        Object[] values = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(PrimaryKey.class)) {
                fieldNames[i] = fields[i].getName();

                try {
                    Method method = o.getClass().getMethod("get" + toUpperCamelCase(fields[i].getName()));
                    values[i] = method.invoke(o);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return PostgresSQL.getInstance().delete(getClassNameForTable(o), fieldNames, values);
    }

    /**
     * Update a register in the database if it exists.
     *
     * @param o Object
     * @return boolean
     */
    public boolean update(Object o) {
        Object   object = getObjectFromKeys(o);
        Field[]  fields = o.getClass().getDeclaredFields();
        String[] fieldsToChange = new String[fields.length];
        Object[] values         = new String[fields.length];
        String[] PKFields       = new String[fields.length];
        Object[] PKvalues       = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {

            try {
                if (!fields[i].isAnnotationPresent(PrimaryKey.class)) {
                    Method method1;
                    if (fields[i].isAnnotationPresent(OneToOne.class) && fields[i].getAnnotation(OneToOne.class).target() != null) {
                        Object objeto = fields[i].getAnnotation(OneToOne.class).target();
                        method1 = objeto.getClass().getMethod("get" + toUpperCamelCase(fields[i].getAnnotation(OneToOne.class).column()));
                    } else {
                        method1 = o.getClass().getMethod("get" + toUpperCamelCase(fields[i].getName()));
                    }
                    Method method2 = object.getClass().getMethod("get" + toUpperCamelCase(fields[i].getName()));

                    Object value1  = method1.invoke(o);
                    Object value2  = method2.invoke(object);

                    if (value1 != value2 && value1 != null && value2 != null && !value1.equals("'null'") && !value2.equals("'null'")) {
                        if (fields[i].isAnnotationPresent(Column.class) && fields[i].getAnnotation(Column.class).name() != null) {
                            fieldsToChange[i] = fields[i].getAnnotation(Column.class).name();
                        } else {
                            fieldsToChange[i] = fields[i].getName();
                        }
                        if (fields[i].getType().getSimpleName().equals("String")) {
                            values[i] = "'" + value1 + "'";
                        } else {
                            values[i] = String.valueOf(value1);
                        }
                    }
                } else {
                    Object value = o.getClass().getMethod("get" + toUpperCamelCase(fields[i].getName())).invoke(o);
                    if (value != null) {
                        PKvalues[i] = String.valueOf(value);
                        if (fields[i].isAnnotationPresent(Column.class) && !fields[i].getAnnotation(Column.class).name().isEmpty()) {
                            PKFields[i] = fields[i].getAnnotation(Column.class).name();
                        } else {
                            PKFields[i] = fields[i].getName();
                        }
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return PostgresSQL.getInstance().update(getClassNameForTable(o), fieldsToChange, values, PKFields, PKvalues);
    }
}
