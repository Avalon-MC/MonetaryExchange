package net.petercashel.monetaryexchange.database.managers;

import net.petercashel.monetaryexchange.database.abstractions.Manager;
import net.petercashel.monetaryexchange.database.entities.PlayerBalance;
import org.sql2o.Sql2o;

import java.util.Optional;

public class BalanceManager extends Manager<PlayerBalance, Integer> {
    public BalanceManager() {
        super(PlayerBalance.class, Integer.class);
    }

    public BalanceManager(Sql2o backend) {
        super(PlayerBalance.class, Integer.class, backend);
    }


    public double GetPlayerBalance(int playerID, int currencyID) {
        Optional<PlayerBalance> result = this.FindOne(where -> {
            where.Equals("PlayerID", playerID, false);
            where.Equals("CurrencyID", currencyID, false);
        });

        return result.map(playerBalance -> playerBalance.Balance).orElse(0.0);

    }

    public boolean SetPlayerBalance(int playerID, int currencyID, double balance) {
        Optional<PlayerBalance> result = this.FindOne(where -> {
            where.Equals("PlayerID", playerID, false);
            where.Equals("CurrencyID", currencyID, false);
        });

        if (result.isPresent()) {
            PlayerBalance b = result.get();
            b.Balance = balance;
            return Update(b);
        } else {
            PlayerBalance b = new PlayerBalance(playerID, currencyID, balance);
            int i = Insert(b);
            return i > 0;
        }
    }

}
