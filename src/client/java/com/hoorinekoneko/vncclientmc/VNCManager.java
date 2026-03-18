package com.hoorinekoneko.vncclientmc;

import com.shinyhut.vernacular.VernacularClient;
import com.shinyhut.vernacular.VernacularConfiguration;
import com.shinyhut.vernacular.input.KeyboardEvent;
import com.shinyhut.vernacular.input.MouseButton;
import com.shinyhut.vernacular.input.MouseEvent;
import com.shinyhut.vernacular.render.Framebuffer;
import com.shinyhut.vernacular.render.RemoteDesktop;
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
    private RemoteDesktop remoteDesktop;

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
                        remoteDesktop = client.getRemoteDesktop();
                        VNCClientMod.LOGGER.info("Connected to VNC server {}:{}", host, port);
                        sendMessage("VNC: Connected to " + host + ":" + port);
                    }

                    @Override
                    public void onDisconnected() {
                        connected = false;
                        remoteDesktop = null;
                        VNCClientMod.LOGGER.info("Disconnected from VNC server");
                        sendMessage("VNC: Disconnected");
                    }
                });

                client.connect();
            } catch (Exception e) {
                VNCClientMod.LOGGER.error("Failed to connect to VNC server: {}", e.getMessage());
                sendMessage("VNC: Connection failed - " + e.getMessage());
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
        remoteDesktop = null;
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

    public void sendMouseMove(int x, int y) {
        if (remoteDesktop != null && connected) {
            remoteDesktop.sendMouseEvent(MouseEvent.move(x, y));
        }
    }

    public void sendMouseClick(int x, int y, int button) {
        if (remoteDesktop != null && connected) {
            MouseButton btn = switch (button) {
                case 0 -> MouseButton.LEFT;
                case 1 -> MouseButton.MIDDLE;
                case 2 -> MouseButton.RIGHT;
                default -> null;
            };
            if (btn != null) {
                remoteDesktop.sendMouseEvent(MouseEvent.click(x, y, btn));
            }
        }
    }

    public void sendKeyPress(int keyCode, boolean pressed) {
        if (remoteDesktop != null && connected) {
            remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                    .keyCode(keyCode)
                    .pressed(pressed)
                    .build());
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
                client.player.sendMessage(net.minecraft.text.Text.literal("VNC: Connected to " + currentHost + ":" + currentPort), false);
            } else {
                client.player.sendMessage(net.minecraft.text.Text.literal("VNC: Not connected"), false);
            }
        }
    }

    private void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
        }
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public int getCurrentPort() {
        return currentPort;
    }
}
