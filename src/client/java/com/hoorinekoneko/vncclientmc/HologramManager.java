package com.hoorinekoneko.vncclientmc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.image.BufferedImage;

public class HologramManager {
    private ArmorStandEntity hologramEntity;
    private BlockPos hologramPos;
    private boolean active = false;
    private boolean visible = true;
    private float scale = 2.0f;
    private int quality = 128;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100;

    public void placeHologram() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        if (hologramEntity != null) {
            removeHologram();
        }

        Vec3d pos = client.player.getPos();
        BlockPos targetPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);

        ArmorStandEntity armorStand = new ArmorStandEntity(
                net.minecraft.entity.EntityType.ARMOR_STAND,
                client.world
        );

        armorStand.setPosition(targetPos.getX() + 0.5, targetPos.getY() + 1.5, targetPos.getZ() + 0.5);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setCustomNameVisible(false);
        armorStand.setArms(false);
        armorStand.setSmall(true);

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("NoBasePlate", true);
        nbt.putBoolean("ShowArms", false);
        nbt.putBoolean("Small", true);
        nbt.putInt("DisabledSlots", 2039583);
        armorStand.readNbt(nbt);

        client.world.addEntity(armorStand.getId(), armorStand);

        this.hologramEntity = armorStand;
        this.hologramPos = targetPos;
        this.active = true;

        VNCClientMod.LOGGER.info("Hologram placed at {}", targetPos);
        sendMessage("Hologram placed at your feet");
    }

    public void removeHologram() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        if (hologramEntity != null) {
            client.world.removeEntity(hologramEntity.getId(), net.minecraft.entity.Entity.RemovalReason.DISCARDED);
            hologramEntity = null;
        }

        hologramPos = null;
        active = false;

        VNCClientMod.LOGGER.info("Hologram removed");
        sendMessage("Hologram removed");
    }

    public void toggleHologram() {
        if (active) {
            visible = !visible;
            if (hologramEntity != null) {
                hologramEntity.setInvisible(!visible);
            }
        } else {
            placeHologram();
        }
    }

    public void update() {
        if (!active || hologramEntity == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        if (VNCClientMod.vncManager != null && VNCClientMod.vncManager.isConnected()) {
            VNCClientMod.vncManager.updateFrame();
        }
    }

    public void renderHologram(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (!active || hologramEntity == null || !visible) {
            return;
        }

        if (VNCClientMod.vncManager == null || !VNCClientMod.vncManager.isConnected()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) {
            return;
        }

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        Vec3d hologramWorldPos = hologramEntity.getPos();

        double renderX = hologramWorldPos.x - cameraPos.x;
        double renderY = hologramWorldPos.y - cameraPos.y + 1.8;
        double renderZ = hologramWorldPos.z - cameraPos.z;

        matrices.push();
        matrices.translate(renderX, renderY, renderZ);

        matrices.scale(scale * 0.5f, scale * 0.5f, scale * 0.5f);

        renderScreen(matrices, vertexConsumers);

        matrices.pop();
    }

    private void renderScreen(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        BufferedImage frame = VNCClientMod.vncManager != null ? VNCClientMod.vncManager.getCurrentFrame() : null;
        if (frame == null) {
            return;
        }

        int size = quality;
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        float srcAspect = (float) frame.getWidth() / frame.getHeight();
        int srcX, srcY, srcW, srcH;
        if (srcAspect > 1) {
            srcH = frame.getHeight();
            srcW = (int) (srcH);
            srcX = (frame.getWidth() - srcW) / 2;
            srcY = 0;
        } else {
            srcW = frame.getWidth();
            srcH = (int) (srcW);
            srcX = 0;
            srcY = (frame.getHeight() - srcH) / 2;
        }

        g2d.drawImage(frame, 0, 0, size, size, srcX, srcY, srcX + srcW, srcY + srcH, null);
        g2d.dispose();

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int[] pixels = scaled.getRGB(0, 0, size, size, null, 0, size);

        int blockSize = Math.max(2, size / 32);
        for (int by = 0; by < size; by += blockSize) {
            for (int bx = 0; bx < size; bx += blockSize) {
                int avgR = 0, avgG = 0, avgB = 0, avgA = 0, count = 0;

                for (int py = by; py < Math.min(by + blockSize, size); py++) {
                    for (int px = bx; px < Math.min(bx + blockSize, size); px++) {
                        int pixel = pixels[py * size + px];
                        avgR += (pixel >> 16) & 0xFF;
                        avgG += (pixel >> 8) & 0xFF;
                        avgB += pixel & 0xFF;
                        avgA += (pixel >> 24) & 0xFF;
                        count++;
                    }
                }

                if (count > 0) {
                    avgR /= count;
                    avgG /= count;
                    avgB /= count;
                    avgA /= count;

                    if (avgA > 10) {
                        float x1 = (bx / (float) size) * 2 - 1;
                        float y1 = (by / (float) size) * 2 - 1;
                        float x2 = ((bx + blockSize) / (float) size) * 2 - 1;
                        float y2 = ((by + blockSize) / (float) size) * 2 - 1;

                        float r = avgR / 255f;
                        float g = avgG / 255f;
                        float b = avgB / 255f;
                        float a = (avgA / 255f) * 0.9f;

                        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a);
                        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);
                        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a);
                        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
                    }
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public boolean isHologramActive() {
        return active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.5f, Math.min(10.0f, scale));
        sendMessage("Hologram scale: " + this.scale);
    }

    public float getScale() {
        return scale;
    }

    public void setQuality(int quality) {
        this.quality = Math.max(32, Math.min(256, quality));
        sendMessage("Hologram quality: " + this.quality);
    }

    public int getQuality() {
        return quality;
    }

    public BlockPos getHologramPos() {
        return hologramPos;
    }

    public void printStatus() {
        if (active && hologramPos != null) {
            sendMessage("Hologram at " + hologramPos.getX() + ", " + hologramPos.getY() + ", " + hologramPos.getZ());
        } else {
            sendMessage("Hologram not placed. Use /vnc holo place");
        }
    }

    private void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
        }
    }
}
