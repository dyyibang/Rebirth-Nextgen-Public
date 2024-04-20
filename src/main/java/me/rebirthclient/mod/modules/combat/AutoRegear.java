package me.rebirthclient.mod.modules.combat;

import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.InventoryUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoRegear extends Module {
     private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable"));
    private final SliderSetting disableTime =
            add(new SliderSetting("DisableTime", 0, 1000));
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate"));
    private final BooleanSetting place = add(new BooleanSetting("Place"));
    private final BooleanSetting preferOpen = add(new BooleanSetting("PerferOpen"));
    private final BooleanSetting open = add(new BooleanSetting("Open"));
    private final SliderSetting range = add(new SliderSetting("Range", 0.0f, 6f));
    private final SliderSetting minRange = add(new SliderSetting("MinRange", 0.0f, 3f));
    private final BooleanSetting mine = add(new BooleanSetting("Mine"));
    private final BooleanSetting take = add(new BooleanSetting("Take"));
    private final BooleanSetting smart = add(new BooleanSetting("Smart", v -> take.getValue()).setParent());
    private final SliderSetting crystal = add(new SliderSetting("Crystal", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting exp = add(new SliderSetting("Exp", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting totem = add(new SliderSetting("Totem", 0, 36, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting gapple = add(new SliderSetting("Gapple", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting endChest = add(new SliderSetting("EndChest", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting web = add(new SliderSetting("Web", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting glowstone = add(new SliderSetting("Glowstone", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting anchor = add(new SliderSetting("Anchor", 0, 512, v -> take.getValue() && smart.isOpen()));
    private final SliderSetting pearl = add(new SliderSetting("Pearl", 0, 64, v -> take.getValue() && smart.isOpen()));
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

    public AutoRegear() {
        super("AutoRegear", "Auto place shulker and replenish", Category.Combat);
    }

    public int findShulker() {
        final AtomicInteger atomicInteger = new AtomicInteger(-1);
        if (InventoryUtil.findClass(ShulkerBoxBlock.class) != -1) {
            atomicInteger.set(InventoryUtil.findClass(ShulkerBoxBlock.class));
        }
        return atomicInteger.get();
    }

    private final Timer timer = new Timer();
    BlockPos placePos = null;
    private final Timer disableTimer = new Timer();
    @Override
    public void onEnable() {
        if (nullCheck()){
            return;
        }
        disableTimer.reset();
        placePos = null;
        int oldSlot = mc.player.getInventory().selectedSlot;
        if (!this.place.getValue()) {
            return;
        }
        double distance = 100;
        BlockPos bestPos = null;
        for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
            if (!BlockUtil.isAir(pos.up())) continue;
            if (preferOpen.getValue() && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) return;
            if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < minRange.getValue()) continue;
            if (!BlockUtil.clientCanPlace(pos, false)
                    || !BlockUtil.isStrictDirection(pos.offset(Direction.DOWN), Direction.UP)
                    || !BlockUtil.canClick(pos.offset(Direction.DOWN))
            ) continue;
            if (bestPos == null || MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos())) < distance) {
                distance = MathHelper.sqrt((float) mc.player.squaredDistanceTo(pos.toCenterPos()));
                bestPos = pos;
            }
        }
        if (bestPos != null) {
            if (this.findShulker() == -1) {
                CommandManager.sendChatMessage("\u00a7c[!] No shulkerbox found");
                return;
            }
            InventoryUtil.doSwap(this.findShulker());
            placeBlock(bestPos);
            placePos = bestPos;
            InventoryUtil.doSwap(oldSlot);
            timer.reset();
        } else {
            CommandManager.sendChatMessage("\u00a7c[!] No place pos found");
        }
    }

    private void update() {
        this.stealCountList[0] = (int) (this.crystal.getValue() - getItemCount(Items.END_CRYSTAL));
        this.stealCountList[1] = (int) (this.exp.getValue() - getItemCount(Items.EXPERIENCE_BOTTLE));
        this.stealCountList[2] = (int) (this.totem.getValue() - getItemCount(Items.TOTEM_OF_UNDYING));
        this.stealCountList[3] = (int) (this.gapple.getValue() - getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
        this.stealCountList[4] = (int) (this.endChest.getValue() - getItemCount(Item.fromBlock(Blocks.ENDER_CHEST)));
        this.stealCountList[5] = (int) (this.web.getValue() - getItemCount(Item.fromBlock(Blocks.COBWEB)));
        this.stealCountList[6] = (int) (this.glowstone.getValue() - getItemCount(Item.fromBlock(Blocks.GLOWSTONE)));
        this.stealCountList[7] = (int) (this.anchor.getValue() - getItemCount(Item.fromBlock(Blocks.RESPAWN_ANCHOR)));
        this.stealCountList[8] = (int) (this.pearl.getValue() - getItemCount(Items.ENDER_PEARL));
    }

    @Override
    public void onDisable() {
        opend = false;
        if (mine.getValue()) {
            if (placePos != null) {
                PacketMine.INSTANCE.mine(placePos);
            }
        }
    }

    boolean opend = false;
    @Override
    public void onUpdate() {
        if (smart.getValue()) update();
        if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
            if (opend) {
                opend = false;
                if (autoDisable.getValue()) disable2();
                return;
            }
            if (open.getValue()) {
                if (placePos != null && MathHelper.sqrt((float) mc.player.squaredDistanceTo(placePos.toCenterPos())) <= range.getValue() && mc.world.isAir(placePos.up()) && (!timer.passedMs(500) || mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock)) {
                    if (mc.world.getBlockState(placePos).getBlock() instanceof ShulkerBoxBlock) {
                        BlockUtil.clickBlock(placePos, BlockUtil.getClickSide(placePos), rotate.getValue());
                    }
                } else {
                    boolean found = false;
                    for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
                        if (!BlockUtil.isAir(pos.up())) continue;
                        if (mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                            BlockUtil.clickBlock(pos, BlockUtil.getClickSide(pos), rotate.getValue());
                            found = true;
                            break;
                        }
                    }
                    if (!found && autoDisable.getValue()) this.disable2();
                }
            } else if (!this.take.getValue()) {
                if (autoDisable.getValue()) this.disable2();
            }
            return;
        }
        opend = true;
        if (!this.take.getValue()) {
            if (autoDisable.getValue()) this.disable2();
            return;
        }
        boolean take = false;
        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulker) {
            for (Slot slot : shulker.slots) {
                if (slot.id < 27 && !slot.getStack().isEmpty() && (!smart.getValue() || needSteal(slot.getStack()))) {
                    mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                    take = true;
                }
            }
        }
        if (autoDisable.getValue() && !take) this.disable2();
    }

    private void disable2() {
        if (disableTimer.passedMs(disableTime.getValueInt()))
            disable();
    }
    private boolean needSteal(final ItemStack i) {
        if (i.getItem().equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
            stealCountList[0] = stealCountList[0] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
            stealCountList[1] = stealCountList[1] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
            stealCountList[2] = stealCountList[2] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE) && this.stealCountList[3] > 0) {
            stealCountList[3] = stealCountList[3] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.ENDER_CHEST)) && this.stealCountList[4] > 0) {
            stealCountList[4] = stealCountList[4] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.COBWEB)) && this.stealCountList[5] > 0) {
            stealCountList[5] = stealCountList[5] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.GLOWSTONE)) && this.stealCountList[6] > 0) {
            stealCountList[6] = stealCountList[6] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Item.fromBlock(Blocks.RESPAWN_ANCHOR)) && this.stealCountList[7] > 0) {
            stealCountList[7] = stealCountList[7] - i.getCount();
            return true;
        }
        if (i.getItem().equals(Items.ENDER_PEARL) && this.stealCountList[8] > 0) {
            stealCountList[8] = stealCountList[8] - i.getCount();
            return true;
        }
        return false;
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() != item) continue;
            count = count + entry.getValue().getCount();
        }
        return count;
    }

    private void placeBlock(BlockPos pos) {
        BlockUtil.placedPos.add(pos);
        boolean sneak = BlockUtil.shiftBlocks.contains(BlockUtil.getBlock(pos)) && !mc.player.isSneaking();
        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        BlockUtil.clickBlock(pos.offset(Direction.DOWN), Direction.UP, rotate.getValue());
        if (sneak)
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }
}