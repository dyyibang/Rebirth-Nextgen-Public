package me.rebirthclient.mod.modules.combat;

import com.mojang.authlib.GameProfile;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.api.util.*;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

import static me.rebirthclient.api.util.BlockUtil.getBlock;
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;
    public static BlockPos placePos;
    private final EnumSetting page =
            add(new EnumSetting("Page", Page.General));
    //General
    private final BooleanSetting noUsing =
            add(new BooleanSetting("NoUsing", v -> page.getValue() == Page.General));
    private final EnumSetting calcMode = add(new EnumSetting("CalcMode", AutoAnchor.CalcMode.Meteor, v -> page.getValue() == Page.General));
    private final SliderSetting antiSuicide =
            add(new SliderSetting("AntiSuicide", 0.0, 10.0, v -> page.getValue() == Page.General));
    private final SliderSetting targetRange =
            add(new SliderSetting("TargetRange", 0.0, 20.0, v -> page.getValue() == Page.General));
    private final SliderSetting updateDelay =
            add(new SliderSetting("UpdateDelay", 0, 1000, v -> page.getValue() == Page.General));
    private final SliderSetting calcDelay =
            add(new SliderSetting("CalcDelay", 0, 1000, v -> page.getValue() == Page.General));
    private final SliderSetting breakWall =
            add(new SliderSetting("WallRange", 0.0, 6.0, v -> page.getValue() == Page.General));

    //Rotate
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", v -> page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting newRotate =
            add(new BooleanSetting("YawStep", v -> rotate.isOpen() && page.getValue() == Page.Rotate));
    private final SliderSetting yawStep =
            add(new SliderSetting("Steps", 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting sync =
            add(new BooleanSetting("Sync", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final BooleanSetting checkLook =
            add(new BooleanSetting("CheckLook", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
    private final SliderSetting fov =
            add(new SliderSetting("Fov", 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));
    
    //Place
    private final BooleanSetting place =
            add(new BooleanSetting("Place", v -> page.getValue() == Page.Place));
    private final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 0, 1000, v -> page.getValue() == Page.Place && place.getValue()));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 0.0, 6, v -> page.getValue() == Page.Place && place.getValue()));
    private final SliderSetting placeMinDamage =
            add(new SliderSetting("PlaceMin", 0.0, 36.0, v -> page.getValue() == Page.Place && place.getValue()));
    private final SliderSetting placeMaxSelf =
            add(new SliderSetting("PlaceSelf", 0.0, 36.0, v -> page.getValue() == Page.Place && place.getValue()));
    private final EnumSetting autoSwap =
            add(new EnumSetting("AutoSwap", SwapMode.OFF, v -> page.getValue() == Page.Place && place.getValue()));
    private final BooleanSetting extraPlace =
            add(new BooleanSetting("SpamPlace", v -> page.getValue() == Page.Place && place.getValue()));
    private final SliderSetting ignoreTime =
            add(new SliderSetting("IgnoreTime", 0, 1000, v -> page.getValue() == Page.Place));
    //Break
    private final BooleanSetting Break =
            add(new BooleanSetting("Break", v -> page.getValue() == Page.Break));
    private final SliderSetting breakDelay =
            add(new SliderSetting("BreakDelay", 0, 1000, v -> page.getValue() == Page.Break && Break.getValue()));
    private final SliderSetting breakRange =
            add(new SliderSetting("BreakRange", 0.0, 6.0, v -> page.getValue() == Page.Break && Break.getValue()));
    private final SliderSetting breakMinDamage =
            add(new SliderSetting("BreakMin", 0.0, 36.0, v -> page.getValue() == Page.Break && Break.getValue()));
    private final SliderSetting breakMaxSelf =
            add(new SliderSetting("BreakSelf", 0.0, 36.0, v -> page.getValue() == Page.Break && Break.getValue()));
    //Render
    private final BooleanSetting render =
            add(new BooleanSetting("Render", v -> page.getValue() == Page.Render));
    private final BooleanSetting shrink =
            add(new BooleanSetting("Shrink", v -> page.getValue() == Page.Render && render.getValue()));
    private final BooleanSetting outline =
            add(new BooleanSetting("Outline", v -> page.getValue() == Page.Render && render.getValue()).setParent());
    private final SliderSetting outlineAlpha =
            add(new SliderSetting("OutlineAlpha", 0, 255, v -> outline.isOpen() && page.getValue() == Page.Render && render.getValue()));
    private final BooleanSetting box =
            add(new BooleanSetting("Box", v -> page.getValue() == Page.Render && render.getValue()).setParent());
    private final SliderSetting boxAlpha =
            add(new SliderSetting("BoxAlpha", 0, 255, v -> box.isOpen() && page.getValue() == Page.Render && render.getValue()));
    private final BooleanSetting reset =
            add(new BooleanSetting("Reset", v -> page.getValue() == Page.Render && render.getValue()));
    private final ColorSetting color =
            add(new ColorSetting("Color", new Color(255, 255, 255), v -> page.getValue() == Page.Render && render.getValue()));
    private final SliderSetting animationTime =
            add(new SliderSetting("AnimationTime", 0f, 8f, v -> page.getValue() == Page.Render && render.getValue()));
    private final SliderSetting startFadeTime =
            add(new SliderSetting("StartFadeTime", 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
    private final SliderSetting fadeTime =
            add(new SliderSetting("FadeTime", 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
    //Predict
    public final SliderSetting predictTicks =
            add(new SliderSetting("PredictTicks", 0, 10, v -> page.getValue() == Page.Predict));
    private final BooleanSetting terrainIgnore =
            add(new BooleanSetting("TerrainIgnore", v -> page.getValue() == Page.Predict));
    //Misc
    private final BooleanSetting slowFace =
            add(new BooleanSetting("SlowFace", v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting slowDelay =
            add(new SliderSetting("SlowDelay", 0, 2000, v -> page.getValue() == Page.Misc && slowFace.isOpen()));
    private final SliderSetting slowMinDamage =
            add(new SliderSetting("SlowMin", 0.0, 36.0, v -> page.getValue() == Page.Misc && slowFace.isOpen()));
    private final BooleanSetting forcePlace =
            add(new BooleanSetting("ForcePlace", v -> page.getValue() == Page.Misc).setParent());
    private final SliderSetting forceMaxHealth =
            add(new SliderSetting("ForceMaxHealth", 0, 36, v -> page.getValue() == Page.Misc && forcePlace.isOpen()));
    private final SliderSetting forceMin =
            add(new SliderSetting("ForceMin", 0.0, 36.0, v -> page.getValue() == Page.Misc && forcePlace.isOpen()));
    private final Timer delayTimer = new Timer();
    private final Timer calcTimer = new Timer();
    public static final Timer placeTimer = new Timer();
    public final Timer lastBreakTimer = new Timer();
    private final Timer noPosTimer = new Timer();
    private final FadeUtils fadeUtils = new FadeUtils(500);
    private final FadeUtils animation = new FadeUtils(500);
    double lastSize = 0;
    private PlayerEntity displayTarget;
    private float lastYaw = 0f;
    private float lastPitch = 0f;
    public float lastDamage;
    public Vec3d directionVec = null;
    private BlockPos renderPos = null;
    private Box lastBB = null;
    private Box nowBB = null;

    public AutoCrystal() {
        super("AutoCrystal", "Recode", Category.Combat);
        INSTANCE = this;
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
                && BlockUtil.getClickSideStrict(obsPos) != null
                && !BlockUtil.hasEntityBlockCrystal(boost, ignoreCrystal, ignoreItem)
                && !BlockUtil.hasEntityBlockCrystal(boost.up(), ignoreCrystal, ignoreItem)
                && (getBlock(boost) == Blocks.AIR || BlockUtil.hasEntityBlockCrystal(boost, false, ignoreItem) && getBlock(boost) == Blocks.FIRE    );
    }

    public boolean behindWall(BlockPos pos) {
        Vec3d testVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 2 * 0.85, pos.getZ() + 0.5);
        HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
        if (result == null || result.getType() == HitResult.Type.MISS) return false;
        return MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > breakWall.getValueFloat();
    }

    @Override
    public void onDisable() {
        lastYaw = Rebirth.RUN.lastYaw;
        lastPitch = Rebirth.RUN.lastPitch;
    }

    @Override
    public void onEnable() {
        lastYaw = Rebirth.RUN.lastYaw;
        lastPitch = Rebirth.RUN.lastPitch;
        lastBreakTimer.reset();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingEvent event) {
        update();
    }

    @Override
    public void onUpdate() {
        update();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        update();
        double quad = noPosTimer.passedMs(startFadeTime.getValue() * 1000L) ? fadeUtils.easeOutQuad() : 0;
        if (nowBB != null && render.getValue() && quad < 1) {
            Box bb = nowBB;
            if (shrink.getValue()) {
                bb = nowBB.shrink(quad * 0.5, quad * 0.5, quad * 0.5);
                bb = bb.shrink(-quad * 0.5, -quad * 0.5, -quad * 0.5);
            }
            if (this.box.getValue())
                Render3DUtil.drawBBFill(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (boxAlpha.getValue() * Math.abs(quad - 1))));
            if (outline.getValue())
                Render3DUtil.drawBBBox(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (outlineAlpha.getValue() * Math.abs(quad - 1))));
            //if (text.getValue() && lastPos != null) RenderUtil.drawText(String.valueOf(lastDamage), nowBB);
        } else if (reset.getValue()) nowBB = null;
    }

    @EventHandler()
    public void onRotate(RotateEvent event) {
        if (placePos != null && newRotate.getValue() && directionVec != null) {
            float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
            lastYaw = newAngle[0];
            lastPitch = newAngle[1];
            event.setYaw(lastYaw);
            event.setPitch(lastPitch);
        } else {
            lastYaw = Rebirth.RUN.lastYaw;
            lastPitch = Rebirth.RUN.lastPitch;
        }
    }

    private void update() {
        if (nullCheck()) return;
        animUpdate();
        if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
        if (noUsing.getValue() && EntityUtil.isUsing()) {
            lastBreakTimer.reset();
            placePos = null;
            return;
        }
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            placePos = null;
            return;
        }
        delayTimer.reset();
        if (calcTimer.passedMs(calcDelay.getValueInt())) {
            BlockPos breakPos = null;
            calcTimer.reset();
            placePos = null;
            lastDamage = 0f;
            ArrayList<PlayerAndPredict> list = new ArrayList<>();
            for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getRange())) {
                list.add(new PlayerAndPredict(target));
            }
            PlayerAndPredict self = new PlayerAndPredict(mc.player);
            if (list.isEmpty()) {
                lastBreakTimer.reset();
            }
            for (Entity crystal : mc.world.getEntities()) {
                if (!(crystal instanceof EndCrystalEntity)) continue;
                if (EntityUtil.getEyesPos().distanceTo(crystal.getPos()) > breakRange.getValue()) continue;
                if (!mc.player.canSee(crystal) && mc.player.distanceTo(crystal) > breakWall.getValue())
                    continue;
                for (PlayerAndPredict pap : list) {
                    float damage = calculateDamage(crystal.getPos(), pap.player, pap.predict);
                    float selfDamage = calculateDamage(crystal.getPos(), self.player, self.predict);
                    if (selfDamage > breakMaxSelf.getValue())
                        continue;
                    if (antiSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - antiSuicide.getValue())
                        continue;
                    if (damage < EntityUtil.getHealth(pap.player)) {
                        if (damage < getBreakDamage(pap.player)) continue;
                    }
                    if (breakPos == null || damage > lastDamage) {
                        displayTarget = pap.player;
                        breakPos = new BlockPosX(crystal.getPos());
                        lastDamage = damage;
                    }
                }
            }
            if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || findCrystal()) {
                for (BlockPos pos : BlockUtil.getSphere((float) placeRange.getValue() + 1)) {
                    lastBreakTimer.passedMs(ignoreTime.getValueInt());
                    if (!canPlaceCrystal(pos, !lastBreakTimer.passedMs(ignoreTime.getValueInt()), false)) continue;
                    if (behindWall(pos)) continue;
                    if (!canTouch(pos.down())) continue;
                    for (PlayerAndPredict pap : list) {
                        float damage = calculateDamage(pos, pap.player, pap.predict);
                        float selfDamage = calculateDamage(pos, self.player, self.predict);
                        if (selfDamage > placeMaxSelf.getValue())
                            continue;
                        if (antiSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - antiSuicide.getValue())
                            continue;
                        if (damage < EntityUtil.getHealth(pap.player)) {
                            if (damage < getPlaceDamage(pap.player)) continue;
                        }
                        if (placePos == null || damage > lastDamage) {
                            displayTarget = pap.player;
                            placePos = pos;
                            breakPos = null;
                            lastDamage = damage;
                        }
                    }
                }
            }
            if (breakPos != null) {
                doBreak(breakPos);
                if (extraPlace.getValue() && placePos != null) doPlace(placePos);
                return;
            }
        }
        if (placePos != null) {
            doCrystal(placePos);
        }
    }

    private boolean canTouch(BlockPos pos) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        return side != null && pos.toCenterPos().add(new Vec3d(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= placeRange.getValue();
    }

    private void animUpdate() {
        fadeUtils.setLength((long) (fadeTime.getValue() * 1000));
        if (placePos != null) {
            lastBB = new Box(new BlockPos(placePos.down()));
            noPosTimer.reset();
            if (nowBB == null) {
                nowBB = lastBB;
            }
            if (renderPos == null || !renderPos.equals(placePos)) {
                animation.setLength((animationTime.getValue() * 1000) <= 0 ? 0 :
                        (long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) <= 5 ?
                                (long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) * (animationTime.getValue() * 1000))
                                : (animationTime.getValue() * 5000L))
                );
                animation.reset();
                renderPos = placePos;
            }
        }
        if (!noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000))) {
            fadeUtils.reset();
        }
        double size = animation.easeOutQuad();
        if (nowBB != null && lastBB != null) {
            if (Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ) > 16) {
                nowBB = lastBB;
            }
            if (lastSize != size) {
                nowBB = new Box(nowBB.minX + (lastBB.minX - nowBB.minX) * size,
                        nowBB.minY + (lastBB.minY - nowBB.minY) * size,
                        nowBB.minZ + (lastBB.minZ - nowBB.minZ) * size,
                        nowBB.maxX + (lastBB.maxX - nowBB.maxX) * size,
                        nowBB.maxY + (lastBB.maxY - nowBB.maxY) * size,
                        nowBB.maxZ + (lastBB.maxZ - nowBB.maxZ) * size
                );
                lastSize = size;
            }
        }
    }
    public void doCrystal(BlockPos pos) {
        if (canPlaceCrystal(pos, false, true)) {
            if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || findCrystal()) {
                doPlace(pos);
            }
        } else {
            doBreak(pos);
        }
    }
    public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
        return calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), player, predict);
    }

    public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
        if (terrainIgnore.getValue()) {
            CombatUtil.terrainIgnore = true;
        }
        float damage = 0;
        switch ((AutoAnchor.CalcMode) calcMode.getValue()) {
            case Meteor -> damage = (float) MeteorDamageUtil.crystalDamage(player, pos, predict);
            case Thunder -> damage = DamageUtil.calculateDamage(pos, player, predict, 6);
        }
        CombatUtil.terrainIgnore = false;
        return damage;
    }

    private double getPlaceDamage(PlayerEntity target) {
        if (!PacketMine.INSTANCE.obsidian.isPressed() && slowFace.getValue() && lastBreakTimer.passedMs((long) slowDelay.getValue())) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue() && !PacketMine.INSTANCE.obsidian.isPressed()) {
            return forceMin.getValue();
        }
        return placeMinDamage.getValue();
    }

    private double getBreakDamage(PlayerEntity target) {
        if (slowFace.getValue() && lastBreakTimer.passedMs((long) slowDelay.getValue()) && !PacketMine.INSTANCE.obsidian.isPressed()) {
            return slowMinDamage.getValue();
        }
        if (forcePlace.getValue() && EntityUtil.getHealth(target) <= forceMaxHealth.getValue() && !PacketMine.INSTANCE.obsidian.isPressed()) {
            return forceMin.getValue();
        }
        return breakMinDamage.getValue();
    }

    private boolean findCrystal() {
        if (autoSwap.getValue() == SwapMode.OFF) return false;
        return getCrystal() != -1;
    }

    private void doBreak(BlockPos pos) {
        lastBreakTimer.reset();
        if (!Break.getValue()) return;
        for (EndCrystalEntity entity : mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
            if (rotate.getValue()) {
                if (!faceVector(entity.getPos().add(0, 0.25, 0))) return;
            }
            if (!CombatUtil.breakTimer.passedMs((long) breakDelay.getValue())) return;
            CombatUtil.breakTimer.reset();
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            if (!placeTimer.passedMs((long) placeDelay.getValue()) || !extraPlace.getValue())
                return;
            if (lastDamage >= placeMinDamage.getValueFloat() && placePos != null) {
                doPlace(placePos);
            }
            break;
        }
    }

    private void doPlace(BlockPos pos) {
        if (!place.getValue()) return;
        if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !findCrystal()) {
            return;
        }
        if (!canTouch(pos.down())) {
            return;
        }
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        Vec3d vec = obsPos.toCenterPos().add(facing.getVector().getX() * 0.5,facing.getVector().getY() * 0.5,facing.getVector().getZ() * 0.5);
        if (rotate.getValue()) {
            if (!faceVector(vec)) return;
        }
        if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
        placeTimer.reset();
        if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
            placeCrystal(pos);
        } else if (findCrystal()) {
            int old = mc.player.getInventory().selectedSlot;
            int crystal = getCrystal();
            if (crystal == -1) return;
            doSwap(crystal);
            placeCrystal(pos);
            if (autoSwap.getValue() == SwapMode.SILENT) {
                doSwap(old);
            } else if (autoSwap.getValue() == SwapMode.Inventory) {
                doSwap(crystal);
                EntityUtil.sync();
            }
        }
    }

    private void doSwap(int slot) {
        if (autoSwap.getValue() == SwapMode.SILENT || autoSwap.getValue() == SwapMode.NORMAL) {
            InventoryUtil.doSwap(slot);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
        }
    }
    private int getCrystal() {
        if (autoSwap.getValue() == SwapMode.SILENT || autoSwap.getValue() == SwapMode.NORMAL) {
            return InventoryUtil.findItem(Items.END_CRYSTAL);
        } else if (autoSwap.getValue() == SwapMode.Inventory) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return -1;
    }
    public void placeCrystal(BlockPos pos) {
        //PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        Direction facing = BlockUtil.getClickSide(obsPos);
        BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }

    public enum Page {
        General,
        Rotate,
        Place,
        Break,
        Misc,
        Predict,
        Render
    }

    public enum SwapMode {
        OFF, NORMAL, SILENT, Inventory
    }

    public boolean faceVector(Vec3d directionVec) {
        if (!newRotate.getValue()) {
            EntityUtil.faceVector(directionVec);
            return true;
        } else {
            this.directionVec = directionVec;
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
                if (sync.getValue()) EntityUtil.sendYawAndPitch(angle[0], angle[1]);
                return true;
            }
        }
        return !checkLook.getValue();
    }

    private float[] injectStep(float[] angle, float steps) {
        if (steps < 0.01f) steps = 0.01f;

        if (steps > 1) steps = 1;

        if (steps < 1 && angle != null) {
            float packetYaw = lastYaw;
            float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);

            if (Math.abs(diff) > 90 * steps) {
                angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
            }

            float packetPitch = lastPitch;
            diff = angle[1] - packetPitch;
            if (Math.abs(diff) > 90 * steps) {
                angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
            }
        }

        return new float[]{
                angle[0],
                angle[1]
        };
    }

    public class PlayerAndPredict {
        PlayerEntity player;
        PlayerEntity predict;
        public PlayerAndPredict(PlayerEntity player) {
            this.player = player;
            if (predictTicks.getValueFloat() > 0) {
                predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
                predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
                predict.setHealth(player.getHealth());
                predict.prevX = player.prevX;
                predict.prevZ = player.prevZ;
                predict.prevY = player.prevY;
                predict.setOnGround(player.isOnGround());
                predict.getInventory().clone(player.getInventory());
                predict.setPose(player.getPose());
                for (StatusEffectInstance se : player.getStatusEffects()) {
                    predict.addStatusEffect(se);
                }
            } else {
                predict = player;
            }
        }
    }
}
