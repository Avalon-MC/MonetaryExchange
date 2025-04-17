package net.petercashel.monetaryexchange.database.entities;

import net.petercashel.monetaryexchange.database.annotations.ColumnDataTypeEnum;
import net.petercashel.monetaryexchange.database.annotations.DBField;
import net.petercashel.monetaryexchange.database.annotations.DBKey;
import net.petercashel.monetaryexchange.database.annotations.DBTable;

import java.util.UUID;

@DBTable(TableName = "tbl_PlayerProfiles")
public class PlayerProfile {

    @DBKey
    @DBField(ColumnName = "id", DataType = ColumnDataTypeEnum.INTEGER)
    public int ID;

    @DBField(ColumnName = "gameProfileID", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 48)
    public String GameProfileID;

    @DBField(ColumnName = "displayName", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 256)
    public String DisplayName;

    public PlayerProfile() {
    }
    public PlayerProfile(UUID uuid, String displayName) {
        GameProfileID = uuid.toString();
        DisplayName = displayName;
    }
    public PlayerProfile(String uuid, String displayName) {
        GameProfileID = uuid;
        DisplayName = displayName;
    }
}
