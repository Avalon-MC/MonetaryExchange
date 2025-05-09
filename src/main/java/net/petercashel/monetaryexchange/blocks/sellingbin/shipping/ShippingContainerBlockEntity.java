package net.petercashel.monetaryexchange.blocks.sellingbin.shipping;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.petercashel.monetaryexchange.blocks.sellingbin.common.AbstractSellingBinBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

import static net.petercashel.monetaryexchange.MonetaryExchange.SHIPPING_CONTAINER_BLOCK_ENTITY;

public class ShippingContainerBlockEntity extends AbstractSellingBinBlockEntity implements WorldlyContainer, Container, Nameable, MenuProvider {

    public ShippingContainerBlockEntity(BlockPos pos, BlockState blockState) {
        super(SHIPPING_CONTAINER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getContainerName() {
        return Component.translatable("block.monetaryexchange.shipping_container");
    }

    @Override
    public AbstractContainerMenu createContainerMenu(int containerId, Inventory inventory, Player player) {
        return new ChestMenu(MenuType.GENERIC_9x3, containerId, inventory, this, 3);
    }





//    // Assume that slot 0 is our output and slots 1-8 are our inputs.
//    // Further assume that we output to the top and take inputs from all other sides.
//    private static final int[] OUTPUTS = new int[]{0};
//    private static final int[] INPUTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8};

    private static final int[] AllSLOTS = IntStream.range(0, 27).toArray();;

    // Return an array of exposed slot indices based on the passed Direction.
    @Override
    public int[] getSlotsForFace(Direction side) {
        //return side == Direction.UP ? OUTPUTS : INPUTS;
        return AllSLOTS;
    }

    // Whether items can be placed through the given side at the given slot.
    // For our example, we return true only if we're not inputing from above and are in the index range [1, 8].
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        //return direction != Direction.UP && index > 0 && index < 9;
        return index >= 0 && index < 27;
    }

    // Whether items can be taken from the given side and the given slot.
    // For our example, we return true only if we're pulling from above and from slot index 0.
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        //return direction == Direction.UP && index == 0;
        return false;
    }



    private final NonNullList<ItemStack> items = NonNullList.withSize(
            // The size of the list, i.e. the amount of slots in our container.
            27,
            // The default value to be used in place of where you'd use null in normal lists.
            ItemStack.EMPTY
    );

    @Override
    public NonNullList<ItemStack> GetItemsList() {
        return items;
    }

    // The amount of slots in our container.
    @Override
    public int getContainerSize() {
        return 27;
    }



    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.get(ContainerHelper.TAG_ITEMS) != null) {
            ContainerHelper.loadAllItems(tag.getCompound(ContainerHelper.TAG_ITEMS), GetItemsList(), registries);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag t = new CompoundTag();
        ContainerHelper.saveAllItems(t, GetItemsList(), registries);

        tag.put(ContainerHelper.TAG_ITEMS, t);
    }
}
