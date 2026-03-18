package com.hoorinekoneko.vncclientmc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.image.BufferedImage;

public class HologramRenderer {
    private boolean visible = true;
    private float scale = 1.0f;
    private float distance = 3.0f;
    private int quality = 64;
    private BufferedImage currentImage;
    private int lastQuality = 64;

    public void updateImage(BufferedImage image) {
        if (image == null) return;

        if (currentImage == null || lastQuality != quality) {
            currentImage = new BufferedImage(quality, quality, BufferedImage.TYPE_INT_RGB);
            lastQuality = quality;
        }
        
        java.awt.Graphics2D g2d = currentImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        float srcAspect = (float) image.getWidth() / image.getHeight();
        float dstAspect = 1.0f;
        
        int srcX, srcY, srcW, srcH;
        if (srcAspect > dstAspect) {
            srcH = image.getHeight();
            srcW = (int) (srcH * dstAspect);
            srcX = (image.getWidth() - srcW) / 2;
            srcY = 0;
        } else {
            srcW = image.getWidth();
            srcH = (int) (srcW / dstAspect);
            srcX = 0;
            srcY = (image.getHeight() - srcH) / 2;
        }
        
        g2d.drawImage(image, 0, 0, quality, quality, srcX, srcY, srcX + srcW, srcY + srcH, null);
        g2d.dispose();
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (!visible || currentImage == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Entity entity = client.player;
        Vec3d cameraPos = entity.getCameraPosVec(tickDelta);
        Vec3d lookVec = entity.getRotationVec(tickDelta);
        
        Vec3d hologramPos = cameraPos.add(lookVec.x * distance, lookVec.y * distance - 0.5, lookVec.z * distance);

        matrices.push();
        matrices.translate(hologramPos.x, hologramPos.y, hologramPos.z);
        
        matrices.multiply(client.player.getRotationClient());
        matrices.scale(-scale, -scale, scale);

        renderImage(matrices);

        matrices.pop();
    }

    private void renderImage(MatrixStack matrices) {
        if (currentImage == null) return;

        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int[] pixels = currentImage.getRGB(0, 0, width, height, null, 0, width);

        int blockSize = Math.max(1, width / 32);
        for (int by = 0; by < height; by += blockSize) {
            for (int bx = 0; bx < width; bx += blockSize) {
                int avgR = 0, avgG = 0, avgB = 0, count = 0;
                
                for (int py = by; py < Math.min(by + blockSize, height); py++) {
                    for (int px = bx; px < Math.min(bx + blockSize, width); px++) {
                        int pixel = pixels[py * width + px];
                        avgR += (pixel >> 16) & 0xFF;
                        avgG += (pixel >> 8) & 0xFF;
                        avgB += pixel & 0xFF;
                        count++;
                    }
                }
                
                if (count > 0) {
                    avgR /= count;
                    avgG /= count;
                    avgB /= count;

                    float x1 = (bx / (float) width) * 2 - 1;
                    float y1 = (by / (float) height) * 2 - 1;
                    float x2 = ((bx + blockSize) / (float) width) * 2 - 1;
                    float y2 = ((by + blockSize) / (float) height) * 2 - 1;

                    float r = avgR / 255f;
                    float g = avgG / 255f;
                    float b = avgB / 255f;

                    buffer.vertex(matrix, x1, y2, 0).color(r, g, b, 0.9f);
                    buffer.vertex(matrix, x2, y2, 0).color(r, g, b, 0.9f);
                    buffer.vertex(matrix, x2, y1, 0).color(r, g, b, 0.9f);
                    buffer.vertex(matrix, x1, y1, 0).color(r, g, b, 0.9f);
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggleVisible() {
        this.visible = !this.visible;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, Math.min(10.0f, scale));
    }

    public float getScale() {
        return scale;
    }

    public void setDistance(float distance) {
        this.distance = Math.max(1.0f, Math.min(20.0f, distance));
    }

    public float getDistance() {
        return distance;
    }

    public void setQuality(int quality) {
        this.quality = Math.max(32, Math.min(256, quality));
    }

    public int getQuality() {
        return quality;
    }
}
