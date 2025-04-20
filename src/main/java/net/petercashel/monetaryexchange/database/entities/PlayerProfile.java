package net.petercashel.monetaryexchange.database.entities;

import net.petercashel.monetaryexchange.database.annotations.ColumnDataTypeEnum;
import net.petercashel.monetaryexchange.database.annotations.DBField;
import net.petercashel.monetaryexchange.database.annotations.DBKey;
import net.petercashel.monetaryexchange.database.annotations.DBTable;

import java.time.LocalDateTime;
import java.util.UUID;

@DBTable(TableName = "tbl_PlayerProfiles")
public class PlayerProfile {

    @DBKey
    @DBField(ColumnName = "playerID", DataType = ColumnDataTypeEnum.INTEGER)
    public int ID;

    @DBField(ColumnName = "gameProfileID", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 48)
    public String GameProfileID;

    @DBField(ColumnName = "displayName", DataType = ColumnDataTypeEnum.VARCHAR, MaxLength = 256, Nullable = true)
    public String DisplayName;


    @DBField(ColumnName = "lastlogin", DataType = ColumnDataTypeEnum.LOCALDATETIME)
    public LocalDateTime LastLogin;

    public PlayerProfile() {
    }
    public PlayerProfile(UUID uuid, String displayName) {
        GameProfileID = uuid.toString();
        DisplayName = displayName;
        LastLogin = LocalDateTime.now();
    }
    public PlayerProfile(String uuid, String displayName) {
        GameProfileID = uuid;
        DisplayName = displayName;
        LastLogin = LocalDateTime.now();
    }
}
