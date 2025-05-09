package net.petercashel.monetaryexchange.blocks.sellingbin.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SellingBinEnderAttachment implements INBTSerializable<CompoundTag> {
    SellingBinEnderContainer container;
    public SellingBinEnderAttachment(int size) {
        container = new SellingBinEnderContainer(size);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compound = new CompoundTag();
        compound.put("BinItems", this.container.createTag(provider));
        return compound;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compound) {
        if (compound.contains("BinItems", 9)) {
            this.container.fromTag(compound.getList("BinItems", 10), provider);
        }
    }

    public SellingBinEnderContainer Container() {
        return container;
    }
}
