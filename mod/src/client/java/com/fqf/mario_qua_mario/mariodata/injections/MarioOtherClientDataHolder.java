package com.fqf.mario_qua_mario.mariodata.injections;

import com.fqf.mario_qua_mario.mariodata.MarioOtherClientData;
import org.jetbrains.annotations.NotNull;

public interface MarioOtherClientDataHolder extends MarioDataHolder {
	@SuppressWarnings("DataFlowIssue") @Override
	default @NotNull MarioOtherClientData mqm$getMarioData() {
		return null;
	}
}
