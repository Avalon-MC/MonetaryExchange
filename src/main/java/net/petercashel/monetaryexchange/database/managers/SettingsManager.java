package net.petercashel.monetaryexchange.database.managers;

import net.petercashel.monetaryexchange.database.abstractions.Manager;
import net.petercashel.monetaryexchange.database.entities.Setting;
import org.sql2o.Sql2o;

import java.util.Optional;

public class SettingsManager extends Manager<Setting, Integer> {
    public SettingsManager() {
        super(Setting.class, Integer.class);
    }

    public SettingsManager(Sql2o backend) {
        super(Setting.class, Integer.class, backend);
    }

    public Optional<Setting> FindByKey(String key) {
        return this.FindOne(where -> {
            where.Equals("SettingKey", key, false);
        });
    }
}