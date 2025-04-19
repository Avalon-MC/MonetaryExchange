package net.petercashel.monetaryexchange.database.migration;

import net.petercashel.monetaryexchange.database.entities.PlayerProfile;
import net.petercashel.monetaryexchange.database.entities.Setting;
import net.petercashel.monetaryexchange.database.managers.PlayerProfileManager;
import net.petercashel.monetaryexchange.database.managers.SettingsManager;
import org.sql2o.Sql2o;

import java.util.Optional;
import java.util.UUID;

public class DBMigration {

    public static int NewDBVersion = 1;

    public static void Prepare(Sql2o instance) {
        SettingsManager settingsManager = new SettingsManager(instance);

        //Set this to current version, will be replaced with
        int DBVersion = 0;
        Optional<Setting> tableVersion = null;

        if (!settingsManager.TableExists()) {
            settingsManager.CreateTable();
            settingsManager.Insert(new Setting("__TableVersion").SerializeValue(0));
        }

        tableVersion = settingsManager.FindByKey("__TableVersion");
        if (tableVersion.isPresent()) {
            DBVersion = tableVersion.get().DeserializeIntegerValue();
        }

        //Put Managers here. in order of table creation.
        PlayerProfileManager playerProfileManager = new PlayerProfileManager(instance);




        //Case 0 is default new install seed.
        //Case 1+ is for migrations. If you make changes, you program those into the number NewDBVersion is set to,
        // and then increment NewDBVersion by 1 so that new installs fall into the default catch again after 0 is run.
        switch (DBVersion) {
            case 0:
                //Run table creation in order for new installs.

                if (!playerProfileManager.TableExists()) {
                    playerProfileManager.CreateTable();
                }





                //Set to current Version
                if (tableVersion.isPresent()) {
                    Setting s = tableVersion.get().SerializeValue(DBVersion);
                    settingsManager.Update(s);
                }
                break;
            case 1:
                if (!playerProfileManager.TableExists()) {
                    playerProfileManager.CreateTable();
                }



            default:
                //Ending code
                //Set to current Version
                if (tableVersion.isPresent()) {
                    Setting s = tableVersion.get().SerializeValue(DBVersion);
                    settingsManager.Update(s);
                }
                break;




        }



    }

}
