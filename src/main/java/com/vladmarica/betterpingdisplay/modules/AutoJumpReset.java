package com.vladmarica.betterpingdisplay.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class AutoJumpReset {
	private final int keyCode;
	private boolean enabled = true;
	private boolean keyWasDown;

	public AutoJumpReset(int keyCode) {
		this.keyCode = keyCode;
	}

	public void handleToggle(MinecraftClient client) {
		boolean keyDown = InputUtil.isKeyPressed(client.getWindow(), keyCode);
		if (keyDown && !keyWasDown) {
			enabled = !enabled;
			ModuleManager.playToggleSound(client, enabled);
		}
		keyWasDown = keyDown;
	}

	private int lastHurtTime;
	private int targetJumpHurtTime;

	public void tick(MinecraftClient client) {
		if (!enabled || client.player == null) {
			return;
		}

		int currentHurtTime = client.player.hurtTime;

		// When fresh damage is taken (hurtTime jumps to 10), pick a random delay
		if (currentHurtTime == 10 && lastHurtTime < 10) {
			// Pick a random hurtTime to jump at between 7 and 9 (1 to 3 ticks delay)
			targetJumpHurtTime = 7 + (int)(Math.random() * 3);
		}

		if (currentHurtTime == targetJumpHurtTime && currentHurtTime < lastHurtTime) {
			if (client.player.isOnGround() && client.player.isSprinting()) {
				client.player.jump();
			}
			targetJumpHurtTime = 0; // Prevent jumping again for this hit
		}

		lastHurtTime = currentHurtTime;
	}
}
