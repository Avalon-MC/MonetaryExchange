package net.petercashel.monetaryexchange.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemWallet extends Item {
    private final String CurrencyGameID;

    public ItemWallet(Properties properties, String currencyGameID) {
        super(properties);
        CurrencyGameID = currencyGameID;
    }

    public boolean IsCurrencyGameID(String currencyGameID) {
        return currencyGameID.equals(CurrencyGameID);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }
}
