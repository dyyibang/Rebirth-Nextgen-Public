package me.rebirthclient.mod.modules.render;

import me.rebirthclient.Rebirth;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Render2DUtil;
import me.rebirthclient.api.util.TextUtil;
import me.rebirthclient.mod.gui.font.FontRenderers;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.settings.impl.BooleanSetting;
import me.rebirthclient.mod.settings.impl.ColorSetting;
import me.rebirthclient.mod.settings.impl.EnumSetting;
import me.rebirthclient.mod.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class NameTags extends Module {
    public static NameTags INSTANCE;
    private final SliderSetting scale = add(new SliderSetting("Scale", 0.1f, 2f, 0.01));
    private final SliderSetting minScale = add(new SliderSetting("MinScale", 0.1f, 1f, 0.01));
    private final SliderSetting scaled = add(new SliderSetting("Scaled", 0, 2, 0.01));
    private final SliderSetting offset = add(new SliderSetting("Offset", 0.001f, 1f, 0.001));
    private final SliderSetting height = add(new SliderSetting("Height", -3, 3, 0.01f));
    private final BooleanSetting gamemode = add(new BooleanSetting("Gamemode"));
    private final BooleanSetting ping = add(new BooleanSetting("Ping"));
    private final BooleanSetting health = add(new BooleanSetting("Health"));
    private final BooleanSetting distance = add(new BooleanSetting("Distance"));
    private final BooleanSetting pops = add(new BooleanSetting("TotemPops"));
    private final BooleanSetting enchants = add(new BooleanSetting("Enchants"));
    private final ColorSetting outline = add(new ColorSetting("Outline", new Color(0x99FFFFFF, true)).injectBoolean(true));
    private final ColorSetting rect = add(new ColorSetting("Rect", new Color(0x99000001, true)).injectBoolean(true));
    private final ColorSetting friendColor = add(new ColorSetting("FriendColor", new Color(0xFF1DFF1D, true)));
    private final ColorSetting color = add(new ColorSetting("Color", new Color(0xFFFFFFFF, true)));

    public final EnumSetting font = add(new EnumSetting("FontMode", Font.Fast));
    private final SliderSetting armorHeight = add(new SliderSetting("ArmorHeight", -10, 10f));
    private final SliderSetting armorScale = add(new SliderSetting("ArmorScale", 0.1f, 2f, 0.01f));
    private final EnumSetting armorMode = add(new EnumSetting("ArmorMode", Armor.Full));

    public NameTags() {
        super("NameTags", Category.Render);
        INSTANCE = this;
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        for (PlayerEntity ent : mc.world.getPlayers()) {
            if (ent == mc.player && mc.options.getPerspective().isFirstPerson()) continue;
            double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta();
            double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta();
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta();
            Vec3d vector = new Vec3d(x, y + height.getValue() + ent.getBoundingBox().getYLength() + 0.3, z);
            Vec3d preVec = vector;
            vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (vector.z > 0 && vector.z < 1) {
                Vector4d position = new Vector4d(vector.x, vector.y, vector.z, 0);
                position.x = Math.min(vector.x, position.x);
                position.y = Math.min(vector.y, position.y);
                position.z = Math.max(vector.x, position.z);

                String final_string = "";

                if (ping.getValue()) {
                    final_string += getEntityPing(ent) + "ms ";
                }
                if (gamemode.getValue()) {
                    final_string += translateGamemode(getEntityGamemode(ent)) + " ";
                }
                final_string += Formatting.RESET + ent.getName().getString();
                if (health.getValue()) {
                    final_string += " " + getHealthColor(ent) + round2(ent.getAbsorptionAmount() + ent.getHealth());
                }
                if (distance.getValue()) {
                    final_string += " " + Formatting.RESET + String.format("%.1f", mc.player.distanceTo(ent)) + "m";
                }
                if (pops.getValue() && Rebirth.POP.getPop(ent.getName().getString()) != 0) {
                    final_string += " \u00a7bPop" + " " + Formatting.LIGHT_PURPLE + Rebirth.POP.getPop(ent.getName().getString());
                }

                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;

                float diff = (float) (endPosX - posX) / 2;
                float textWidth;

                if (font.getValue() == Font.Fancy) {
                    textWidth = (FontRenderers.Arial.getWidth(final_string) * 1);
                } else {
                    textWidth = mc.textRenderer.getWidth(final_string);
                }

                float tagX = (float) ((posX + diff - textWidth / 2) * 1);

                ArrayList<ItemStack> stacks = new ArrayList<>();

                stacks.add(ent.getMainHandStack());
                stacks.add(ent.getInventory().armor.get(3));
                stacks.add(ent.getInventory().armor.get(2));
                stacks.add(ent.getInventory().armor.get(1));
                stacks.add(ent.getInventory().armor.get(0));
                stacks.add(ent.getOffHandStack());

                context.getMatrices().push();
                context.getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                float size = (float) Math.max(1 - MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(preVec)) * 0.01 * scaled.getValue(), 0);
                context.getMatrices().scale(Math.max(scale.getValueFloat() * size, minScale.getValueFloat()), Math.max(scale.getValueFloat() * size, minScale.getValueFloat()), 1f);
                context.getMatrices().translate(0, offset.getValueFloat() * MathHelper.sqrt((float) EntityUtil.getEyesPos().squaredDistanceTo(preVec)), 0);
                context.getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);

                float item_offset = 0;
                if (armorMode.getValue() != Armor.None) {
                    for (ItemStack armorComponent : stacks) {
                        if (!armorComponent.isEmpty()) {
                            context.getMatrices().push();
                            context.getMatrices().translate(tagX - 2 + (textWidth + 4) / 2f, (float) (posY - 13f) + 6.5f, 0);
                            context.getMatrices().scale(armorScale.getValueFloat(), armorScale.getValueFloat(), 1f);
                            context.getMatrices().translate(-(tagX - 2 + (textWidth + 4) / 2f), -(float) ((posY - 13f) + 6.5f), 0);
                            context.getMatrices().translate(posX - 52.5 + item_offset, (float) (posY - 29f) + armorHeight.getValueFloat(), 0);
                            float durability = armorComponent.getMaxDamage() - armorComponent.getDamage();
                            int percent = (int) ((durability / (float) armorComponent.getMaxDamage()) * 100F);
                            Color color;
                            if (percent <= 33) {
                                color = Color.RED;
                            } else if (percent <= 66) {
                                color = Color.YELLOW;
                            } else {
                                color = Color.GREEN;
                            }
                            switch ((Armor) armorMode.getValue()) {
                                case Armor -> {
                                    DiffuseLighting.disableGuiDepthLighting();
                                    context.drawItem(armorComponent, 0, 0);
                                    context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                                }
                                case Full -> {
                                    DiffuseLighting.disableGuiDepthLighting();
                                    context.drawItem(armorComponent, 0, 0);
                                    context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                                    if (armorComponent.getMaxDamage() > 0) {
                                        if (font.getValue() == Font.Fancy) {
                                            FontRenderers.Arial.drawString(context.getMatrices(), String.valueOf(percent), 9 - FontRenderers.Arial.getWidth(String.valueOf(percent)) / 2, -FontRenderers.Arial.getFontHeight() + 3, color.getRGB());
                                        } else {
                                            context.drawText(mc.textRenderer, String.valueOf(percent), 9 - mc.textRenderer.getWidth(String.valueOf(percent)) / 2, -mc.textRenderer.fontHeight + 1, color.getRGB(), true);
                                        }
                                    }
                                }
                                case Durability -> {
                                    context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                                    if (armorComponent.getMaxDamage() > 0) {
                                        if (!armorComponent.isItemBarVisible()) {
                                            int i = armorComponent.getItemBarStep();
                                            int j = armorComponent.getItemBarColor();
                                            int k = 2;
                                            int l = 13;
                                            context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, -16777216);
                                            context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | -16777216);
                                        }
                                        if (font.getValue() == Font.Fancy) {
                                            FontRenderers.Arial.drawString(context.getMatrices(), String.valueOf(percent), 9 - FontRenderers.Arial.getWidth(String.valueOf(percent)) / 2, 7, color.getRGB());
                                        } else {
                                            context.drawText(mc.textRenderer, String.valueOf(percent), 9 - mc.textRenderer.getWidth(String.valueOf(percent)) / 2, 5, color.getRGB(), true);
                                        }
                                    }
                                }
                            }
                            context.getMatrices().pop();

                            if (this.enchants.getValue()) {
                                float enchantmentY = 0;
                                NbtList enchants = armorComponent.getEnchantments();
                                for (int index = 0; index < enchants.size(); ++index) {
                                    String id = enchants.getCompound(index).getString("id");
                                    short level = enchants.getCompound(index).getShort("lvl");
                                    String encName;
                                    switch (id) {
                                        case "minecraft:blast_protection" -> encName = "B" + level;
                                        case "minecraft:protection" -> encName = "P" + level;
                                        case "minecraft:thorns" -> encName = "T" + level;
                                        case "minecraft:sharpness" -> encName = "S" + level;
                                        case "minecraft:efficiency" -> encName = "E" + level;
                                        case "minecraft:unbreaking" -> encName = "U" + level;
                                        case "minecraft:power" -> encName = "PO" + level;
                                        default -> {
                                            continue;
                                        }
                                    }

                                    if (font.getValue() == Font.Fancy) {
                                        FontRenderers.Arial.drawString(context.getMatrices(), encName, posX - 50 + item_offset, (float) posY - 45 + enchantmentY, -1);
                                    } else {
                                        context.getMatrices().push();
                                        context.getMatrices().translate((posX - 50f + item_offset), (posY - 45f + enchantmentY), 0);
                                        context.drawText(mc.textRenderer, encName, 0, 0, -1, true);
                                        context.getMatrices().pop();
                                    }
                                    enchantmentY -= 8;
                                }
                            }
                        }
                        item_offset += 18f;
                    }
                }
                if (rect.booleanValue) {
                    Render2DUtil.drawRect(context.getMatrices(), tagX - 2, (float) (posY - 13f), textWidth + 4, 11, rect.getValue());
                }
                if (outline.booleanValue) {
                    Render2DUtil.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), textWidth + 6, 1, outline.getValue());
                    Render2DUtil.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 2f), textWidth + 6, 1, outline.getValue());
                    Render2DUtil.drawRect(context.getMatrices(), tagX - 3, (float) (posY - 14f), 1, 12, outline.getValue());
                    Render2DUtil.drawRect(context.getMatrices(), tagX + textWidth + 2, (float) (posY - 14f), 1, 12, outline.getValue());
                }
                if (font.getValue() == Font.Fancy) {
                    FontRenderers.Arial.drawString(context.getMatrices(), final_string, tagX, (float) posY - 10, Rebirth.FRIEND.isFriend(ent) ? friendColor.getValue().getRGB() : this.color.getValue().getRGB());
                } else {
                    context.getMatrices().push();
                    context.getMatrices().translate(tagX, ((float) posY - 11), 0);
                    context.drawText(mc.textRenderer, final_string, 0, 0, Rebirth.FRIEND.isFriend(ent) ? friendColor.getValue().getRGB() : this.color.getValue().getRGB(), true);
                    context.getMatrices().pop();
                }
                context.getMatrices().pop();
            }
        }
    }

    public static String getEntityPing(PlayerEntity entity) {
        if (mc.getNetworkHandler() == null) return "-1";
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return "-1";
        int ping = playerListEntry.getLatency();
        Formatting color = Formatting.GREEN;
        if (ping >= 100) {
            color = Formatting.YELLOW;
        }
        if (ping >= 250) {
            color = Formatting.RED;
        }
        return color.toString() + ping;
    }

    public static GameMode getEntityGamemode(PlayerEntity entity) {
        if (entity == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return playerListEntry == null ? null : playerListEntry.getGameMode();
    }

    private String translateGamemode(GameMode gamemode) {
        if (gamemode == null) return "\u00a77[BOT]";
        return switch (gamemode) {
            case SURVIVAL -> "\u00a7b[S]";
            case CREATIVE -> "\u00a7c[C]";
            case SPECTATOR -> "\u00a77[SP]";
            case ADVENTURE -> "\u00a7e[A]";
        };
    }

    private Formatting getHealthColor(@NotNull PlayerEntity entity) {
        int health = (int) ((int) entity.getHealth() + entity.getAbsorptionAmount());
        if (health <= 15 && health > 7) return Formatting.YELLOW;
        if (health > 15) return Formatting.GREEN;
        return Formatting.RED;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public enum Font {
        Fancy, Fast
    }

    public enum Armor {
        None, Full, Durability, Armor
    }
}