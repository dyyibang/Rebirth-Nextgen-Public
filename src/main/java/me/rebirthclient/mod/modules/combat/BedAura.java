/**
 * Anti-Invis Module
 */
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
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

import static me.rebirthclient.api.util.BlockUtil.getWorldActionId;

public class BedAura extends Module {
	public static BedAura INSTANCE;
	public final EnumSetting page = add(new EnumSetting("Page", Page.General));
	//General
	private final BooleanSetting noUsing =
			add(new BooleanSetting("NoUsing", v -> page.getValue() == Page.General));
	private final EnumSetting calcMode = add(new EnumSetting("CalcMode", AutoAnchor.CalcMode.Meteor, v -> page.getValue() == Page.General));
	private final SliderSetting antiSuicide =
			add(new SliderSetting("AntiSuicide", 3.0, 0.0, 10.0, v -> page.getValue() == Page.General));
	private final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, v -> page.getValue() == Page.General));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 50, 0, 1000, v -> page.getValue() == Page.General));
	private final SliderSetting calcDelay =
			add(new SliderSetting("CalcDelay", 200, 0, 1000, v -> page.getValue() == Page.General));
	private final BooleanSetting inventorySwap =
			add(new BooleanSetting("InventorySwap", v -> page.getValue() == Page.General));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("CheckMine", v -> page.getValue() == Page.General));
	private final BooleanSetting selfGround =
			add(new BooleanSetting("SelfGround", v -> page.getValue() == Page.General));
	//Rotate
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", v -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting newRotate =
			add(new BooleanSetting("NewRotate", v -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting yawStep =
			add(new SliderSetting("YawStep", 0.1f, 1.0f, 0.01f, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting sync =
			add(new BooleanSetting("Sync", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkLook =
			add(new BooleanSetting("CheckLook", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

	//Calc
	private final BooleanSetting place =
			add(new BooleanSetting("Place", v -> page.getValue() == Page.Calc));
	private final SliderSetting placeDelay =
			add(new SliderSetting("PlaceDelay", 300, 0, 1000, v -> page.getValue() == Page.Calc && place.getValue()));
	private final BooleanSetting Break =
			add(new BooleanSetting("Break", v -> page.getValue() == Page.Calc));
	private final SliderSetting breakDelay =
			add(new SliderSetting("BreakDelay", 300, 0, 1000, v -> page.getValue() == Page.Calc && Break.getValue()));
	private final SliderSetting range =
			add(new SliderSetting("Range", 5.0, 0.0, 6, v -> page.getValue() == Page.Calc));
	private final SliderSetting placeMinDamage =
			add(new SliderSetting("MinDamage", 5.0, 0.0, 36.0, v -> page.getValue() == Page.Calc));
	private final SliderSetting placeMaxSelf =
			add(new SliderSetting("MaxSelfDamage", 12.0, 0.0, 36.0, v -> page.getValue() == Page.Calc));
	private final BooleanSetting smart =
			add(new BooleanSetting("Smart", v -> page.getValue() == Page.Calc));
	private final BooleanSetting breakOnlyHasCrystal =
			add(new BooleanSetting("OnlyHasBed", v -> page.getValue() == Page.Calc && Break.getValue()));
	//Render
	private final BooleanSetting render =
			add(new BooleanSetting("Render", v -> page.getValue() == Page.Render));
	private final BooleanSetting shrink =
			add(new BooleanSetting("Shrink", v -> page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting outline =
			add(new BooleanSetting("Outline", v -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting outlineAlpha =
			add(new SliderSetting("OutlineAlpha", 150, 0, 255, v -> outline.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting box =
			add(new BooleanSetting("Box", v -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting boxAlpha =
			add(new SliderSetting("BoxAlpha", 70, 0, 255, v -> box.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting reset =
			add(new BooleanSetting("Reset", v -> page.getValue() == Page.Render && render.getValue()));
	private final ColorSetting color =
			add(new ColorSetting("Color", new Color(255, 255, 255), v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting animationTime =
			add(new SliderSetting("AnimationTime", 2f, 0f, 8f, v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting startFadeTime =
			add(new SliderSetting("StartFadeTime", 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting fadeTime =
			add(new SliderSetting("FadeTime", 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	//Predict
	private final SliderSetting predictTicks =
			add(new SliderSetting("PredictTicks", 4, 0, 10, v -> page.getValue() == Page.Predict));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", v -> page.getValue() == Page.Predict));
	public BedAura() {
		super("BedAura", Category.Combat);
		INSTANCE = this;
	}
	public static BlockPos placePos;
	private final Timer delayTimer = new Timer();
	private final Timer calcTimer = new Timer();
	private final Timer breakTimer = new Timer();
	private final Timer placeTimer = new Timer();
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

	@Override
	public void onDisable() {
		lastYaw = Rebirth.RUN.lastYaw;
		lastPitch = Rebirth.RUN.lastPitch;
	}

	@Override
	public void onEnable() {
		lastYaw = Rebirth.RUN.lastYaw;
		lastPitch = Rebirth.RUN.lastPitch;
		
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

	@EventHandler
	public void onUpdateWalking(UpdateWalkingEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	private void update() {
		if (nullCheck()) return;
		animUpdate();
		if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
		if (noUsing.getValue() && EntityUtil.isUsing()) {
			placePos = null;
			return;
		}
		if (mc.player.isSneaking()) {
			placePos = null;
			return;
		}
		if(selfGround.getValue() &&  !mc.player.isOnGround()){
			placePos = null;
			return;
		}
		if (mc.world.getRegistryKey().equals(World.OVERWORLD)) {
			placePos = null;
			return;
		}
		if (breakOnlyHasCrystal.getValue() && getBed() == -1) {
			placePos = null;
			return;
		}
		delayTimer.reset();
		if (calcTimer.passedMs(calcDelay.getValueInt())) {
			calcTimer.reset();
			placePos = null;
			lastDamage = 0f;
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getRange())) {
				list.add(new PlayerAndPredict(target));
			}
			PlayerAndPredict self = new PlayerAndPredict(mc.player);
			for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
				if (!canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) continue;
				for (PlayerAndPredict pap : list) {
					float damage = calculateDamage(pos, pap.player, pap.predict);
					float selfDamage = calculateDamage(pos, self.player, self.predict);
					if (selfDamage > placeMaxSelf.getValue())
						continue;
					if (antiSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - antiSuicide.getValue())
						continue;
					if (damage < EntityUtil.getHealth(pap.player)) {
						if (damage < placeMinDamage.getValueFloat()) continue;
						if (smart.getValue()) {
							if (damage < selfDamage) {
								continue;
							}
						}
					}
					if (placePos == null || damage > lastDamage) {
						displayTarget = pap.player;
						placePos = pos;
						lastDamage = damage;
					}
				}
			}
		}
		if (placePos != null) {
			doBed(placePos);
		}
	}

	public void doBed(BlockPos pos) {
		if (canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) {
			if (getBed() != -1) {
				doPlace(pos);
			}
		} else {
			doBreak(pos);
		}
	}

	private void doBreak(BlockPos pos) {
		if (!Break.getValue()) return;
		if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
			Direction side = BlockUtil.getClickSide(pos);
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!breakTimer.passedMs((long) breakDelay.getValue())) return;
			breakTimer.reset();
			mc.player.swingHand(Hand.MAIN_HAND);
			BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
			mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
		}
	}

	private void doPlace(BlockPos pos) {
		if (!place.getValue()) return;
		int bedSlot;
		if ((bedSlot = getBed()) == -1) {
			placePos = null;
			return;
		}

		int oldSlot = mc.player.getInventory().selectedSlot;
		Direction facing = null;
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if (BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
				facing = i;
				break;
			}
		}
		if (facing != null) {
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + Direction.UP.getVector().getX() * 0.5, pos.getY() + 0.5 + Direction.UP.getVector().getY() * 0.5, pos.getZ() + 0.5 + Direction.UP.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
			placeTimer.reset();
			doSwap(bedSlot);
			AutoPush.pistonFacing(facing.getOpposite());
			BlockUtil.clickBlock(pos.offset(facing).down(), Direction.UP, false);
			if (rotate.getValue()) {
				EntityUtil.faceVector(directionVec);
			}
			if (inventorySwap.getValue()) {
				doSwap(bedSlot);
				EntityUtil.sync();
			} else {
				doSwap(oldSlot);
			}
		}
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
		} else if (reset.getValue()) nowBB = null;
	}
	private void animUpdate() {
		fadeUtils.setLength((long) (fadeTime.getValue() * 1000));
		if (placePos != null) {
			lastBB = new Box(placePos);
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
	public int getBed() {
		return inventorySwap.getValue() ? InventoryUtil.findClassInventorySlot(BedItem.class) : InventoryUtil.findClass(BedItem.class);
	}

	private void doSwap(int slot) {
		if (inventorySwap.getValue()) {
			mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
		} else {
			InventoryUtil.doSwap(slot);
		}
	}

	public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
		BlockState oldState = BlockUtil.getState(pos);
		mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
		float damage = calculateDamage(pos.toCenterPos(), player, predict);
		mc.world.setBlockState(pos, oldState);
		return damage;
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
	
	private boolean canPlaceBed(BlockPos pos) {
		if (BlockUtil.canReplace(pos) && (!checkMine.getValue() || !BlockUtil.isMining(pos))) {
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (!BlockUtil.isStrictDirection(pos.offset(i).down(), Direction.UP)) continue;
				if (BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
					return true;
				}
			}
		}
		return false;
	}
	

	public enum Page {
		General,
		Rotate,
		Calc,
		Predict,
		Render
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
			float diff = MathHelper.wrapDegrees(angle[0] - lastYaw);

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
