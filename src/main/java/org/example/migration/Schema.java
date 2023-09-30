package org.example.migration;

import org.example.connection.PostgresSQL;
import org.example.migration.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    public String getClassNameForTable(Object o) {
        String[] classname = o.getClass().getName().split("\\.");
        return classname[classname.length - 1].toLowerCase();
    }

    public String getFieldAndType(Object o) {
        StringBuilder sb = new StringBuilder("\n ");

        Field[] fields = o.getClass().getDeclaredFields();

        for(Field field : fields) {
            sb.append(field.getName()).append(" ");
            sb.append(getType(field)).append(", ");
        }
        int index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);

        return sb.toString();
    }

    public String getPrimaryKeyFields(Object o) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = o.getClass().getDeclaredFields();

        for(Field field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)){
                sb.append(field.getName()).append(",");
            }
        }
        int index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);

        return sb.toString();
    }

    public String getInsertSql(Object o) throws NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        Field[] fields   = o.getClass().getDeclaredFields();

        sb.append(getClassNameForTable(o));
        sb.append("(");

        for(Field field : fields) {
            sb.append(field.getName()).append(",");
        }
        int index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);

        sb.append(") VALUES (");

        for(Field field : fields) {
            Class<?> clazz = o.getClass();

            Method method = clazz.getMethod("get" + toUpperCamelCase(field.getName()));
            try {
                Object value = method.invoke(o);
                if (value == null) {
                    sb.append("null");
                }
                else if (field.getType().getSimpleName().equals("String")) {
                    sb.append('\'').append(method.invoke(o)). append('\'');
                } else {
                    sb.append(method.invoke(o));
                }
                sb.append(',');
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        index = sb.lastIndexOf(",");
        sb.deleteCharAt(index);
        sb.append(")");

        return sb.toString();
    }

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

    public String getType(Field field) {
        return DatabaseTypes.types.get(field.getType().getSimpleName());
    }

    public void create(Object o){
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().createTableIfNotExists( getClassNameForTable(o) + "(" + getFieldAndType(o)));
        addPrimaryKey(o);
    }

    public void addPrimaryKey(Object o){
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().alterTableAddPrimaryKeys(getClassNameForTable(o), getPrimaryKeyFields(o)));
    }

    public void drop(Object o) {
        PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().dropTableIfExists(getClassNameForTable(o)));
    }

    public void save(Object o) {
        try {
            PostgresSQL.getInstance().executeQuery(PostgresSQL.getInstance().save(getInsertSql(o)));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object> getAllFromSql(Object o) {
        ResultSet rs = PostgresSQL.getInstance().getAllFromSql(getClassNameForTable(o));
        List<Object> objects = new ArrayList<>();

        try {
            while (rs.next()) {
                Object object = o.getClass().getDeclaredConstructor().newInstance();
                Field[] fields = o.getClass().getDeclaredFields();

                for (Field field : fields) {
                    Method method = o.getClass().getMethod("set" +  toUpperCamelCase(field.getName()), field.getType());
                    Object value = rs.getObject(field.getName());

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

    public Object getObjectFromKeys(Object o) {
        Object object = null;
        try {
            object = o.getClass().getDeclaredConstructor().newInstance();


        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return object;
    }
}
