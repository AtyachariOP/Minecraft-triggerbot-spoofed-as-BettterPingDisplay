package com.vladmarica.betterpingdisplay.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class TriggerBot {
	private final int keyCode;
	private boolean enabled = true;
	private boolean keyWasDown;

	public TriggerBot(int keyCode) {
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

	public void tick(MinecraftClient client) {
		if (!enabled) {
			return;
		}

		if (client.player == null || client.interactionManager == null) {
			return;
		}

		String itemName = client.player.getMainHandStack().getItem().getTranslationKey();
		if (!itemName.contains("sword") && !itemName.contains("axe")) {
			return;
		}

		// Must not be using item (blocking, eating, drinking, drawing bow)
		if (client.player.isUsingItem()) {
			return;
		}

		// Must not have inventory or other screen open
		if (client.currentScreen instanceof HandledScreen) {
			return;
		}

		// Player must be alive
		if (client.player.getHealth() <= 0.0f) {
			return;
		}

		// Must have a targeted entity
		Entity targetEntity = client.targetedEntity; // client.targetedEntity is mapped to field_1692 (crosshairTarget entity)
		if (targetEntity == null && client.crosshairTarget != null) {
			// Fallback if targetedEntity isn't populated (it usually is by game renderer)
			if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hitResult) {
				targetEntity = hitResult.getEntity();
			}
		}

		if (!(targetEntity instanceof LivingEntity target)) {
			return;
		}

		// Target must be alive
		if (target.getHealth() <= 0.0f) {
			return;
		}

		if (client.player.isOnGround()) {
			if (!client.player.isSprinting()) {
				return;
			}
			if ((double) client.player.getAttackCooldownProgress(0.5f) < 0.85 + Math.random() * 0.1) {
				return;
			}
			client.interactionManager.attackEntity(client.player, target);
			client.player.swingHand(Hand.MAIN_HAND);
		} else {
			if ((double) client.player.getAttackCooldownProgress(0.5f) < 0.85 + Math.random() * 0.05) {
				return;
			}
			if (client.player.getVelocity().y > -0.1) {
				return;
			}
			client.interactionManager.attackEntity(client.player, target);
			client.player.swingHand(Hand.MAIN_HAND);
		}
	}
}
