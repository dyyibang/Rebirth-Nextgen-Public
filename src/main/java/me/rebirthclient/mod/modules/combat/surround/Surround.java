package me.rebirthclient.mod.modules.combat.surround;

import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.movement.AutoCenter;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class Surround extends Module {
    public static Surround INSTANCE = new Surround();
    public final BooleanSetting enableInHole =
            add(new BooleanSetting("EnableInHole"));
    private final Timer timer = new Timer();
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 0, 500));
    private final SliderSetting multiPlace =
            add(new SliderSetting("MultiPlace", 1, 8));
    private final BooleanSetting spam =
            add(new BooleanSetting("Spam"));
    private final BooleanSetting mineSpam =
            add(new BooleanSetting("MineSpam"));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("CheckMine"));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate"));
    private final BooleanSetting helper =
            add(new BooleanSetting("Helper"));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("BreakCrystal"));
    private final SliderSetting safeHealth =
            add(new SliderSetting("SafeHealth", 0.0f, 36f, v -> breakCrystal.getValue()));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", v -> breakCrystal.getValue()));
    private final BooleanSetting center =
            add(new BooleanSetting("Center"));
    public final BooleanSetting extend =
            add(new BooleanSetting("Extend"));
    public final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround"));
    private final BooleanSetting moveDisable =
            add(new BooleanSetting("MoveDisable"));
    private final BooleanSetting strictDisable =
            add(new BooleanSetting("StrictDisable", v-> moveDisable.getValue()));
    private final BooleanSetting isMoving =
            add(new BooleanSetting("isMoving", v-> moveDisable.getValue()));
    private final BooleanSetting jumpDisable =
            add(new BooleanSetting("JumpDisable"));
    private final BooleanSetting inMoving =
            add(new BooleanSetting("inMoving", v-> jumpDisable.getValue()));
    private final BooleanSetting enderChest =
            add(new BooleanSetting("EnderChest"));
    public final BooleanSetting render =
            add(new BooleanSetting("Render"));
    public final BooleanSetting box = add(new BooleanSetting("Box", v -> render.getValue()));
    public final BooleanSetting outline = add(new BooleanSetting("Outline", v -> render.getValue()));
    public final ColorSetting color =  add(new ColorSetting("Color",new Color(255, 255, 255, 100), v -> render.getValue()));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 0, 5000, v -> render.getValue()));
    public final BooleanSetting pre = add(new BooleanSetting("Pre", v -> render.getValue()));
    public final BooleanSetting moveReset = add(new BooleanSetting("Reset", v -> render.getValue()));

    double startX = 0;
    double startY = 0;
    double startZ = 0;
    int progress = 0;
    public Surround() {
        super("Surround", "Surrounds you with Obsidian", Category.Combat);
        INSTANCE = this;
    }

    static boolean checkSelf(BlockPos pos) {
        return mc.player.getBoundingBox().intersects(new Box(pos));
    }

    BlockPos startPos = null;
    @Override
    public void onEnable() {
        startPos = EntityUtil.getPlayerPos();
        startX = mc.player.getX();
        startY = mc.player.getY();
        startZ = mc.player.getZ();
        if (center.getValue() && getBlock() != -1) {
            AutoCenter.INSTANCE.enable();
        }
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        if (!timer.passedMs((long) delay.getValue())) return;
        progress = 0;
        BlockPos pos = EntityUtil.getPlayerPos();
        if (startPos == null || (!EntityUtil.getPlayerPos().equals(startPos) && moveDisable.getValue() && strictDisable.getValue() && (!isMoving.getValue() || MovementUtil.isMoving())) || (MathHelper.sqrt((float) mc.player.squaredDistanceTo(startX, startY, startZ)) > 1.3 && moveDisable.getValue() && !strictDisable.getValue() && (!isMoving.getValue() || MovementUtil.isMoving())) || (jumpDisable.getValue() && (startY - mc.player.getY() > 0.5 || startY - mc.player.getY() < -0.5) && (!inMoving.getValue() || MovementUtil.isMoving()))) {
            disable();
            return;
        }
        if (getBlock() == -1) {
            CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oObsidian" + (enderChest.getValue() ? "/EnderChest" : "") + "?");
            disable();
            return;
        }
        if (onlyGround.getValue() && !mc.player.isOnGround()) return;
        for (Direction i : Direction.values()) {
            if (i == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(i);
            if (BlockUtil.getPlaceSide(offsetPos) != null) {
                placeBlock(offsetPos);
            } else {
                if (BlockUtil.canReplace(offsetPos)) placeBlock(getHelper(offsetPos));
            }
            if (checkSelf(offsetPos) && extend.getValue()) {
                for (Direction i2 : Direction.values()) {
                    if (i2 == Direction.UP) continue;
                    BlockPos offsetPos2 = offsetPos.offset(i2);
                    if (checkSelf(offsetPos2)) {
                        for (Direction i3 : Direction.values()) {
                            if (i3 == Direction.UP) continue;
                            placeBlock(offsetPos2);
                            BlockPos offsetPos3 = offsetPos2.offset(i3);
                            placeBlock(BlockUtil.getPlaceSide(offsetPos3) != null || !BlockUtil.canReplace(offsetPos3) ? offsetPos3 : getHelper(offsetPos3));
                        }
                    }
                    placeBlock(BlockUtil.getPlaceSide(offsetPos2) != null || !BlockUtil.canReplace(offsetPos2) ? offsetPos2 : getHelper(offsetPos2));
                }
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (pos == null) return;
        if (pre.getValue()) {
            ExtraSurround.addBlock(pos);
        }
        if (checkMine.getValue() && BlockUtil.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, true) && (!mineSpam.getValue() || !BlockUtil.isMining(pos))) return;
        if (breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= safeHealth.getValue()) {
            CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        }
        if (!(breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= safeHealth.getValue()) && BlockUtil.hasEntity(pos, false) && !spam.getValue()) return;
        if (!(progress < multiPlace.getValue())) return;
        int old = mc.player.getInventory().selectedSlot;
        int block = getBlock();
        if (block == -1) return;
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.sync();
        } else {
            doSwap(old);
        }
        progress++;
        timer.reset();
        ExtraSurround.addBlock(pos);
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
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        } else {
            if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !enderChest.getValue()) {
                return InventoryUtil.findBlock(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
        }
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!helper.getValue()) {
            return pos.down();
        }
        for (Direction i : Direction.values()) {
            if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
        }
        return null;
    }
}