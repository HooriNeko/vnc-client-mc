package com.hoorinekoneko.vncclientmc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VNCClientMod implements ModInitializer {
    public static final String MOD_ID = "vnc-client-mc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static VNCManager vncManager;
    public static HologramRenderer hologramRenderer;

    private static final String DEFAULT_VNC_HOST = "localhost";
    private static final int DEFAULT_VNC_PORT = 5900;

    private KeyBinding toggleHologramKey;

    @Override
    public void onInitialize() {
        LOGGER.info("VNC Client For MC initializing...");

        vncManager = new VNCManager();
        hologramRenderer = new HologramRenderer();

        toggleHologramKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vnc-client-mc.toggle_hologram",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.vnc-client-mc"
        ));

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);

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
                .then(CommandManager.literal("hologram")
                    .then(CommandManager.literal("show")
                        .executes(ctx -> {
                            hologramRenderer.setVisible(true);
                            sendFeedback("Hologram enabled");
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("hide")
                        .executes(ctx -> {
                            hologramRenderer.setVisible(false);
                            sendFeedback("Hologram disabled");
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("scale")
                        .then(CommandManager.argument("value", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.floatArg())
                            .executes(ctx -> {
                                float scale = ctx.getArgument("value", Float.class);
                                hologramRenderer.setScale(scale);
                                sendFeedback("Hologram scale: " + scale);
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("distance")
                        .then(CommandManager.argument("value", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.floatArg())
                            .executes(ctx -> {
                                float distance = ctx.getArgument("value", Float.class);
                                hologramRenderer.setDistance(distance);
                                sendFeedback("Hologram distance: " + distance);
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("quality")
                        .then(CommandManager.argument("level", net.fabricmc.fabric.api.client.command.v2.ArgumentTypes.integer())
                            .executes(ctx -> {
                                int quality = ctx.getArgument("level", Integer.class);
                                hologramRenderer.setQuality(quality);
                                sendFeedback("Hologram quality: " + quality + " (32=low, 64=medium, 128=high)");
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("toggle")
                        .executes(ctx -> {
                            hologramRenderer.toggleVisible();
                            sendFeedback("Hologram " + (hologramRenderer.isVisible() ? "enabled" : "disabled"));
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("status")
                    .executes(ctx -> {
                        vncManager.printStatus();
                        return 1;
                    })
                )
        );
    }

    private void onClientTick(MinecraftClient client) {
        if (toggleHologramKey.wasPressed()) {
            hologramRenderer.toggleVisible();
            sendFeedback("Hologram " + (hologramRenderer.isVisible() ? "enabled" : "disabled"));
        }

        if (vncManager.isConnected()) {
            vncManager.updateFrame();
        }
    }

    private void onHudRender(net.minecraft.client.gui.DrawContext drawContext, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (hologramRenderer != null && hologramRenderer.isVisible()) {
            hologramRenderer.render(
                drawContext.getMatrices(),
                drawContext.getVertexConsumers(),
                tickCounter.getTickDelta(true)
            );
        }
    }

    private void sendFeedback(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal(message), false);
        }
    }
}
