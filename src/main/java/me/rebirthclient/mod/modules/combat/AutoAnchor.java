package me.rebirthclient.mod.modules.combat;

import com.mojang.authlib.GameProfile;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.RotateEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingEvent;
import me.rebirthclient.api.util.*;
import me.rebirthclient.asm.accessors.IPlayerMoveC2SPacket;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

import static me.rebirthclient.api.util.BlockUtil.*;
public class AutoAnchor extends Module {
	public static AutoAnchor INSTANCE;
	public final EnumSetting page = add(new EnumSetting("Page", Page.General));
	public final SliderSetting range =
			add(new SliderSetting("Range", 0.0, 6.0, 0.1, v -> page.getValue() == Page.General));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 0.0, 16.0, 0.1, v -> page.getValue() == Page.General));
	private final BooleanSetting breakCrystal =
			add(new BooleanSetting("BreakCrystal", v -> page.getValue() == Page.General));
	private final BooleanSetting mineSpam =
			add(new BooleanSetting("MineSpam", v -> page.getValue() == Page.General));
	private final BooleanSetting spam =
			add(new BooleanSetting("Spam", v -> page.getValue() == Page.General));
	private final BooleanSetting spamPlace =
			add(new BooleanSetting("SpamPlace", v -> page.getValue() == Page.General));
	private final BooleanSetting inSpam =
			add(new BooleanSetting("InSpam", v -> page.getValue() == Page.General && spamPlace.getValue()));
	private final BooleanSetting usingPause =
			add(new BooleanSetting("UsingPause", v -> page.getValue() == Page.General));
	private final SliderSetting placeDelay =
			add(new SliderSetting("PlaceDelay", 0.0, 0.5, 0.01, v -> page.getValue() == Page.General));
	private final SliderSetting chargingDelay =
			add(new SliderSetting("ChargingDelay", 0.0, 0.5, 0.01, v -> page.getValue() == Page.General));
	private final SliderSetting breakDelay =
			add(new SliderSetting("BreakDelay", 0.0, 0.5, 0.01, v -> page.getValue() == Page.General));
	private final SliderSetting spamDelay =
			add(new SliderSetting("SpamDelay", 0.0, 0.5, 0.01, v -> page.getValue() == Page.General));
	private final SliderSetting calcDelay =
			add(new SliderSetting("CalcDelay", 0.0, 0.5, 0.01, v -> page.getValue() == Page.General));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 0, 1000, v -> page.getValue() == Page.General));

	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", v -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting newRotate =
			add(new BooleanSetting("NewRotate", v -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting yawStep =
			add(new SliderSetting("YawStep", 0.1f, 1.0f, 0.01, v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting sync =
			add(new BooleanSetting("Sync", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkLook =
			add(new BooleanSetting("CheckLook", v -> rotate.isOpen() && newRotate.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 0f, 30f, v -> rotate.isOpen() && newRotate.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

	private final EnumSetting calcMode = add(new EnumSetting("CalcMode", CalcMode.Meteor, v -> page.getValue() == Page.Calc));
	private final BooleanSetting noSuicide =
			add(new BooleanSetting("NoSuicide", v -> page.getValue() == Page.Calc));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", v -> page.getValue() == Page.Calc));
	public final SliderSetting minDamage =
			add(new SliderSetting("PlaceMin", 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc));
	public final SliderSetting breakMin =
			add(new SliderSetting("BreakMin", 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc));
	public final SliderSetting headDamage =
			add(new SliderSetting("HeadDamage", 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc));
	private final SliderSetting minPrefer =
			add(new SliderSetting("MinPrefer", 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc));
	private final SliderSetting maxSelfDamage =
			add(new SliderSetting("MaxSelf", 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc));
	public final SliderSetting predictTicks =
			add(new SliderSetting("PredictTicks", 0.0, 50, 1, v -> page.getValue() == Page.Calc));

	private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100), v -> page.getValue() == Page.Render));
	private final BooleanSetting outline = add(new BooleanSetting("Outline", v -> page.getValue() == Page.Render));
	private final BooleanSetting box = add(new BooleanSetting("Fill", v -> page.getValue() == Page.Render));

	private final Timer updateTimer = new Timer().reset();
	private final Timer delayTimer = new Timer().reset();
	private final Timer calcTimer = new Timer().reset();
	public Vec3d directionVec = null;
	private float lastYaw = 0;
	private float lastPitch = 0;
	public AutoAnchor() {
		super("AutoAnchor", Category.Combat);
		INSTANCE = this;
	}
	private final ArrayList<BlockPos> chargeList = new ArrayList<>();
	public BlockPos currentPos;

	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		update();
		if (currentPos != null) {
			Render3DUtil.draw3DBox(matrixStack, new Box(currentPos), color.getValue(), outline.getValue(), box.getValue());
		}
	}

	@EventHandler()
	public void onRotate(RotateEvent event) {
		if (currentPos != null && newRotate.getValue() && directionVec != null) {
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

	@EventHandler(priority = -199)
	public void onPacketSend(PacketEvent.Send event) {
		if (event.isCancel()) return;
		if (newRotate.getValue() && currentPos != null && directionVec != null && !EntityUtil.rotating && Rebirth.HUD.rotatePlus.getValue()) {
			if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
				if (!packet.changesLook()) return;
				float yaw = packet.getYaw(114514);
				float pitch = packet.getPitch(114514);
				if (yaw == mc.player.getYaw() && pitch == mc.player.getPitch()) {
					float[] angle = injectStep(EntityUtil.getLegitRotations(directionVec), yawStep.getValueFloat());
					((IPlayerMoveC2SPacket) event.getPacket()).setYaw(angle[0]);
					((IPlayerMoveC2SPacket) event.getPacket()).setPitch(angle[1]);
				}
			}
		}
	}

	@Override
	public void onDisable() {
		lastYaw = Rebirth.RUN.lastYaw;
		lastPitch = Rebirth.RUN.lastPitch;
		currentPos = null;
	}

	@Override
	public void onEnable() {
		lastYaw = Rebirth.RUN.lastYaw;
		lastPitch = Rebirth.RUN.lastPitch;
		currentPos = null;
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	public void update() {
		if (!updateTimer.passedMs((long) updateDelay.getValue())) return;
		int anchor = InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = InventoryUtil.findBlock(Blocks.GLOWSTONE);
		int unBlock;
		int old = mc.player.getInventory().selectedSlot;
		if (anchor == -1) {
			currentPos = null;
			return;
		}
		if (glowstone == -1) {
			currentPos = null;
			return;
		}
		if ((unBlock = InventoryUtil.findUnBlock()) == -1) {
			currentPos = null;
			return;
		}
		if (mc.player.isSneaking()) {
			currentPos = null;
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			currentPos = null;
			return;
		}
		updateTimer.reset();
		PlayerAndPredict selfPredict = new PlayerAndPredict(mc.player);
		if (calcTimer.passed((long) (calcDelay.getValueFloat() * 1000))) {
			calcTimer.reset();
			currentPos = null;
			double placeDamage = minDamage.getValue();
			double breakDamage = breakMin.getValue();
			boolean anchorFound = false;
			java.util.List<PlayerEntity> enemies = CombatUtil.getEnemies(targetRange.getValue());
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity player : enemies) {
				list.add(new PlayerAndPredict(player));
			}
			for (PlayerAndPredict pap : list) {
				BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
				if (canPlace(pos, range.getValue(), breakCrystal.getValue()) || getBlock(pos) == Blocks.RESPAWN_ANCHOR && BlockUtil.getClickSideStrict(pos) != null) {
					double selfDamage;
					if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
						continue;
					}
					if (getAnchorDamage(pos, pap.player, pap.predict) > headDamage.getValueFloat()) {
						currentPos = pos;
						break;
					}
				}
			}
			if (currentPos == null) {
				for (BlockPos pos : getSphere(range.getValueFloat())) {
					for (PlayerAndPredict pap : list) {
						double selfDamage;
						if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
							continue;
						}
						if (getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
							if (anchorFound) continue;
							if (!canPlace(pos, range.getValue(), breakCrystal.getValue())) continue;

							BlockState preState = getState(pos);
							mc.world.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState());
							boolean skip = BlockUtil.getClickSideStrict(pos) == null;
							mc.world.setBlockState(pos, preState);
							if (skip) continue;

							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (damage >= placeDamage) {
								if (AutoCrystal.placePos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									placeDamage = damage;
									currentPos = pos;
								}
							}
						} else {
							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (getClickSideStrict(pos) == null) continue;
							if (damage >= breakDamage) {
								if (damage >= minPrefer.getValue()) anchorFound = true;
								if (!anchorFound && damage < placeDamage) {
									continue;
								}
								if (AutoCrystal.placePos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									breakDamage = damage;
									currentPos = pos;
								}
							}
						}
					}
				}
			}
		}

		if (currentPos != null) {
			if (breakCrystal.getValue()) CombatUtil.attackCrystal(new BlockPos(currentPos), rotate.getValue(), false);
			boolean spam = this.spam.getValue() || mineSpam.getValue() && Rebirth.BREAK.isMining(currentPos);
			if (spam) {
				if (!delayTimer.passed((long) (spamDelay.getValueFloat() * 1000))) {
					return;
				}
				delayTimer.reset();
				if (canPlace(currentPos, range.getValue(), breakCrystal.getValue())) {
					placeBlock(currentPos, rotate.getValue(), anchor);
				}
				if (!chargeList.contains(currentPos)) {
					delayTimer.reset();
					clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), glowstone);
					chargeList.add(currentPos);
				}
				chargeList.remove(currentPos);
				clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), unBlock);
				if (spamPlace.getValue()&& inSpam.getValue()) {
					BlockState preState = getState(currentPos);
					mc.world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
					placeBlock(currentPos, rotate.getValue(), anchor);
					mc.world.setBlockState(currentPos, preState);
				}

			} else {
				if (canPlace(currentPos, range.getValue(), breakCrystal.getValue())) {
					if (!delayTimer.passed((long) (placeDelay.getValueFloat() * 1000))) {
						return;
					}
					delayTimer.reset();
					placeBlock(currentPos, rotate.getValue(), anchor);
				} else if (getBlock(currentPos) == Blocks.RESPAWN_ANCHOR) {
					if (!chargeList.contains(currentPos)) {
						if (!delayTimer.passed((long) (chargingDelay.getValueFloat() * 1000))) {
							return;
						}
						delayTimer.reset();
						clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), glowstone);
						chargeList.add(currentPos);
					} else {
						if (!delayTimer.passed((long) (breakDelay.getValueFloat() * 1000))) {
							return;
						}
						delayTimer.reset();
						chargeList.remove(currentPos);
						clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), unBlock);
						if (spamPlace.getValue()) {
							BlockState preState = getState(currentPos);
							mc.world.setBlockState(currentPos, Blocks.AIR.getDefaultState());
							placeBlock(currentPos, rotate.getValue(), anchor);
							mc.world.setBlockState(currentPos, preState);
						}
					}
				}
			}
			InventoryUtil.doSwap(old);
		}
	}

	public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target, PlayerEntity predict) {
		if (terrainIgnore.getValue()) {
			CombatUtil.terrainIgnore = true;
		}
		double damage = 0;
		switch ((CalcMode) calcMode.getValue()) {
			case Meteor -> damage = MeteorDamageUtil.anchorDamage(target, anchorPos, predict);
			case Thunder -> damage = DamageUtil.anchorDamage(anchorPos, target, predict);
		}
		CombatUtil.terrainIgnore = false;
		return damage;
	}
	public void placeBlock(BlockPos pos, boolean rotate, int slot) {
		if (airPlace()) {
			for (Direction i : Direction.values()) {
				if (mc.world.isAir(pos.offset(i))) {
					clickBlock(pos, i, rotate, slot);
					return;
				}
			}
		}
		Direction side = getPlaceSide(pos);
		if (side == null) return;
		BlockUtil.placedPos.add(pos);
		clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
	}
	public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
		Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
		if (rotate) {
			if (!faceVector(directionVec)) return;
		}
		InventoryUtil.doSwap(slot);
		mc.player.swingHand(Hand.MAIN_HAND);
		BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
		mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
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

	public enum CalcMode {
		Meteor,
		Thunder
	}

	public static class PlayerAndPredict {
		PlayerEntity player;
		PlayerEntity predict;
		public PlayerAndPredict(PlayerEntity player) {
			this.player = player;
			if (INSTANCE.predictTicks.getValueFloat() > 0) {
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

	public enum Page {
		General,
		Calc,
		Rotate,
		Render,
	}
}