package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.stomp_types.JumpStomp;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MarioContentEventListeners {
	private static final String NAGGED_TAG = "mqm_nagged";

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if(entity instanceof ServerPlayerEntity mario) {
				IMarioData data = mario.mqm$getIMarioData();
				if(data.isEnabled() && source.isDirect()) {
					Entity attacker = source.getAttacker();
					if(attacker != null) {
						if (JumpStomp.collidingFromTop(attacker, mario, null, false))
							return false;
						else
							MarioQuaMarioContent.LOGGER.info("Allowed Mario to take damage.\nMario Y: {}\nAttacker Top: {}",
									mario.getY(), attacker.getY() + attacker.getHeight());
					}
				}
			}
			return true;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			int timesDisconnected = handler.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME));
			MarioQuaMarioContent.LOGGER.info("\nPlaytime: {}\nDisconnected: {}\nEnabled: {}", handler.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)), timesDisconnected, isPlayerEnabledInNBT(handler.player));
			if(timesDisconnected == 0 && MarioQuaMarioContent.CONFIG.isWelcomeMessageEnabled()) {
				// Player joined for the first time; say hi and tell them how to enable the mod
				String marioCommand = "/mario set character " + Mario.ID;
				Text marioCommandText = Text.literal(marioCommand)
						.formatted(Formatting.UNDERLINE)
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, marioCommand)));
				handler.player.sendMessage(Text.translatable("messages.mario_qua_mario.welcome1").append(marioCommandText).append(Text.translatable("messages.mario_qua_mario.welcome2")));
			}
			else if(timesDisconnected >= 3 && handler.player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) > 48000 && !handler.player.getCommandTags().contains(NAGGED_TAG) && MarioQuaMarioContent.CONFIG.isNagMessageEnabled() && isPlayerEnabledInNBT(handler.player)) {
				handler.player.addCommandTag(NAGGED_TAG); // Sorry
				MutableText nagMessage = Text.translatable("messages.mario_qua_mario.nag1");

				// Ko-fi
				nagMessage.append(Text.translatable("messages.mario_qua_mario.link1")
						.formatted(Formatting.UNDERLINE)
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ko-fi.com/floralquafloral"))));

				// Commission
				nagMessage.append(Text.translatable("messages.mario_qua_mario.link2").append(Text.translatable("messages.mario_qua_mario.link3")
						.formatted(Formatting.UNDERLINE) // TODO Insert real commission link
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://en.wikipedia.org/wiki/Sans_(Undertale)")))));

				nagMessage.append(Text.translatable("messages.mario_qua_mario.nag2"));
				nagMessage.append(Text.translatable("text.autoconfig.mario_qua_mario_content.option.nagMessage"));
				nagMessage.append(Text.translatable("messages.mario_qua_mario.nag3"));

				handler.player.sendMessage(nagMessage);
			}
		});
	}

	private static boolean isPlayerEnabledInNBT(ServerPlayerEntity mario) {
		return new EntityDataObject(mario).getNbt().getCompound("mario_qua_mario.data").getBoolean("Enabled");
	}
}
