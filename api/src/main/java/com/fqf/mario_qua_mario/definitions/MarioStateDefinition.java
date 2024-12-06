package com.fqf.mario_qua_mario.definitions;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface MarioStateDefinition {
	@NotNull Identifier getID();

	void clientTick(IMarioClientData data, boolean isSelf);
	void serverTick(IMarioAuthoritativeData data);
}
