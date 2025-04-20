package net.petercashel.monetaryexchange.database.internals;

import net.petercashel.monetaryexchange.database.entities.CurrencyType;
import net.petercashel.monetaryexchange.database.entities.Setting;
import net.petercashel.monetaryexchange.database.managers.BalanceManager;
import net.petercashel.monetaryexchange.database.managers.CurrencyTypeManager;
import net.petercashel.monetaryexchange.database.managers.PlayerProfileManager;
import net.petercashel.monetaryexchange.database.managers.SettingsManager;
import org.sql2o.Sql2o;

import java.util.Optional;

public class DB_Initializer {

    //Current latest DB Version

    public static int NewDBVersion = 2;
    public static MigrationHelper migrationHelper;
    public static SettingsManager settingsManager;
    public static PlayerProfileManager playerProfileManager;
    public static CurrencyTypeManager currencyTypeManager;
    public static BalanceManager balanceManager;

    public static void Prepare(Sql2o instance) {

        //Put Managers here. in order of table creation.
        migrationHelper = new MigrationHelper(instance);
        settingsManager = new SettingsManager(instance);
        playerProfileManager = new PlayerProfileManager(instance);
        currencyTypeManager = new CurrencyTypeManager(instance);
        balanceManager = new BalanceManager(instance);

        int DBVersion = 0;
        Optional<Setting> tableVersion = null;

        if (!settingsManager.TableExists()) {
            settingsManager.CreateTable();
            settingsManager.Insert(new Setting("__TableVersion").SerializeValue(0));
        }

        tableVersion = settingsManager.FindByKey("__TableVersion");
        DBVersion = getDbVersion(tableVersion);


        //Case 0 is default new install seed.
        //Case 1+ is for migrations. If you make changes, you program those into the number NewDBVersion is set to,
        // and then increment NewDBVersion by 1 so that new installs fall into the default catch again after 0 is run.
        switch (DBVersion) {
            case 0:
                //Run table creation in order for new installs.

                if (!playerProfileManager.TableExists()) {
                    playerProfileManager.CreateTable();
                }
                if (!currencyTypeManager.TableExists()) {
                    currencyTypeManager.CreateTable();
                }
                if (!balanceManager.TableExists()) {
                    balanceManager.CreateTable();
                }


                UpdateDatabaseVersion(tableVersion);
                break;
            case 1:
                //Migrations start here
                migrationHelper.AddColumn(CurrencyType.class, "currencyItem", "VARCHAR(250)");
            case 2:




                //Currency unused

            default:
                //Ending code
                //Set to current Version
                UpdateDatabaseVersion(tableVersion);
                break;




        }

        //Seed data using ensure / if not exists style methods
        currencyTypeManager.EnsureDefaultCurrencyCreated();

        //Handle other currencies



    }

    private static int getDbVersion(Optional<Setting> tableVersion) {
        if (tableVersion.isPresent()) {
            return tableVersion.get().DeserializeIntegerValue();
        }
        return 0;
    }

    private static void UpdateDatabaseVersion(Optional<Setting> tableVersion) {
        if (tableVersion.isPresent()) {
            Setting s = tableVersion.get().SerializeValue(NewDBVersion);
            settingsManager.Update(s);
        }
    }

}
