package net.petercashel.monetaryexchange.database.abstractions;

import net.petercashel.monetaryexchange.database.annotations.*;
import org.sql2o.Connection;
import org.sql2o.Query;

import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class Mapper {

    public static <T> Map<String, DBField> GetTableMapping(Class<T> clazz, boolean IncludeKey) {
        Map<String, DBField> fieldColumnMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DBField.class)) {
                if (field.isAnnotationPresent(DBKey.class) && !IncludeKey) {
                    continue;
                }
                DBField fieldAnnot = field.getAnnotation(DBField.class);
                fieldColumnMap.put(field.getName(), fieldAnnot);
            }
        }


        return fieldColumnMap;
    }


    public static <T> ArrayList<TableConstraint> GetTableConstraints(Class<T> clazz) {
        ArrayList<TableConstraint> constraints = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DBForeignKey.class)) {
                if (field.isAnnotationPresent(DBField.class)) {
                    DBTable tableAnnot =  clazz.getAnnotation(DBTable.class);
                    DBField fieldAnnot = field.getAnnotation(DBField.class);
                    DBForeignKey foreignAnnot = field.getAnnotation(DBForeignKey.class);
                    constraints.add(
                            new TableConstraint(
                                    "FK_" + tableAnnot.TableName() + "_" + foreignAnnot.ForeignTableName(),
                                    fieldAnnot.ColumnName(),
                                    foreignAnnot.ForeignTableName(),
                                    foreignAnnot.ForeignColumnName()
                            )
                    );
                }
            }
        }


        return constraints;
    }

    public static <T> DBKey GetTableIDKey(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DBKey.class)) {
                if (field.isAnnotationPresent(DBField.class)) {
                    return field.getAnnotation(DBKey.class);
                }
            }
        }

        return null;
    }

    public static <T> DBField GetTableIDMapping(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            //field.setAccessible(true);
            if (field.isAnnotationPresent(DBKey.class)) {
                if (field.isAnnotationPresent(DBField.class)) {
                    return field.getAnnotation(DBField.class);
                }
            }
        }

        return null;
    }

    public static <T> String GetTableName(Class<T> clazz) {
        if (clazz.isAnnotationPresent(DBTable.class)) {
            return clazz.getAnnotation(DBTable.class).TableName();
        }
        return "tbl_autogen_" + clazz.getSimpleName();
    }

    public static <T> Query GetInsertQuery(Class<T> clazz, Connection con) {
        Map<String, DBField> Mappings = Mapper.GetTableMapping(clazz, false);
        String tableName = Mapper.GetTableName(clazz);
        StringBuilder insert = new StringBuilder("insert into " + tableName + "(");
        StringBuilder values = new StringBuilder("values (");

        boolean first = false;

        Mappings.forEach((strClassFieldName, dbFieldMapping) -> {
            insert.append(dbFieldMapping.ColumnName() + ", ");
            values.append(":" + strClassFieldName + ", ");
        });

        // Give the parameters the same names as the corresponding properties in your model class
        String sql = insert.toString().trim();
        sql = sql.substring(0, sql.length() - 1) + ") " + values.toString().trim();
        sql = sql.substring(0, sql.length() - 1) + ")";

        return con.createQuery(sql, true);
    }

    public static <U, T> U GetKey(T obj, Class<U> clazzKey) {
        Class<T> clazz = (Class<T>) obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            //field.setAccessible(true);
            if (field.isAnnotationPresent(DBKey.class)) {
                if (field.isAnnotationPresent(DBField.class)) {
                    DBKey keyAnnot = field.getAnnotation(DBKey.class);
                    DBField fieldAnnot = field.getAnnotation(DBField.class);
                    if (keyAnnot.Primary()) {
                        field.setAccessible(true);
                        try {
                            return (U) field.get(obj);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static <T> Query ProcessWhere(Class<T> clazz, Connection con, String queryText, Consumer<Where> whereConsumer) {
        Map<String, DBField> Mappings = Mapper.GetTableMapping(clazz, false);
        Where w = new Where(queryText, Mappings);
        whereConsumer.accept(w);

        return w.GetQuery(con);
    }

    public static <T> Query AddMappings(Class<T> clazz, Query q) {
        Map<String, DBField> Mappings = Mapper.GetTableMapping(clazz, false);

        //I hate this, but my background is C# and this works differently.
        final Query[] localQ = {q};
        Mappings.forEach((strClassFieldName, dbFieldMapping) -> {
            localQ[0] = localQ[0].addColumnMapping(strClassFieldName, dbFieldMapping.ColumnName());
        });

        return localQ[0];
    }

    public static class Where {
        private final Map<String, DBField> Mappings;
        private final String QueryText;
        StringBuilder WhereClause;

        private final Map<String, Object> Parameters;

        public Where(String queryText, Map<String, DBField> mappings) {
            QueryText = queryText;
            WhereClause = new StringBuilder();
            Mappings = mappings;
            Parameters = new HashMap<>();
        }

        public org.sql2o.Query GetQuery(Connection con) {
            Query q = con.createQuery(QueryText.trim() + " WHERE" + WhereClause.toString());

            final Query[] localQ = {q};
            Parameters.forEach((strClassFieldName, paramValue) -> {
                localQ[0] = localQ[0].addParameter(strClassFieldName, paramValue);
            });

            return localQ[0];
        }

        private void AddWhere(String ClassField, String Operator, boolean isOr) {
            String dbField = Mappings.get(ClassField).ColumnName();
            if (WhereClause.length() > 3) {
                if (isOr) {
                    WhereClause.append(" OR");
                } else {
                    WhereClause.append(" AND");
                }
            }
            WhereClause.append(" ").append(dbField).append(" ").append(Operator).append(" :").append(ClassField);
        }

        public void OpenWhereGroup() {
            WhereClause.append("(");
        }
        public void CloseWhereGroup() {
            WhereClause.append(")");
        }

        public void NotEquals(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, "<>", isOr);
            Parameters.put(ClassField, value);
        }
        public void Equals(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, "=", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThan(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, ">", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThan(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, "<", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThanEquals(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, ">=", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThanEquals(String ClassField, Object value, boolean isOr) {
            AddWhere(ClassField, "<=", isOr);
            Parameters.put(ClassField, value);
        }


        public void NotEquals(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, "<>", isOr);
            Parameters.put(ClassField, value);
        }
        public void Equals(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, "=", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThan(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, ">", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThan(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, "<", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThanEquals(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, ">=", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThanEquals(String ClassField, String value, boolean isOr) {
            AddWhere(ClassField, "<=", isOr);
            Parameters.put(ClassField, value);
        }


        public void NotEquals(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, "<>", isOr);
            Parameters.put(ClassField, value);
        }
        public void Equals(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, "=", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThan(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, ">", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThan(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, "<", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThanEquals(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, ">=", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThanEquals(String ClassField, int value, boolean isOr) {
            AddWhere(ClassField, "<=", isOr);
            Parameters.put(ClassField, value);
        }


        public void NotEquals(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, "<>", isOr);
            Parameters.put(ClassField, value);
        }
        public void Equals(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, "=", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThan(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, ">", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThan(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, "<", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThanEquals(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, ">=", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThanEquals(String ClassField, long value, boolean isOr) {
            AddWhere(ClassField, "<=", isOr);
            Parameters.put(ClassField, value);
        }


        public void NotEquals(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, "<>", isOr);
            Parameters.put(ClassField, value);
        }
        public void Equals(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, "=", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThan(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, ">", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThan(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, "<", isOr);
            Parameters.put(ClassField, value);
        }
        public void GreaterThanEquals(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, ">=", isOr);
            Parameters.put(ClassField, value);
        }
        public void LessThanEquals(String ClassField, LocalDateTime value, boolean isOr) {
            AddWhere(ClassField, "<=", isOr);
            Parameters.put(ClassField, value);
        }
    }
}
