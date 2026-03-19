package com.hoorinekoneko.vncclientmc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HologramRenderer {
    private List<BufferedImage> frameHistory = new ArrayList<>();
    private BufferedImage currentFrame;
    private int quality = 128;
    private float scale = 2.0f;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100;

    public void updateImage(BufferedImage image) {
        if (image == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        this.currentFrame = image;
    }

    public void render(MatrixStack matrices, Vec3d cameraPos, float tickDelta) {
        if (currentFrame == null) return;

        int size = quality;
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        float srcAspect = (float) currentFrame.getWidth() / currentFrame.getHeight();
        int srcX, srcY, srcW, srcH;
        if (srcAspect > 1) {
            srcH = currentFrame.getHeight();
            srcW = (int) (srcH);
            srcX = (currentFrame.getWidth() - srcW) / 2;
            srcY = 0;
        } else {
            srcW = currentFrame.getWidth();
            srcH = (int) (srcW);
            srcX = 0;
            srcY = (currentFrame.getHeight() - srcH) / 2;
        }

        g2d.drawImage(currentFrame, 0, 0, size, size, srcX, srcY, srcX + srcW, srcY + srcH, null);
        g2d.dispose();

        this.currentFrame = scaled;
    }

    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void setQuality(int quality) {
        this.quality = Math.max(32, Math.min(256, quality));
    }

    public int getQuality() {
        return quality;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, Math.min(10.0f, scale));
    }

    public float getScale() {
        return scale;
    }
}
