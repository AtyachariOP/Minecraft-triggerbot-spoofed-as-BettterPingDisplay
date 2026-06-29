package com.vladmarica.betterpingdisplay.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
	private static TriggerBot triggerBot;
	private static AutoJumpReset autoJumpReset;
	private static AutoPot autoPot;
	private static boolean initialized;

	private ModuleManager() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		// Hardcoded keys — hidden from MC controls
		triggerBot = new TriggerBot(GLFW.GLFW_KEY_B);
		autoJumpReset = new AutoJumpReset(GLFW.GLFW_KEY_J);
		autoPot = new AutoPot(GLFW.GLFW_KEY_U);

		ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());

		initialized = true;
	}

	public static void onTick() {
		MinecraftClient client = MinecraftClient.getInstance();

		triggerBot.handleToggle(client);
		autoJumpReset.handleToggle(client);
		autoPot.handleToggle(client);

		triggerBot.tick(client);
		autoJumpReset.tick(client);
		autoPot.tick(client);
	}

	static void playToggleSound(MinecraftClient client, boolean enabled) {
		if (client.player == null || client.world == null) {
			return;
		}

		client.world.playSound(
				client.player,
				client.player.getX(),
				client.player.getY(),
				client.player.getZ(),
				SoundEvents.BLOCK_NOTE_BLOCK_PLING,
				SoundCategory.PLAYERS,
				0.7f,
				enabled ? 1.8f : 0.6f);
	}
}
