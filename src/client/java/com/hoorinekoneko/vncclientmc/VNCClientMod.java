package com.hoorinekoneko.vncclientmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
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

        if (vncManager.isConnected()) {
            vncManager.updateFrame();
        }

        hologramManager.update();
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
}
