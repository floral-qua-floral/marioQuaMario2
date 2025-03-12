package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import com.fqf.mario_qua_mario.util.MarioVars;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractMario implements CharacterDefinition {
	@Override public @NotNull Identifier getInitialAction() {
		return MarioQuaMarioContent.makeID("fall");
	}
	@Override public @NotNull Identifier getInitialPowerUp() {
		return MarioQuaMarioContent.makeID("super");
	}

	@Override public @NotNull Identifier getMountedAction(Entity vehicle) {
		return MarioQuaMarioContent.makeID("mounted");
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public Set<String> getPowers() {
		return Set.of(
				Powers.LIGHTNING_SHRINKS,
				Powers.CEILING_CLIPPING
		);
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new MarioVars();
	}
	private static void commonTick(IMarioData data) {
		MarioVars vars = data.getVars(MarioVars.class);
		vars.canDoubleJumpTicks--;
		vars.canTripleJumpTicks--;
		switch(data.getActionCategory()) {
			case AIRBORNE -> {
				if(vars.pSpeed < 1)
					vars.pSpeed -= 0.1; // P-Speed decays while airborne, unless it's full
			}
			case AQUATIC ->
					vars.pSpeed -= 0.225; // P-Speed decays faster while waterborne
			default ->
					// P-Speed is wiped while grounded, mounted, on a wall, or in a Generic action
					// Individual actions can change this by assigning a value to P-Speed themselves!
					vars.pSpeed = 0;
		}
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		commonTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		commonTick(data);
	}
}
