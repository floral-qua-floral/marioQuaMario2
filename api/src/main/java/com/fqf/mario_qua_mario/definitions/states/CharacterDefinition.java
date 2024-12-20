package com.fqf.mario_qua_mario.definitions.states;

import net.minecraft.util.Identifier;

public interface CharacterDefinition extends StatAlteringStateDefinition {
	Identifier getInitialAction();
	Identifier getInitialPowerUp();
}