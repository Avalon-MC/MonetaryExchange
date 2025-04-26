package net.petercashel.monetaryexchange.database.managers;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.petercashel.monetaryexchange.database.abstractions.Manager;
import net.petercashel.monetaryexchange.database.entities.PlayerProfile;
import org.sql2o.Sql2o;

import java.util.Optional;

public class PlayerProfileManager extends Manager<PlayerProfile, Integer> {
    public PlayerProfileManager() {
        super(PlayerProfile.class, Integer.class);
    }

    public PlayerProfileManager(Sql2o backend) {
        super(PlayerProfile.class, Integer.class, backend);
    }

    public Optional<PlayerProfile> FindByGameProfileID(String uuid) {
        return this.FindByProperty("GameProfileID", uuid);
    }

    public void EnsurePlayerCreated(Player player) {
        GameProfile profile = player.getGameProfile();

        if (!FindByGameProfileID(profile.getId().toString()).isPresent()) {
            this.Insert(new PlayerProfile(profile.getId(), profile.getName()));
        }

    }

    public Optional<Integer> GetPlayerID(Player player) {
        GameProfile profile = player.getGameProfile();
        Optional<PlayerProfile> playerDB = FindByGameProfileID(profile.getId().toString());
        if (playerDB.isPresent()) {
            return Optional.of(playerDB.get().PlayerID);
        }

        return Optional.<Integer>empty();
    }
}
