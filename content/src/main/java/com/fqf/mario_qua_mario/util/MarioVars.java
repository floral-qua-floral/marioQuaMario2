package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.mariodata.IMarioData;

public class MarioVars {
	public int canDoubleJumpTicks = 0;
	public int canTripleJumpTicks = 0;
	public double pSpeed = 0; // Range 0-1

	public static MarioVars get(IMarioData data) {
		return data.getVars(MarioVars.class);
	}
}
