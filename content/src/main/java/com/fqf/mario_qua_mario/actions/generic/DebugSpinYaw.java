package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.EntireBodyAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSpinYaw extends Debug {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("debug_spin_yaw");
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, new ProgressHandler(80F, true, Easing.LINEAR),
				new EntireBodyAnimation(0.5F, (data, arrangement, progress) ->
						arrangement.yaw = progress * 360),

				null, null,
				null, null,
				null, null,
				null
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
