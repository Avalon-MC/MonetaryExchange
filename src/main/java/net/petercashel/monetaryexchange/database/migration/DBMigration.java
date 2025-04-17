package net.petercashel.monetaryexchange.database.migration;

import net.petercashel.monetaryexchange.database.entities.PlayerProfile;
import net.petercashel.monetaryexchange.database.managers.PlayerProfileManager;
import org.sql2o.Sql2o;

import java.util.Optional;
import java.util.UUID;

public class DBMigration {

    public static int NewDBVersion = 1;

    public static void Prepare(Sql2o instance) {

        int DBVersion = 0;

        //Todo
        /*
        * Get __Migration Table (if not exist, new from scratch)
        * Find Version row
        * Apply Migrations
        */

        switch (DBVersion) {
            case 0 -> {
                PlayerProfileManager playerProfileManager = new PlayerProfileManager(instance);
                if (!playerProfileManager.TableExists()) {
                    playerProfileManager.CreateTable();
                }
                int id = playerProfileManager.Insert(new PlayerProfile(UUID.randomUUID(), "Testing"));
                Optional<PlayerProfile> result = playerProfileManager.FindOne(where -> {
                    where.Equals("DisplayName","Testing", false);
                });
                if (result.isPresent()) {
                    System.out.println(result.get().ID);
                    PlayerProfile r = result.get();
                    r.DisplayName = "WETEST";
                    playerProfileManager.Update(r);
                }
                if (playerProfileManager.Exists(id)) {
                    playerProfileManager.Delete(id);
                }
            }
            case 1 -> {

            }
            default -> {
                //Demons Jim.
            }
        }


    }

}
