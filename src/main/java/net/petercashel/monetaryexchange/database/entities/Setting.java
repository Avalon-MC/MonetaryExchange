package net.petercashel.monetaryexchange.database.entities;

import net.petercashel.monetaryexchange.MonetaryExchange;
import net.petercashel.monetaryexchange.database.annotations.ColumnDataTypeEnum;
import net.petercashel.monetaryexchange.database.annotations.DBField;
import net.petercashel.monetaryexchange.database.annotations.DBKey;
import net.petercashel.monetaryexchange.database.annotations.DBTable;


@DBTable(TableName = "tbl_Settings")
public class Setting {

    public Setting() {
    }
    public Setting(String key) {
        SettingKey = key;
    }

    @DBKey
    @DBField(ColumnName = "id", DataType = ColumnDataTypeEnum.INTEGER)
    int ID;

    @DBField(ColumnName = "settingkey", DataType = ColumnDataTypeEnum.VARCHAR)
    String SettingKey;

    @DBField(ColumnName = "settingvalue", DataType = ColumnDataTypeEnum.VARCHAR)
    String SettingValue;

    public Setting SerializeValue(int value) {
        SettingValue = MonetaryExchange.InternalGSONInstance.toJson(value);
        return this;
    }

    public Setting SerializeValue(String value) {
        SettingValue = MonetaryExchange.InternalGSONInstance.toJson(value);
        return this;
    }

    public int DeserializeIntegerValue() {
        return MonetaryExchange.InternalGSONInstance.fromJson(SettingValue, Integer.class);
    }

    public String DeserializeStringValue() {
        return MonetaryExchange.InternalGSONInstance.fromJson(SettingValue, String.class);
    }
}
