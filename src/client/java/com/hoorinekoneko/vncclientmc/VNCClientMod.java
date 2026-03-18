package com.hoorinekoneko.vncclientmc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VNCClientMod implements ModInitializer {
    public static final String MOD_ID = "vnc-client-mc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static VNCManager vncManager;
    public static HologramRenderer hologramRenderer;

    private static final String DEFAULT_VNC_HOST = "localhost";
    private static final int DEFAULT_VNC_PORT = 5900;

    @Override
    public void onInitialize() {
        LOGGER.info("VNC Client For MC initializing...");

        vncManager = new VNCManager();
        hologramRenderer = new HologramRenderer();

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
                            return 1;
                        })
                    )
                    .then(CommandManager.literal("hide")
                        .executes(ctx -> {
                            hologramRenderer.setVisible(false);
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
        if (vncManager.isConnected()) {
            vncManager.updateFrame();
        }
    }

    private void onHudRender(net.minecraft.client.gui.DrawContext drawContext, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (hologramRenderer != null) {
            hologramRenderer.render(
                drawContext.getMatrices(),
                drawContext.getVertexConsumers(),
                tickCounter.getTickDelta(true)
            );
        }
    }
}
