package com.hoorinekoneko.vncclientmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VNCClientMod implements ClientModInitializer {
    public static final String MOD_ID = "vnc-client-mc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static VNCManager vncManager;
    public static HologramManager hologramManager;

    private static final String DEFAULT_VNC_HOST = "localhost";
    private static final int DEFAULT_VNC_PORT = 5900;

    private KeyBinding toggleHologramKey;
    private KeyBinding controlModeKey;
    private boolean wasMouseGrabbed = false;
    private boolean controlModeActive = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("VNC Client For MC initializing (client)...");

        vncManager = new VNCManager();
        hologramManager = new HologramManager();

        toggleHologramKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vnc-client-mc.toggle_hologram",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.vnc-client-mc"
        ));

        controlModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vnc-client-mc.control_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.vnc-client-mc"
        ));

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        WorldRenderEvents.AFTER_ENTITIES.register(this::onAfterEntities);

        LOGGER.info("VNC Client For MC initialized!");
    }

    private void registerCommands(CommandManager dispatcher, com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.ClientCommandSource> registryAccess) {
        dispatcher.register(
            CommandManager.literal("vnc")
                .then(CommandManager.literal("connect")
                    .then(CommandManager.argument("host", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.word())
                        .executes(ctx -> {
                            String host = ctx.getArgument("host", String.class);
                            vncManager.connect(host, DEFAULT_VNC_PORT);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("disconnect")
                    .executes(ctx -> {
                        vncManager.disconnect();
                        return 1;
                    })
                )
                .then(CommandManager.literal("holo")
                    .then(CommandManager.literal("place")
                        .executes(ctx -> {
                            hologramManager.placeHologram();
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("remove")
                        .executes(ctx -> {
                            hologramManager.removeHologram();
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("scale")
                        .then(CommandManager.argument("value", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.floatArg())
                            .executes(ctx -> {
                                float scale = ctx.getArgument("value", Float.class);
                                hologramManager.setScale(scale);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("quality")
                        .then(CommandManager.argument("level", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.integer())
                            .executes(ctx -> {
                                int quality = ctx.getArgument("level", Integer.class);
                                hologramManager.setQuality(quality);
                                return 1;
                            })
                        )
                    )
                )
                .then(CommandManager.literal("control")
                    .executes(ctx -> {
                        toggleControlMode();
                        return 1;
                    })
                )
                .then(CommandManager.literal("status")
                    .executes(ctx -> {
                        vncManager.printStatus();
                        hologramManager.printStatus();
                        return 1;
                    })
                )
        );
    }

    private void onClientTick(MinecraftClient client) {
        if (toggleHologramKey.wasPressed()) {
            hologramManager.toggleHologram();
        }

        if (controlModeKey.wasPressed()) {
            toggleControlMode();
        }

        if (controlModeActive && vncManager.isConnected()) {
            handleControlModeInput(client);
        }

        if (vncManager.isConnected()) {
            vncManager.updateFrame();
        }

        hologramManager.update();
    }

    private void toggleControlMode() {
        if (controlModeActive) {
            exitControlMode();
        } else if (hologramManager.isHologramActive() && vncManager.isConnected()) {
            enterControlMode();
        }
    }

    private void enterControlMode() {
        controlModeActive = true;
        wasMouseGrabbed = Mouse.isGrabbed();
        if (!wasMouseGrabbed) {
            MinecraftClient.getInstance().mouse.lockCursor();
        }
        lastMouseX = Mouse.getX();
        lastMouseY = Mouse.getY();
        hologramManager.startControlling();
        sendMessage("Control mode: ON. ESC to exit.");
        LOGGER.info("Entered control mode");
    }

    private void exitControlMode() {
        controlModeActive = false;
        if (!wasMouseGrabbed) {
            MinecraftClient.getInstance().mouse.unlockCursor();
        }
        vncManager.releaseAllKeys();
        hologramManager.stopControlling();
        sendMessage("Control mode: OFF");
        LOGGER.info("Exited control mode");
    }

    private void handleControlModeInput(MinecraftClient client) {
        if (client.currentScreen != null) {
            exitControlMode();
            return;
        }

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        
        int deltaX = mouseX - lastMouseX;
        int deltaY = mouseY - lastMouseY;
        
        if (Math.abs(deltaX) > 0 || Math.abs(deltaY) > 0) {
            HologramManager.Vec2f pos = hologramManager.getClickPositionOnHologram();
            vncManager.sendMouseMove(pos.x, pos.y);
        }
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            boolean state = Mouse.getEventButtonState();
            
            if (button == -1) {
                int wheelDelta = Mouse.getEventDWheel();
                if (wheelDelta != 0) {
                    vncManager.sendMouseWheel(wheelDelta / 120);
                }
                continue;
            }
            
            if (button >= 0 && button <= 2) {
                HologramManager.Vec2f pos = hologramManager.getClickPositionOnHologram();
                vncManager.sendMouseClick(pos.x, pos.y, button, state);
            }
        }
    }

    private void onAfterEntities(WorldRenderContext context) {
        if (hologramManager != null && hologramManager.isHologramActive()) {
            hologramManager.renderHologram(
                context.matrixStack(),
                context.consumers(),
                context.tickCounter().getTickDelta(true)
            );
        }
    }

    private void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
        }
    }
}
