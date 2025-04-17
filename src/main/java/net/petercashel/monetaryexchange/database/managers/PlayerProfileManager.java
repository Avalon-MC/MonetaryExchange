package net.petercashel.monetaryexchange.database.managers;

import net.petercashel.monetaryexchange.database.abstractions.Manager;
import net.petercashel.monetaryexchange.database.entities.PlayerProfile;
import org.sql2o.Sql2o;

public class PlayerProfileManager extends Manager<PlayerProfile, Integer> {
    public PlayerProfileManager() {
        super(PlayerProfile.class, Integer.class);
    }

    public PlayerProfileManager(Sql2o backend) {
        super(PlayerProfile.class, Integer.class, backend);
    }
}
