package net.petercashel.monetaryexchange.database.abstractions;

import net.petercashel.monetaryexchange.MonetaryExchange;
import net.petercashel.monetaryexchange.database.annotations.ColumnDataTypeEnum;
import net.petercashel.monetaryexchange.database.annotations.DBField;
import net.petercashel.monetaryexchange.database.annotations.DBKey;
import net.petercashel.monetaryexchange.database.annotations.TableConstraint;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class Manager<T,U> {
    private final Sql2o backend;
    private final Class<T> Clazz;
    private final Class<U> ClazzKey;

    public Manager(Class<T> clazz, Class<U> clazzKey) {
        backend = MonetaryExchange.Sql2o_Instance;
        Clazz = clazz;
        ClazzKey = clazzKey;
    }
    public Manager(Class<T> clazz, Class<U> clazzKey, Sql2o backend) {
        this.backend = backend;
        Clazz = clazz;
        ClazzKey = clazzKey;
    }

    public boolean Exists(U ID) {
        String tableName = Mapper.GetTableName(Clazz);
        String idColumn = Objects.requireNonNull(Mapper.GetTableIDMapping(Clazz)).ColumnName();

        String command = "SELECT EXISTS(SELECT 1 FROM " + tableName + " WHERE " + idColumn + " = :idParam LIMIT 1)";

        try (Connection con = backend.beginTransaction()) {
            if (MonetaryExchange.DBIsEmbedded) {
                boolean result = con.createQuery(command)
                        .addParameter("idParam", ID)
                        .executeScalar(boolean.class);
                con.commit();
                return result;
            } else {
                int result = con.createQuery(command)
                        .addParameter("idParam", ID)
                        .executeScalar(Integer.class);
                con.commit();
                return result == 1;
            }
        }
    }

    public boolean Delete(U ID) {
        String tableName = Mapper.GetTableName(Clazz);
        String idColumn = Objects.requireNonNull(Mapper.GetTableIDMapping(Clazz)).ColumnName();

        String command = "DELETE FROM " + tableName + " WHERE " + idColumn + " = :idParam";

        try (Connection con = backend.beginTransaction()) {
            int result = con.createQuery(command)
                    .addParameter("idParam", ID)
                    .executeUpdate()
                    .getResult();
            con.commit();
            return result == 1;
        }
    }

    public List<T> Find(Consumer<Mapper.Where> whereConsumer) {
        String tableName = Mapper.GetTableName(Clazz);

        String sql =
                "SELECT * " +
                "FROM " + tableName + " ";

        try (Connection con = backend.beginTransaction()) {
            Query q = Mapper.ProcessWhere(Clazz, con, sql, whereConsumer);
            q = Mapper.AddMappings(Clazz, q);

            List<T> result = q.executeAndFetch(Clazz);
            con.commit();
            return result;
        }
    }

    public Optional<T> FindOne(Consumer<Mapper.Where> whereConsumer)
    {
        return Find(whereConsumer).stream().findFirst();
    }

    public U Insert(T obj) {
        try (Connection con = backend.beginTransaction()) {
            U key = Mapper.GetInsertQuery(Clazz,con).bind(obj).executeUpdate().getKey(ClazzKey);
            con.commit();
            return key;
        }
    }

    public boolean Update(T obj) {
        String tableName = Mapper.GetTableName(Clazz);
        String idColumn = Objects.requireNonNull(Mapper.GetTableIDMapping(Clazz)).ColumnName();
        Map<String, DBField> Mappings = Mapper.GetTableMapping(Clazz, false);
        U key = Mapper.GetKey(obj, ClazzKey);

        String updateSql = "update "+ tableName + " set ";
        boolean first = true;

        for (Map.Entry<String, DBField> entry : Mappings.entrySet()) {
            if (!first) {
                updateSql += ", ";
            } else {
                first = false;
            }
            updateSql += (entry.getValue().ColumnName() + " = :" + entry.getKey());
        }

        updateSql += " where " + idColumn + " = :idParam";

        try (Connection con = backend.beginTransaction()) {
            Query q = con.createQuery(updateSql);

            for (Field field : Clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(DBField.class) && !field.isAnnotationPresent(DBKey.class)) {
                    try {
                        q = q.addParameter(field.getName(), field.get(obj));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            q = q.addParameter("idParam", key);

            int result = q.executeUpdate().getResult();
            con.commit();
            return result > 0;
        }

    }

    /*
    * Done
    * Exists
    * Delete (by id)
    * Find ???
    * Find First ???
    * Insert
    * Update
    *
    * CreateTable
    * TableExists
    *
    */

    public boolean CreateTable() {
        String tableName = Mapper.GetTableName(Clazz);
        DBKey idColumnKey = Objects.requireNonNull(Mapper.GetTableIDKey(Clazz));
        DBField idColumnField = Objects.requireNonNull(Mapper.GetTableIDMapping(Clazz));
        //Map<String, DBField> Mappings = Mapper.GetTableMapping(Clazz, false);
        TreeMap<String, DBField> Mappings = new TreeMap<>(Mapper.GetTableMapping(Clazz, false));
        ArrayList<TableConstraint> Constraints = Mapper.GetTableConstraints(Clazz);


        StringBuilder createQuery = new StringBuilder();
        createQuery.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

        createQuery.append("    ").append(idColumnField.ColumnName()).append(" ").append(GetDataType(idColumnField));
        if (!idColumnField.Nullable()) createQuery.append(" NOT NULL");
        if (idColumnKey.AutoIncrement()) createQuery.append(" AUTO_INCREMENT");
        createQuery.append(",\n");

        for (Map.Entry<String, DBField> entry : Mappings.entrySet()) {
            createQuery.append("    ").append(entry.getValue().ColumnName()).append(" ").append(GetDataType(entry.getValue()));
            if (!entry.getValue().Nullable()) createQuery.append(" NOT NULL");
            createQuery.append(",\n");
        }

        if (idColumnKey.AutoIncrement()) createQuery.append("    ").append("PRIMARY KEY (").append(idColumnField.ColumnName()).append(")\n");

        if (Constraints != null && !Constraints.isEmpty()) {
            for (TableConstraint c : Constraints) {
                createQuery.append("    ").append("CONSTRAINT ").append(c.ConstraintName()).append("\n");
                createQuery.append("    ").append("FOREIGN KEY (").append(c.LocalKey()).append(")\n");
                createQuery.append("    ").append("REFERENCES ").append(c.ForeignTable()).append("(").append(c.ForeignKey()).append(")\n");
            }
        }

        createQuery.append(");");


        try (Connection con = backend.beginTransaction()) {

            Query q = con.createQuery(createQuery.toString());

            int result = q.executeUpdate().getResult();
            con.commit();
            return TableExists();
        }
    }

    public boolean TableExists() {
        String tableName = Mapper.GetTableName(Clazz);
        String Commandtext = "SHOW TABLES LIKE '" + tableName + "';";

        if (MonetaryExchange.DBIsEmbedded) {
            Commandtext = "SELECT TABLE_NAME \n" +
                    "FROM information_schema.tables\n" +
                    "WHERE table_name = '" + tableName + "'\n" +
                    "LIMIT 1;";
        }

        try (Connection con = backend.beginTransaction()) {
            Query q = con.createQuery(Commandtext.toString());

            int result = q.executeScalarList(Objects.class).size();
            con.commit();
            return result > 0;
        }

    }

    private String GetDataType(DBField dbField) {
        switch (dbField.DataType()) {
            case INTEGER -> {
                return "INTEGER";
            }
            case LONG -> {
                return "BIGINT";
            }
            case VARCHAR -> {
                return "VARCHAR(" + dbField.MaxLength() + ")";
            }
            case BOOLEAN -> {
                return "BOOLEAN";
            }
            case DECIMAL -> {
                return "DECIMAL(" + dbField.Decimal_Precision() + "," + dbField.Decimal_Scale() + ")";
            }
            case LOCALDATETIME -> {
                return "TIMESTAMP";
            }
        }
        return "VARCHAR(255)";
    }
}
