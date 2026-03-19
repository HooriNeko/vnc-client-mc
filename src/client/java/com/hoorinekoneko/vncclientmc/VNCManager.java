package com.hoorinekoneko.vncclientmc;

import com.shinyhut.vernacular.VernacularClient;
import com.shinyhut.vernacular.VernacularConfiguration;
import com.shinyhut.vernacular.input.KeyboardEvent;
import com.shinyhut.vernacular.input.MouseButton;
import com.shinyhut.vernacular.input.MouseEvent;
import com.shinyhut.vernacular.render.RemoteDesktop;
import com.shinyhut.vernacular.events.FramebufferUpdateEvent;
import com.shinyhut.vernacular.events.FramebufferUpdateListener;
import com.shinyhut.vernacular.events.VncEventListener;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.input.Keyboard;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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
    private final Set<Integer> pressedKeys = new HashSet<>();

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
        pressedKeys.clear();
    }

    public void updateFrame() {
        if (!connected || currentFrame == null) {
            return;
        }

        updateCounter++;
        if (updateCounter >= UPDATE_INTERVAL) {
            updateCounter = 0;
        }

        handleKeyboardInput();
    }

    private void handleKeyboardInput() {
        if (remoteDesktop == null || !connected) {
            return;
        }

        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();
            boolean state = Keyboard.getEventKeyState();
            
            if (key == 0) continue;

            int keyCode = translateKey(key);
            if (keyCode != 0) {
                if (state) {
                    if (!pressedKeys.contains(keyCode)) {
                        pressedKeys.add(keyCode);
                        remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                                .keyCode(keyCode)
                                .pressed(true)
                                .build());
                    }
                } else {
                    if (pressedKeys.contains(keyCode)) {
                        pressedKeys.remove(keyCode);
                        remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                                .keyCode(keyCode)
                                .pressed(false)
                                .build());
                    }
                }
            }

            char c = Keyboard.getEventCharacter();
            if (state && c != '\0' && Character.isDefined(c)) {
                remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                        .character(c)
                        .pressed(true)
                        .build());
                remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                        .character(c)
                        .pressed(false)
                        .build());
            }
        }
    }

    private int translateKey(int mcKey) {
        return switch (mcKey) {
            case Keyboard.KEY_ESCAPE -> 1;
            case Keyboard.KEY_1 -> 2;
            case Keyboard.KEY_2 -> 3;
            case Keyboard.KEY_3 -> 4;
            case Keyboard.KEY_4 -> 5;
            case Keyboard.KEY_5 -> 6;
            case Keyboard.KEY_6 -> 7;
            case Keyboard.KEY_7 -> 8;
            case Keyboard.KEY_8 -> 9;
            case Keyboard.KEY_9 -> 10;
            case Keyboard.KEY_0 -> 11;
            case Keyboard.KEY_MINUS -> 12;
            case Keyboard.KEY_EQUALS -> 13;
            case Keyboard.KEY_BACK -> 14;
            case Keyboard.KEY_TAB -> 15;
            case Keyboard.KEY_Q -> 16;
            case Keyboard.KEY_W -> 17;
            case Keyboard.KEY_E -> 18;
            case Keyboard.KEY_R -> 19;
            case Keyboard.KEY_T -> 20;
            case Keyboard.KEY_Y -> 21;
            case Keyboard.KEY_U -> 22;
            case Keyboard.KEY_I -> 23;
            case Keyboard.KEY_O -> 24;
            case Keyboard.KEY_P -> 25;
            case Keyboard.KEY_LBRACKET -> 26;
            case Keyboard.KEY_RBRACKET -> 27;
            case Keyboard.KEY_RETURN -> 28;
            case Keyboard.KEY_LCONTROL -> 29;
            case Keyboard.KEY_A -> 30;
            case Keyboard.KEY_S -> 31;
            case Keyboard.KEY_D -> 32;
            case Keyboard.KEY_F -> 33;
            case Keyboard.KEY_G -> 34;
            case Keyboard.KEY_H -> 35;
            case Keyboard.KEY_J -> 36;
            case Keyboard.KEY_K -> 37;
            case Keyboard.KEY_L -> 38;
            case Keyboard.KEY_SEMICOLON -> 39;
            case Keyboard.KEY_APOSTROPHE -> 40;
            case Keyboard.KEY_GRAVE -> 41;
            case Keyboard.KEY_LSHIFT -> 42;
            case Keyboard.KEY_BACKSLASH -> 43;
            case Keyboard.KEY_Z -> 44;
            case Keyboard.KEY_X -> 45;
            case Keyboard.KEY_C -> 46;
            case Keyboard.KEY_V -> 47;
            case Keyboard.KEY_B -> 48;
            case Keyboard.KEY_N -> 49;
            case Keyboard.KEY_M -> 50;
            case Keyboard.KEY_COMMA -> 51;
            case Keyboard.KEY_PERIOD -> 52;
            case Keyboard.KEY_SLASH -> 53;
            case Keyboard.KEY_RSHIFT -> 54;
            case Keyboard.KEY_MULTIPLY -> 55;
            case Keyboard.KEY_LMENU -> 56;
            case Keyboard.KEY_SPACE -> 57;
            case Keyboard.KEY_CAPITAL -> 58;
            case Keyboard.KEY_F1 -> 59;
            case Keyboard.KEY_F2 -> 60;
            case Keyboard.KEY_F3 -> 61;
            case Keyboard.KEY_F4 -> 62;
            case Keyboard.KEY_F5 -> 63;
            case Keyboard.KEY_F6 -> 64;
            case Keyboard.KEY_F7 -> 65;
            case Keyboard.KEY_F8 -> 66;
            case Keyboard.KEY_F9 -> 67;
            case Keyboard.KEY_F10 -> 68;
            case Keyboard.KEY_NUMLOCK -> 69;
            case Keyboard.KEY_SCROLL -> 70;
            case Keyboard.KEY_NUMPAD7 -> 71;
            case Keyboard.KEY_NUMPAD8 -> 72;
            case Keyboard.KEY_NUMPAD9 -> 73;
            case Keyboard.KEY_SUBTRACT -> 74;
            case Keyboard.KEY_NUMPAD4 -> 75;
            case Keyboard.KEY_NUMPAD5 -> 76;
            case Keyboard.KEY_NUMPAD6 -> 77;
            case Keyboard.KEY_ADD -> 78;
            case Keyboard.KEY_NUMPAD1 -> 79;
            case Keyboard.KEY_NUMPAD2 -> 80;
            case Keyboard.KEY_NUMPAD3 -> 81;
            case Keyboard.KEY_NUMPAD0 -> 82;
            case Keyboard.KEY_DECIMAL -> 83;
            case Keyboard.KEY_F11 -> 87;
            case Keyboard.KEY_F12 -> 88;
            case Keyboard.KEY_F13 -> 100;
            case Keyboard.KEY_F14 -> 101;
            case Keyboard.KEY_F15 -> 102;
            case Keyboard.KEY_KANA -> 112;
            case Keyboard.KEY_F16 -> 113;
            case Keyboard.KEY_F17 -> 114;
            case Keyboard.KEY_F18 -> 115;
            case Keyboard.KEY_F19 -> 116;
            case Keyboard.KEY_F20 -> 117;
            case Keyboard.KEY_F21 -> 118;
            case Keyboard.KEY_F22 -> 119;
            case Keyboard.KEY_F23 -> 120;
            case Keyboard.KEY_F24 -> 121;
            case Keyboard.KEY_LWIN -> 124;
            case Keyboard.KEY_RWIN -> 125;
            case Keyboard.KEY_DELETE -> 127;
            case Keyboard.KEY_HOME -> 1027;
            case Keyboard.KEY_END -> 1023;
            case Keyboard.KEY_PGUP -> 1029;
            case Keyboard.KEY_PGDN -> 1028;
            case Keyboard.KEY_UP -> 1026;
            case Keyboard.KEY_LEFT -> 1025;
            case Keyboard.KEY_DOWN -> 1028;
            case Keyboard.KEY_RIGHT -> 1027;
            default -> 0;
        };
    }

    public void sendMouseMove(int x, int y) {
        if (remoteDesktop != null && connected) {
            remoteDesktop.sendMouseEvent(MouseEvent.move(x, y));
        }
    }

    public void sendMouseClick(int x, int y, int button, boolean pressed) {
        if (remoteDesktop != null && connected) {
            MouseButton btn = switch (button) {
                case 0 -> MouseButton.LEFT;
                case 1 -> MouseButton.MIDDLE;
                case 2 -> MouseButton.RIGHT;
                default -> null;
            };
            if (btn != null) {
                if (pressed) {
                    remoteDesktop.sendMouseEvent(MouseEvent.click(x, y, btn));
                } else {
                    remoteDesktop.sendMouseEvent(MouseEvent.release(x, y, btn));
                }
            }
        }
    }

    public void sendMouseWheel(int clicks) {
        if (remoteDesktop != null && connected && clicks != 0) {
            remoteDesktop.sendMouseEvent(MouseEvent.wheel(clicks));
        }
    }

    public void releaseAllKeys() {
        if (remoteDesktop != null) {
            for (int keyCode : pressedKeys) {
                remoteDesktop.sendKeyboardEvent(KeyboardEvent.builder()
                        .keyCode(keyCode)
                        .pressed(false)
                        .build());
            }
            pressedKeys.clear();
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
