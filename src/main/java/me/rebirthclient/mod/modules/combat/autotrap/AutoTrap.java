package me.rebirthclient.mod.modules.combat.autotrap;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.Placement;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;

public class AutoTrap
        extends Module {
    final Timer timer = new Timer();
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate"));
    private final SliderSetting multiPlace = add(new SliderSetting("MultiPlace", 1, 8));
    private final BooleanSetting autoDisable =
            add(new BooleanSetting("AutoDisable"));
    private final SliderSetting range =
            add(new SliderSetting("Range", 1.0f, 8.0f));
    private final EnumSetting targetMod =
            add(new EnumSetting("TargetMode", TargetMode.Single));
    private final BooleanSetting checkMine =
            add(new BooleanSetting("CheckMine"));
    private final BooleanSetting helper =
            add(new BooleanSetting("Helper"));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap"));
    private final BooleanSetting extend =
            add(new BooleanSetting("Extend"));
    private final BooleanSetting antiStep =
            add(new BooleanSetting("AntiStep"));
    private final BooleanSetting onlyBreak =
            add(new BooleanSetting("OnlyBreak", v -> antiStep.getValue()));
    private final BooleanSetting head =
            add(new BooleanSetting("Head"));
    private final BooleanSetting headExtend =
            add(new BooleanSetting("HeadExtend"));
    private final BooleanSetting headAnchor =
            add(new BooleanSetting("HeadAnchor"));
    private final BooleanSetting chestUp =
            add(new BooleanSetting("ChestUp"));
    private final BooleanSetting onlyBreaking =
            add(new BooleanSetting("OnlyBreaking", v -> chestUp.getValue()));
    private final BooleanSetting chest =
            add(new BooleanSetting("Chest"));
    private final BooleanSetting onlyGround =
            add(new BooleanSetting("OnlyGround", v -> chest.getValue()));
    private final BooleanSetting legs =
            add(new BooleanSetting("Legs"));
    private final BooleanSetting legAnchor =
            add(new BooleanSetting("LegAnchor"));
    private final BooleanSetting down =
            add(new BooleanSetting("Down"));
    private final BooleanSetting onlyHole =
            add(new BooleanSetting("OnlyHole"));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("BreakCrystal"));
    private final BooleanSetting usingPause = add(new BooleanSetting("UsingPause"));
    public final SliderSetting delay =
            add(new SliderSetting("Delay", 0, 500));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 1.0f, 6.0f));
    private final BooleanSetting selfGround = add(new BooleanSetting("SelfGround"));
    public final BooleanSetting render =
            add(new BooleanSetting("Render"));
    public final BooleanSetting box = add(new BooleanSetting("Box", v -> render.getValue()));
    public final BooleanSetting outline = add(new BooleanSetting("Outline", v -> render.getValue()));
    public final ColorSetting color =  add(new ColorSetting("Color",new Color(255, 255, 255, 100), v -> render.getValue()));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 0, 5000, v -> render.getValue()));
    public final BooleanSetting pre = add(new BooleanSetting("Pre", v -> render.getValue()));
    public final BooleanSetting sync = add(new BooleanSetting("Sync", v -> render.getValue()));
    public PlayerEntity target;
    public static AutoTrap INSTANCE;

    public AutoTrap() {
        super("AutoTrap", "Automatically trap the enemy", Category.Combat);
        INSTANCE = this;
    }

    public enum TargetMode {
        Single, Multi
    }

    int progress = 0;
    private final ArrayList<BlockPos> trapList = new ArrayList<>();
    private final ArrayList<BlockPos> placeList = new ArrayList<>();

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        trapList.clear();
        placeList.clear();
        progress = 0;
        if (selfGround.getValue() && !mc.player.isOnGround()) {
            target = null;
            return;
        }
        if (usingPause.getValue() && EntityUtil.isUsing()) {
            target = null;
            return;
        }
        if (!timer.passedMs((long) delay.getValue())) {
            return;
        }
        if (targetMod.getValue() == TargetMode.Single) {
            target = CombatUtil.getClosestEnemy(range.getValue());
            if (target == null) {
                if (autoDisable.getValue()) disable();
                return;
            }
            trapTarget(target);
        } else if (targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (PlayerEntity player : CombatUtil.getEnemies(range.getValue())) {
                found = true;
                target = player;
                trapTarget(target);
            }
            if (!found) {
                if (autoDisable.getValue()) disable();
                target = null;
            }
        }
    }

    private void trapTarget(PlayerEntity target) {
        if (onlyHole.getValue() && !BlockUtil.isHole(EntityUtil.getEntityPos(target))) return;
        doTrap(EntityUtil.getEntityPos(target, true));
    }

    private void doTrap(BlockPos pos) {
        if (trapList.contains(pos)) return;
        trapList.add(pos);
        if (legs.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i);
                placeAnchor(offsetPos, legAnchor.getValue());
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos) && getHelper(offsetPos) != null)
                    placeBlock(getHelper(offsetPos));
            }
        }
        if (headExtend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(z, 0, x);
                    if (checkEntity(new BlockPos(offsetPos))) placeAnchor(offsetPos.up(2), headAnchor.getValue());
                }
            }
        }
        if (head.getValue()) {
            if (BlockUtil.clientCanPlace(pos.up(2))) {
                if (BlockUtil.getPlaceSide(pos.up(2)) == null) {
                    boolean trapChest = true;
                    if (getHelper(pos.up(2)) != null) {
                        placeBlock(getHelper(pos.up(2)));
                        trapChest = false;
                    }
                    if (trapChest) {
                        for (Direction i : Direction.values()) {
                            if (i == Direction.DOWN || i == Direction.UP) continue;
                            BlockPos offsetPos = pos.offset(i).up();
                            if (BlockUtil.clientCanPlace(offsetPos.up(), false)) {
                                if (BlockUtil.canPlace(offsetPos)) {
                                    placeBlock(offsetPos);
                                    trapChest = false;
                                    break;
                                }
                            }
                        }
                        if (trapChest) {
                            for (Direction i : Direction.values()) {
                                if (i == Direction.DOWN || i == Direction.UP) continue;
                                BlockPos offsetPos = pos.offset(i).up();
                                if (BlockUtil.clientCanPlace(offsetPos.up(), false)) {
                                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos) && getHelper(offsetPos) != null) {
                                        placeBlock(getHelper(offsetPos));
                                        trapChest = false;
                                        break;
                                    }
                                }
                            }
                            if (trapChest) {
                                for (Direction i : Direction.values()) {
                                    if (i == Direction.DOWN || i == Direction.UP) continue;
                                    BlockPos offsetPos = pos.offset(i).up();
                                    if (BlockUtil.clientCanPlace(offsetPos.up(), false)) {
                                        if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, false) && getHelper(offsetPos) != null) {
                                            if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), false) && getHelper(offsetPos.down()) != null) {
                                                placeBlock(getHelper(offsetPos.down()));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                placeAnchor(pos.up(2), headAnchor.getValue());
            }
        }
        if (antiStep.getValue() && (BlockUtil.isMining(pos.up(2)) || !onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3))) {
                if (getHelper(pos.up(3), Direction.DOWN) != null) {
                    placeBlock(getHelper(pos.up(3)));
                }
            }
            placeBlock(pos.up(3));
        }
        if (down.getValue()) {
            BlockPos offsetPos = pos.down();
            placeBlock(offsetPos);
            if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos) && getHelper(offsetPos) != null)
                placeBlock(getHelper(offsetPos));
        }
        if (chestUp.getValue()) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up(2);
                if (!onlyBreaking.getValue() || BlockUtil.isMining(pos.up(2))) {
                    placeBlock(offsetPos);
                    if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos)) {
                        if (getHelper(offsetPos) != null) {
                            placeBlock(getHelper(offsetPos));
                        } else if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down()) && getHelper(offsetPos.down()) != null) {
                            placeBlock(getHelper(offsetPos.down()));
                        }
                    }
                }
            }
        }
        if (chest.getValue() && (!onlyGround.getValue() || target.isOnGround())) {
            for (Direction i : Direction.values()) {
                if (i == Direction.DOWN || i == Direction.UP) continue;
                BlockPos offsetPos = pos.offset(i).up();
                placeBlock(offsetPos);
                if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos)) {
                    if (getHelper(offsetPos) != null) {
                        placeBlock(getHelper(offsetPos));
                    } else
                    if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down()) && getHelper(offsetPos.down()) != null) {
                        placeBlock(getHelper(offsetPos.down()));
                    }
                }
            }
        }
        if (extend.getValue()) {
            for (int x : new int[]{1, 0, -1}) {
                for (int z : new int[]{1, 0, -1}) {
                    BlockPos offsetPos = pos.add(x, 0, z);
                    if (checkEntity(new BlockPos(offsetPos))) doTrap(offsetPos);
                }
            }
        }
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName().getString();
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (Rebirth.HUD.placement.getValue() == Placement.Strict && !BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
        }
        return null;
    }

    public BlockPos getHelper(BlockPos pos, Direction ignore) {
        if (!helper.getValue()) return null;
        for (Direction i : Direction.values()) {
            if (i == ignore) continue;
            if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
            if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
            if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
        }
        return null;
    }
    private boolean checkEntity(BlockPos pos) {
        if (mc.player.getBoundingBox().intersects(new Box(pos))) return false;
        for (Entity entity : mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (entity.isAlive())
                return true;
        }
        return false;
    }

    private void placeAnchor(BlockPos pos, boolean anchor) {
        if (pre.getValue()) ExtraAutoTrap.addBlock(pos);
        if (BlockUtil.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < multiPlace.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue())
            return;
        int old = mc.player.getInventory().selectedSlot;
        int block = anchor && getAnchor() != -1 ? getAnchor() : getBlock();
        if (block == -1) return;
        if (placeList.contains(pos)) return;
        if (!pre.getValue()) ExtraAutoTrap.addBlock(pos);
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.sync();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
    }
    private void placeBlock(BlockPos pos) {
        if (pre.getValue()) ExtraAutoTrap.addBlock(pos);
        if (BlockUtil.isMining(pos)) return;
        if (!BlockUtil.canPlace(pos, 6, breakCrystal.getValue())) return;
        if (!(progress < multiPlace.getValue())) return;
        if (MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > placeRange.getValue()) return;
        int old = mc.player.getInventory().selectedSlot;
        int block = getBlock();
        if (block == -1) return;
        if (placeList.contains(pos)) return;
        if (!pre.getValue()) ExtraAutoTrap.addBlock(pos);
        placeList.add(pos);
        CombatUtil.attackCrystal(pos, rotate.getValue(), usingPause.getValue());
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.sync();
        } else {
            doSwap(old);
        }
        timer.reset();
        progress++;
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

    private int getAnchor() {
        if (inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR);
        } else {
            return InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
        }
    }
}
