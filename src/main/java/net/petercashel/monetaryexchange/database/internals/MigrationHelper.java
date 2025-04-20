package net.petercashel.monetaryexchange.database.internals;

import net.petercashel.monetaryexchange.database.abstractions.Mapper;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

public class MigrationHelper {
    private final Sql2o _backend;

    public MigrationHelper(Sql2o instance) {
        _backend = instance;
    }

    //
    // Useful docs for writing this together. Need to support H2 as well.
    //
    // https://dev.mysql.com/doc/refman/8.4/en/alter-table.html
    // https://www.h2database.com/html/commands.html#alter_table_add
    //


    private boolean ExecuteCommand(String Commandtext) {
        try (Connection con = _backend.beginTransaction()) {
            Query q = con.createQuery(Commandtext);
            Connection c = q.executeUpdate();
            int result = c.getResult();
            con.commit();
            return result > 0;
        }
    }

    public <T> boolean AddColumn(Class<T> clazz, String columnName, String columnType) {
        String alter = "ALTER TABLE "+ Mapper.GetTableName(clazz) +"\n" +
                "ADD COLUMN " +  columnName + " " + columnType + ";";
        return ExecuteCommand(alter);
    }

    public <T> boolean AddColumn(Class<T> clazz, String columnName, String columnType, String afterColumn) {
        String alter = "ALTER TABLE "+ Mapper.GetTableName(clazz) +"\n" +
                "ADD COLUMN " + columnName + " " + columnType + " AFTER " + afterColumn + ";";
        return ExecuteCommand(alter);
    }

    public <T> boolean DropColumn(Class<T> clazz, String columnName) {
        String alter = "ALTER TABLE "+ Mapper.GetTableName(clazz) +"\n" +
                "DROP COLUMN " +  columnName + ";";
        return ExecuteCommand(alter);
    }
}
