package com.hoorinekoneko.vncclientmc;

import com.shinyhut.vernacular.VernacularClient;
import com.shinyhut.vernacular.VernacularConfiguration;
import com.shinyhut.vernacular.authentication.Authenticator;
import com.shinyhut.vernacular.authentication.PasswordAuthenticator;
import com.shinyhut.vernacular.events.FramebufferUpdateEvent;
import com.shinyhut.vernacular.events.FramebufferUpdateListener;
import com.shinyhut.vernacular.events.VncEventListener;
import net.minecraft.client.MinecraftClient;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VNCManager {
    private VernacularClient client;
    private ExecutorService executor;
    private volatile BufferedImage currentFrame;
    private volatile boolean connected = false;
    private volatile String currentHost = "";
    private volatile int currentPort = 0;
    private int updateCounter = 0;
    private static final int UPDATE_INTERVAL = 2;

    public VNCManager() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void connect(String host, int port) {
        if (connected) {
            disconnect();
        }

        this.currentHost = host;
        this.currentPort = port;

        executor.execute(() -> {
            try {
                VNCClientMod.LOGGER.info("Connecting to VNC server {}:{}", host, port);

                VernacularConfiguration config = VernacularConfiguration.builder()
                        .host(host)
                        .port(port)
                        .autoReconnect(true)
                        .build();

                client = new VernacularClient(config);
                client.addEventListener(new FramebufferUpdateListener() {
                    @Override
                    public void onFramebufferUpdate(FramebufferUpdateEvent event) {
                        currentFrame = event.getFramebuffer().getImage();
                        updateCounter = 0;
                    }
                });

                client.addEventListener(new VncEventListener() {
                    @Override
                    public void onConnected() {
                        connected = true;
                        VNCClientMod.LOGGER.info("Connected to VNC server {}:{}", host, port);
                    }

                    @Override
                    public void onDisconnected() {
                        connected = false;
                        VNCClientMod.LOGGER.info("Disconnected from VNC server");
                    }
                });

                client.connect();
            } catch (Exception e) {
                VNCClientMod.LOGGER.error("Failed to connect to VNC server: {}", e.getMessage());
                connected = false;
            }
        });
    }

    public void disconnect() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (IOException e) {
                VNCClientMod.LOGGER.error("Error disconnecting: {}", e.getMessage());
            }
            client = null;
        }
        connected = false;
        currentFrame = null;
    }

    public void updateFrame() {
        if (!connected || currentFrame == null) {
            return;
        }

        updateCounter++;
        if (updateCounter >= UPDATE_INTERVAL && VNCClientMod.hologramRenderer != null) {
            VNCClientMod.hologramRenderer.updateImage(currentFrame);
            updateCounter = 0;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void printStatus() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (connected) {
                client.player.sendMessage(net.minecraft.text.Text.literal("VNC Status: Connected to " + currentHost + ":" + currentPort), false);
            } else {
                client.player.sendMessage(net.minecraft.text.Text.literal("VNC Status: Not connected"), false);
            }
        }
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public int getCurrentPort() {
        return currentPort;
    }
}
