package net.flytre.flytre_lib.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.IntStream;

/**
 * Implemented inventory to prevent errors when creating your custom machine!
 * Highly recommended over default inventory unless you need absolutely crazy
 * behavior!
 */
public interface EasyInventory extends SidedInventory, IOTypeProvider {
    static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return first.getCount() + second.getCount() <= first.getMaxCount() && ItemStack.areTagsEqual(first, second);
        }
    }

    DefaultedList<ItemStack> getItems();

    Map<Direction, IOType> getItemIO();

    default Map<Direction, IOType> getIOType() {
        return getItemIO();
    }

    @Override
    default int size() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(getItems(), slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    default ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    default void clear() {
        getItems().clear();
    }

    @Override
    default int[] getAvailableSlots(Direction side) {
        return IntStream.range(0, size()).toArray();
    }

    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(slot, stack) && getItemIO().get(dir).canInsert();
    }

    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return getItemIO().get(dir).canExtract();
    }

    default ItemStack addStack(ItemStack stack, Direction dir) {
        int[] slots = getAvailableSlots(dir);
        for (int i : slots) {
            ItemStack currentStack = getStack(i);
            if (canInsert(i, stack, dir)) {
                if (currentStack.isEmpty()) {
                    setStack(i, stack);
                    markDirty();
                    stack = ItemStack.EMPTY;
                } else if (canMergeItems(currentStack, stack)) {
                    if (currentStack.getCount() < currentStack.getMaxCount()) {
                        int p = stack.getMaxCount() - currentStack.getCount();
                        int j = Math.min(stack.getCount(), p);
                        stack.decrement(j);
                        currentStack.increment(j);
                    }
                }
            }
        }
        return stack;
    }

    default ItemStack addStackInternal(ItemStack stack) {
        for (int i : IntStream.range(0, size()).toArray()) {
            ItemStack currentStack = getStack(i);
            if (isValid(i, stack)) {
                if (currentStack.isEmpty()) {
                    setStack(i, stack);
                    markDirty();
                    stack = ItemStack.EMPTY;
                } else if (canMergeItems(currentStack, stack)) {
                    if (currentStack.getCount() < currentStack.getMaxCount()) {
                        int p = stack.getMaxCount() - currentStack.getCount();
                        int j = Math.min(stack.getCount(), p);
                        stack.decrement(j);
                        currentStack.increment(j);
                    }
                }
            }
        }
        return stack;
    }

}
