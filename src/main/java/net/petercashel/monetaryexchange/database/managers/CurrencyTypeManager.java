package net.petercashel.monetaryexchange.database.managers;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.petercashel.monetaryexchange.MonetaryExchange;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;
import net.petercashel.monetaryexchange.database.abstractions.Manager;
import net.petercashel.monetaryexchange.database.entities.CurrencyType;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

public class CurrencyTypeManager extends Manager<CurrencyType, Integer> {
    public CurrencyTypeManager() {
        super(CurrencyType.class, Integer.class);
    }

    public CurrencyTypeManager(Sql2o backend) {
        super(CurrencyType.class, Integer.class, backend);
    }

    public String GetCurrencyItem(String currencyGameID) {

        Optional<CurrencyType> currencyType = FindByProperty("CurrencyGameID", currencyGameID);
        if (currencyType.isPresent()) {
            return currencyType.get().CurrencyItem;
        }

        return MonetaryExchange.DEFAULT_COIN_ITEM.getRegisteredName();
    }

    public List<String> GetCurrencyGameIDs() {
        return LoadAll().stream().map(x -> x.CurrencyGameID).toList();
    }

    public Optional<Integer> GetCurrencyID(String currencyGameID) {
        Optional<CurrencyType> currencyType = FindByProperty("CurrencyGameID", currencyGameID);
        if (currencyType.isPresent()) {
            return Optional.of(currencyType.get().currencyID);
        }

        return Optional.empty();
    }


    public void EnsureDefaultCurrencyCreated() {
        Optional<CurrencyType> currencyType = FindByProperty("CurrencyGameID", MonetaryExchangeAPI.GetDefaultCurrencyID());

        if (!currencyType.isPresent()) {
            this.Insert(new CurrencyType(MonetaryExchangeAPI.GetDefaultCurrencyID(), MonetaryExchange.DEFAULT_COIN_ITEM.getRegisteredName()));
        }

    }
}
