package com.vladmarica.betterpingdisplay.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoPot {
	private static final Field FOCUSED_SLOT_FIELD = findFocusedSlotField();
	private static final Method GET_SLOT_AT_METHOD = findGetSlotAtMethod();

	private final int keyCode;
	private boolean enabled = true;
	private int lastHoveredSlotId = -1;
	private boolean keyWasDown;

	public AutoPot(int keyCode) {
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
		if (!enabled || client.player == null || client.interactionManager == null || !(client.currentScreen instanceof InventoryScreen)) {
			resetHover();
			return;
		}

		Slot hoveredSlot = getFocusedSlot(client);
		if (hoveredSlot == null) {
			resetHover();
			return;
		}

		if (hoveredSlot.id == lastHoveredSlotId || !isPotion(hoveredSlot.getStack()) || isHotbarSlot(client, hoveredSlot)) {
			return;
		}

		Slot emptyHotbarSlot = findFirstEmptyHotbarSlot(client.player.currentScreenHandler);
		if (emptyHotbarSlot == null || emptyHotbarSlot.id == hoveredSlot.id) {
			lastHoveredSlotId = hoveredSlot.id;
			return;
		}

		lastHoveredSlotId = hoveredSlot.id;
		moveToHotbar(client, hoveredSlot.id, emptyHotbarSlot.getIndex());
		resetHover();
	}

	private void moveToHotbar(MinecraftClient client, int sourceSlotId, int targetHotbarIndex) {
		int syncId = client.player.currentScreenHandler.syncId;
		client.interactionManager.clickSlot(syncId, sourceSlotId, targetHotbarIndex, SlotActionType.SWAP, client.player);
	}

	private Slot findFirstEmptyHotbarSlot(ScreenHandler handler) {
		for (Slot slot : handler.slots) {
			if (slot.inventory == MinecraftClient.getInstance().player.getInventory()
					&& slot.getIndex() >= 0
					&& slot.getIndex() <= 8
					&& slot.getStack().isEmpty()) {
				return slot;
			}
		}
		return null;
	}

	private boolean isPotion(ItemStack stack) {
		return stack.isOf(Items.POTION)
				|| stack.isOf(Items.SPLASH_POTION)
				|| stack.isOf(Items.LINGERING_POTION);
	}

	private boolean isHotbarSlot(MinecraftClient client, Slot slot) {
		return slot.inventory == client.player.getInventory() && slot.getIndex() >= 0 && slot.getIndex() <= 8;
	}

	private Slot getFocusedSlot(MinecraftClient client) {
		if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
			return null;
		}

		Slot slotAtMouse = getSlotAtMouse(client, handledScreen);
		if (slotAtMouse != null) {
			return slotAtMouse;
		}

		if (FOCUSED_SLOT_FIELD == null) {
			return null;
		}

		try {
			return (Slot) FOCUSED_SLOT_FIELD.get(handledScreen);
		} catch (IllegalAccessException ex) {
			return null;
		}
	}

	private Slot getSlotAtMouse(MinecraftClient client, HandledScreen<?> handledScreen) {
		if (GET_SLOT_AT_METHOD == null) {
			return null;
		}

		try {
			double mouseX = client.mouse.getScaledX(client.getWindow());
			double mouseY = client.mouse.getScaledY(client.getWindow());
			return (Slot) GET_SLOT_AT_METHOD.invoke(handledScreen, mouseX, mouseY);
		} catch (ReflectiveOperationException ex) {
			return null;
		}
	}

	private void resetHover() {
		lastHoveredSlotId = -1;
	}

	private static Field findFocusedSlotField() {
		try {
			Field field = HandledScreen.class.getDeclaredField("focusedSlot");
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException ignored) {
		}

		for (Field field : HandledScreen.class.getDeclaredFields()) {
			if (field.getType() == Slot.class) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}

	private static Method findGetSlotAtMethod() {
		for (Method method : HandledScreen.class.getDeclaredMethods()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (method.getReturnType() == Slot.class
					&& parameterTypes.length == 2
					&& parameterTypes[0] == double.class
					&& parameterTypes[1] == double.class) {
				method.setAccessible(true);
				return method;
			}
		}
		return null;
	}
}
