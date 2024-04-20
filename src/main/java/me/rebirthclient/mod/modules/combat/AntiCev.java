package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class AntiCev extends Module {
    public static AntiCev INSTANCE = new AntiCev();
    final Timer timer = new Timer();
    private final SliderSetting delay =
            add(new SliderSetting("Delay", 0, 500));
    private final SliderSetting multiPlace =
            add(new SliderSetting("MultiPlace", 1, 8));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate"));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround"));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("BreakCrystal"));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("CheckMine"));
    private final BooleanSetting eatingPause = add(new BooleanSetting("EatingPause"));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    int progress = 0;

    public AntiCev() {
        super("AntiCev", "Anti cev", Category.Combat);
        INSTANCE = this;
    }

    private final List<BlockPos> crystalPos = new ArrayList<>();

    private BlockPos pos;

    @Override
    public void onUpdate() {
        if (!timer.passedMs(delay.getValue())) return;
        if (eatingPause.getValue() && EntityUtil.isUsing()) return;
        progress = 0;
        if (pos != null && !pos.equals(EntityUtil.getPlayerPos(true))) {
            crystalPos.clear();
        }
        pos = EntityUtil.getPlayerPos(true);
        for (Direction i : Direction.values()) {
            if (i == Direction.DOWN) continue;
            if (isGod(pos.offset(i).up())) continue;
            BlockPos offsetPos = pos.offset(i).up(2);
            if (crystalHere(offsetPos) && !crystalPos.contains(offsetPos)) {
                crystalPos.add(offsetPos);
            }
        }
        if (getBlock() == -1) {
            return;
        }
        if (onlyGround.getValue() && !mc.player.isOnGround()) return;
        crystalPos.removeIf((pos) -> !BlockUtil.clientCanPlace(pos, true));
        for (BlockPos defensePos : crystalPos) {
            if (crystalHere(defensePos)) {
                if (breakCrystal.getValue()) {
                    CombatUtil.attackCrystal(defensePos, rotate.getValue(), false);
                }
            }
            if (BlockUtil.canPlace(defensePos, 6, breakCrystal.getValue())) {
                placeBlock(defensePos);
            }
        }
    }

    private boolean crystalHere(BlockPos pos) {
        for (Entity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos))) {
            if (EntityUtil.getEntityPos(entity).equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGod(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }
    private void placeBlock(BlockPos pos) {
        if (!(progress < multiPlace.getValue())) return;
        if (checkMine.getValue() && BlockUtil.isMining(pos)) {
            return;
        }
        int oldSlot = mc.player.getInventory().selectedSlot;
        int block;
        if ((block = getBlock()) == -1) {
            return;
        }
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.sync();
        } else {
            doSwap(oldSlot);
        }
        progress++;
        timer.reset();
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        } else {
            InventoryUtil.doSwap(slot);
        }
    }
    private int getBlock() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }
}