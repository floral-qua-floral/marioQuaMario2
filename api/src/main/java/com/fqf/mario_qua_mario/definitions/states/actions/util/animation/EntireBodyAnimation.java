package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;

public record EntireBodyAnimation(float pivotHeightFactor, @NotNull Arrangement.Mutator mutator)
		implements PlayermodelAnimation.MutatorContainer {
}
