package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public interface IMarioData {
	PlayerEntity getMario();
	boolean isClient();
	default boolean isServer() {
		return !this.isClient();
	}

	boolean isEnabled();
	Identifier getActionID();
	ActionCategory getActionCategory();
	Identifier getPowerUpID();
	Identifier getCharacterID();

	boolean hasPower(String power);

	double getStat(CharaStat stat);
	float getHorizontalScale();
	float getVerticalScale();
	int getBumpStrengthModifier();

	<T> T getVars(Class<T> clazz);
}
