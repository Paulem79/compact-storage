package com.witchica.compactstorage.common.inventory;

import com.witchica.compactstorage.common.util.CompactStorageInventoryImpl;
import com.witchica.compactstorage.common.util.CompactStorageUpgradeType;
import com.witchica.compactstorage.common.util.CompactStorageUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackInventory implements Container, CompactStorageInventoryImpl  {
    public NonNullList<ItemStack> items;
    public int inventoryWidth;
    public int inventoryHeight;

    private final Player player;
    private final int backpackSlot;
    private final boolean isInOffhand;

    public BackpackInventory(CompoundTag itemsNbt, Player player, boolean isInOffhand) {
        this.backpackSlot = player.getInventory().selected;
        this.player = player;
        this.isInOffhand = isInOffhand;

        this.fromTag(itemsNbt);
    }


    public void resizeInventory(boolean copy_contents) {
        NonNullList<ItemStack> newInventory = NonNullList.withSize(inventoryWidth * inventoryHeight, ItemStack.EMPTY);

        if(copy_contents) {
            NonNullList<ItemStack> list = this.items;

            for(int i = 0; i < list.size(); i++) {
                newInventory.set(i, list.get(i));
            }
        }

        this.items = newInventory;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public NonNullList<ItemStack> getItemList() {
        return items;
    }

    @Override
    public int getInventoryWidth() {
        return inventoryWidth;
    }

    @Override
    public int getInventoryHeight() {
        return inventoryHeight;
    }

    @Override
    public int getContainerSize() {
        return getInventoryWidth() * getInventoryHeight();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
    }

    @Override
    public void setChanged() {
        
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void fromTag(CompoundTag tag) {
        this.inventoryWidth = tag.contains("inventory_width") ? tag.getInt("inventory_width") : 9;
        this.inventoryHeight = tag.contains("inventory_height") ? tag.getInt("inventory_height") : 6;
        
        this.items = NonNullList.withSize(inventoryWidth * inventoryHeight, ItemStack.EMPTY);
        CompactStorageUtil.readItemsFromTag(this.items, tag);
    }

    @Override
    public boolean increaseSize(int x, int y) {
        if((inventoryWidth > 23 && x > 0) || (inventoryHeight > 11 && y > 0)) {
            return false;
        }

        inventoryWidth += x;
        inventoryHeight += y;

        resizeInventory(true);
        setChanged();

        return true;
    }

    @Override
    public void applyRetainingUpgrade() {

    }

    @Override
    public boolean canUpgradeTypeBeApplied(CompactStorageUpgradeType upgradeType) {
        switch(upgradeType) {
            case RETAINING -> {
                return false;
            }
            case WIDTH_INCREASE -> {
                return inventoryWidth < 24;
            }
            case HEIGHT_INCREASE -> {
                return inventoryHeight < 12;
            }
        }

        return false;
    }

    @Override
    public boolean hasUpgrade(CompactStorageUpgradeType upgradeTypes) {
        switch(upgradeTypes) {
            case RETAINING -> {
                return false;
            }
            case WIDTH_INCREASE -> {
                return inventoryWidth > 9;
            }
            case HEIGHT_INCREASE -> {
                return inventoryHeight > 6;
            }
        }

        return false;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("inventory_width", inventoryWidth);
        tag.putInt("inventory_height", inventoryHeight);

        CompactStorageUtil.writeItemsToTag(this.items, tag);

        return tag;
    }

    @Override
    public void startOpen(Player player) {
        Container.super.startOpen(player);
        player.playNotifySound(SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1f, 1f);
    }

    @Override
    public void stopOpen(Player player) {
        Container.super.stopOpen(player);
        Inventory inventory = player.getInventory();

        if(isInOffhand) {
            if(!player.getItemInHand(InteractionHand.OFF_HAND).hasTag()) {
                player.getItemInHand(InteractionHand.OFF_HAND).setTag(new CompoundTag());
            }

            player.getItemInHand(InteractionHand.OFF_HAND).getTag().put("Backpack", toTag());
        } else {
            if(!inventory.getItem(backpackSlot).hasTag()) {
                inventory.getItem(backpackSlot).setTag(new CompoundTag());
            }

            inventory.getItem(backpackSlot).getTag().put("Backpack", toTag());
        }
        player.playNotifySound(SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1f, 1f);
    }

    @Override
    public boolean applyUpgrade(CompactStorageUpgradeType upgradeType) {
        if(canUpgradeTypeBeApplied(upgradeType)) {
            switch(upgradeType) {
                case WIDTH_INCREASE -> {
                    increaseSize(1, 0);
                    return true;
                }
                case HEIGHT_INCREASE -> {
                    increaseSize(0, 1);
                    return true;
                }
            }
        }

        return false;
    }
}