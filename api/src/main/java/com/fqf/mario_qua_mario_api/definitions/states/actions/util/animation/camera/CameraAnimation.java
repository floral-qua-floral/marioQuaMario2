package com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;

public record CameraAnimation(
		CameraProgressHandler progressHandler,
		Arrangement.Mutator mutator
) {

}
