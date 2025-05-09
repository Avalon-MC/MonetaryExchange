package net.petercashel.monetaryexchange.blocks.sellingbin.diamond;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.petercashel.monetaryexchange.MonetaryExchange;
import net.petercashel.monetaryexchange.blocks.sellingbin.common.AbstractSellingBinBlockEntity;
import net.petercashel.monetaryexchange.blocks.sellingbin.common.SellingBinEnderContainer;

import static net.petercashel.monetaryexchange.MonetaryExchange.SELLING_BIN_DIAMOND_BLOCK_ENTITY;

public class SellingBinDiamondBlockEntity extends AbstractSellingBinBlockEntity implements Container, Nameable, MenuProvider {

    public SellingBinDiamondBlockEntity(BlockPos pos, BlockState blockState) {
        super(SELLING_BIN_DIAMOND_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getContainerName() {
        return Component.translatable("block.monetaryexchange.selling_bin_iron");
    }

    @Override
    public AbstractContainerMenu createContainerMenu(int containerId, Inventory inventory, Player player) {
        SellingBinEnderContainer c = player.getData(MonetaryExchange.SELLING_BIN_DIAMOND_HANDLER).Container();
        return new ChestMenu(MenuType.GENERIC_9x4, containerId, inventory, c, 4);
    }



    @Override
    public NonNullList<ItemStack> GetItemsList() {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }
}
