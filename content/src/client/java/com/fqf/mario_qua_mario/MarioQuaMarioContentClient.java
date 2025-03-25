package com.fqf.mario_qua_mario;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class MarioQuaMarioContentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMarioContent.LOGGER.info("Mario qua Mario Content Client initializing...");
	}

	public static class ContentClientHelperImplemented extends MarioQuaMarioContent.ContentClientHelper {
		@Override
		public Text getBackflipDismountText() {
			GameOptions options = MinecraftClient.getInstance().options;
			return Text.translatable("mount.onboard.mario", options.sneakKey.getBoundKeyLocalizedText(), options.jumpKey.getBoundKeyLocalizedText());
		}
	}
}