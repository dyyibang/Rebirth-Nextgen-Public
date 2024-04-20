package me.rebirthclient.mod.modules.render;

import com.google.common.collect.Maps;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.events.eventbus.EventHandler;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.CommandManager;
import me.rebirthclient.api.util.*;
import me.rebirthclient.asm.accessors.IEntity;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

public class LogoutSpots extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
    private final BooleanSetting box = add(new BooleanSetting("Box"));
    private final BooleanSetting outline = add(new BooleanSetting("Outline"));
    private final BooleanSetting text = add(new BooleanSetting("Text"));
    private final SliderSetting textScaled =
            add(new SliderSetting("TextScaled", 0, 2, 0.1, v -> text.getValue()));
    private final BooleanSetting rect = add(new BooleanSetting("Rect", v -> text.getValue()));
    private final BooleanSetting message = add(new BooleanSetting("Message"));

    private final Map<UUID, PlayerEntity> playerCache = Maps.newConcurrentMap();
    private final Map<UUID, PlayerEntity> logoutCache = Maps.newConcurrentMap();

    public LogoutSpots() {
        super("LogoutSpots", Category.Render);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerListS2CPacket packet) {
            if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (PlayerListS2CPacket.Entry addedPlayer : packet.getPlayerAdditionEntries()) {
                    for (UUID uuid : logoutCache.keySet()) {
                        if (!uuid.equals(addedPlayer.profile().getId())) continue;
                        PlayerEntity player = logoutCache.get(uuid);
                        if (message.getValue()) CommandManager.sendChatMessage("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7alogged back at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        logoutCache.remove(uuid);
                    }
                }
            }
            playerCache.clear();
        }

        if (event.getPacket() instanceof PlayerRemoveS2CPacket packet) {
            for (UUID uuid2 : packet.profileIds()) {
                for (UUID uuid : playerCache.keySet()) {
                    if (!uuid.equals(uuid2)) continue;
                    final PlayerEntity player = playerCache.get(uuid);
                    if (!logoutCache.containsKey(uuid)) {
                        if (message.getValue()) CommandManager.sendChatMessage("\u00a7e[!] \u00a7b" + player.getName().getString() + " \u00a7clogged out at X: " + (int) player.getX() + " Y: " + (int) player.getY() + " Z: " + (int) player.getZ());
                        logoutCache.put(uuid, player);
                    }
                }
            }
            playerCache.clear();
        }
    }

    @Override
    public void onEnable() {
        playerCache.clear();
        logoutCache.clear();
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || player.equals(mc.player)) continue;
            playerCache.put(player.getGameProfile().getId(), player);
        }
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity data = logoutCache.get(uuid);
            if (data == null) continue;
            Render3DUtil.draw3DBox(matrixStack, ((IEntity) data).getDimensions().getBoxAt(data.getPos()), color.getValue(), outline.getValue(), box.getValue());
        }
    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (!text.getValue()) return;
        for (UUID uuid : logoutCache.keySet()) {
            final PlayerEntity player = logoutCache.get(uuid);
            if (player == null) continue;
            drawText(drawContext, player.getName().getString() + " \u00a7a" + new DecimalFormat("0.0").format(EntityUtil.getHealth(player)) + " \u00a7clogged", player.getPos().add(0, player.getBoundingBox().getYLength() + 0.4, 0), Rebirth.FRIEND.isFriend(player));
        }
    }

    public void drawText(DrawContext context, String text, Vec3d vector, boolean friend) {
        Vec3d preVec = vector;
        vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
        if (vector.z > 0 && vector.z < 1) {
            double posX = vector.x;
            double posY = vector.y;
            double endPosX = Math.max(vector.x, vector.z);
            float scale = (float) Math.max(1 - MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(preVec)) * 0.01 * textScaled.getValue(), 0); //EntityUtil.getEyesPos().distanceTo(preVec)
            float diff = (float) (endPosX - posX) / 2;
            float textWidth = mc.textRenderer.getWidth(text) * scale;
            float tagX = (float) (posX + diff - textWidth / 2);
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, scale);
            double y = (posY - 11 + mc.textRenderer.fontHeight * 1.2) / scale;
            if (rect.getValue()) Render2DUtil.drawRect(context.getMatrices(), (int) (tagX / scale) - 2, (int) y - 3, mc.textRenderer.getWidth(text) + 4, 14, new Color(0x99000001, true));
            context.drawText(mc.textRenderer, text, (int) (tagX / scale), (int) y, friend ? new Color(0, 255, 0).getRGB() : -1, true);
            context.getMatrices().pop();
        }
    }
}