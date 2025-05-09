package net.petercashel.monetaryexchange.items;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.petercashel.monetaryexchange.api.MonetaryExchangeAPI;

import java.util.Optional;

public class ItemWallet extends Item {
    private final String CurrencyGameID;

    public ItemWallet(Properties properties, String currencyGameID) {
        super(properties);
        CurrencyGameID = currencyGameID;
    }

    public boolean IsCurrencyGameID(String currencyGameID) {
        return currencyGameID.equals(CurrencyGameID);
    }

    private Optional<Holder.Reference<Item>> GetCurrencyItem() {
        ResourceLocation loc = MonetaryExchangeAPI.API_Instance.GetCurrencyItem(CurrencyGameID);
        return BuiltInRegistries.ITEM.getHolder(loc);
    }

    private boolean isCurrencyItem(ItemStack stack) {
        Optional<Holder.Reference<Item>> item = GetCurrencyItem();
        if (item.isPresent()) {
            return stack.is(item.get());
        }
        return false;
    }

    private boolean IsUsedHand(Player player, InteractionHand usedHand) {
        if (!player.getItemInHand(usedHand).isEmpty() && player.getItemInHand(usedHand).getItem() instanceof ItemWallet) {
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return super.useOn(context);
        }
        if (context.getPlayer() == null) {
            return super.useOn(context);
        }
        if (!IsUsedHand(context.getPlayer(), context.getHand())) return super.useOn(context);

        //Use On Block

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) {
            return super.use(level, player, usedHand);
        }
        if (!IsUsedHand(player, usedHand)) {
            return super.use(level, player, usedHand);
        }

        //Right Click Air
        if (player.isShiftKeyDown()) {
            withdrawCurrency(level, player, usedHand);
            player.playSound(SoundEvent.createFixedRangeEvent(ResourceLocation.parse("minecraft:block.amethyst_block.hit"), 1f));
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        } else {
            depositCurrency(level, player, usedHand);
            player.playSound(SoundEvent.createFixedRangeEvent(ResourceLocation.parse("minecraft:block.amethyst_block.resonate"), 1f));
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide()) {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
        //Probably right click player
        if (!(interactionTarget instanceof Player)) {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }


        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (entity.level().isClientSide()) {
            return super.onEntitySwing(stack, entity, hand);
        }
        //Left Swing Air, entity is player
        return super.onEntitySwing(stack, entity, hand);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }


    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        //Probably left click player, We leave this alone. We dont break punching.
        return super.onLeftClickEntity(stack, player, entity);
    }



    private void depositCurrency(Level level, Player player, InteractionHand usedHand) {
        //count total
        //deposit
        //clear
        double count = 0;

        Inventory playerInv = player.getInventory();
        int slots = playerInv.getContainerSize();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = playerInv.getItem(slot);
            if (!stack.isEmpty() && isCurrencyItem(stack)) {
                count += stack.getCount();
            }
        }

        if (count == 0) {
            //Balance
            count = MonetaryExchangeAPI.API_Instance.GetCurrency(CurrencyGameID, player);
            Component c = Component.literal("§7You have §a")
                    .append(Double.toString(count))
                    .append(" §7")
                    .append(
                            count > 1 ?
                                Component.translatable("item.monetaryexchange.default_coin_plural") :
                                Component.translatable("item.monetaryexchange.default_coin")
                    )
                    .append(".");
            player.sendSystemMessage(c);

        } else {
            //Deposit, the  clear items
            if (MonetaryExchangeAPI.API_Instance.GiveCurrency(CurrencyGameID, player, count)) {
                double balance = MonetaryExchangeAPI.API_Instance.GetCurrency(CurrencyGameID, player);
                Component c = Component.literal("§7You've deposited §a")
                        .append(Double.toString(count))
                        .append(" §7")
                        .append(
                                count > 1 ?
                                        Component.translatable("item.monetaryexchange.default_coin_plural") :
                                        Component.translatable("item.monetaryexchange.default_coin")
                        )
                        .append(" §7(")
                        .append(Double.toString(balance))
                        .append(").");
                player.sendSystemMessage(c);

                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = playerInv.getItem(slot);
                    if (!stack.isEmpty() && isCurrencyItem(stack)) {
                        playerInv.setItem(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    private void withdrawCurrency(Level level, Player player, InteractionHand usedHand) {
        int maxStackSize = 64;
        double balance = MonetaryExchangeAPI.API_Instance.GetCurrency(CurrencyGameID, player);
        if (balance >= 1) {
            Optional<Holder.Reference<Item>> currencyItem = GetCurrencyItem();
            if (currencyItem.isPresent() && currencyItem.get().isBound()) {
                Item coin = currencyItem.get().value();
                maxStackSize = coin.getDefaultMaxStackSize();

                if (maxStackSize > balance) {
                    maxStackSize = (int) Math.floor(balance);
                }
                if (MonetaryExchangeAPI.API_Instance.TakeCurrency(CurrencyGameID, player, maxStackSize)) {
                    player.addItem(new ItemStack(coin, maxStackSize));
                    balance = MonetaryExchangeAPI.API_Instance.GetCurrency(CurrencyGameID, player);

                    Component c = Component.literal("§7You've withdrawn §a")
                            .append(Double.toString(maxStackSize))
                            .append(" §7")
                            .append(
                                    maxStackSize > 1 ?
                                            Component.translatable("item.monetaryexchange.default_coin_plural") :
                                            Component.translatable("item.monetaryexchange.default_coin")
                            )
                            .append(" §7(")
                            .append(Double.toString(balance))
                            .append(").");
                    player.sendSystemMessage(c);
                }
            }
        } else {
            Component c = Component.literal("§7You have §a")
                    .append(Double.toString(balance))
                    .append(" §7")
                    .append(
                            balance > 1 ?
                                    Component.translatable("item.monetaryexchange.default_coin_plural") :
                                    Component.translatable("item.monetaryexchange.default_coin")
                    )
                    .append(".");
            player.sendSystemMessage(c);
        }
    }
}
