package com.fqf.mario_qua_mario.definitions.states.actions.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param targetID The ID of the action that this transition leads to.
 * @param evaluator The Evaluator that determines if this transition should fire.
 * @param travelExecutor The effect the transition has on Mario's motion.
 * @param clientsExecutor Client-side effects of this transition firing.
 */
public record TransitionDefinition(
		@NotNull Identifier targetID,
		@NotNull Evaluator evaluator, @NotNull EvaluatorEnvironment environment,
		@Nullable TravelExecutor travelExecutor,
		@Nullable ClientsExecutor clientsExecutor
) {
	/**
	 * Alternate constructor provided for convenience
	 */
	public TransitionDefinition(@NotNull Identifier targetID, @NotNull Evaluator evaluator, @NotNull EvaluatorEnvironment context) {
		this(targetID, evaluator, context, null, null);
	}

	/**
	 * Runs on the client-side to test if the associated transition should occur.
	 */
	@FunctionalInterface public interface Evaluator {
		boolean shouldTransition(IMarioReadableMotionData data);
	}

	/**
	 * Runs on the main client and on the server when the associated transition occurs.
	 * In a multiplayer environment, this won't run on your client when another player does the transition, but it will
	 * when you're the one transitioning.
	 */
	@FunctionalInterface public interface TravelExecutor {
		boolean execute(IMarioTravelData data);
	}

	/**
	 * Runs on the client side for anyone who is in range to see Mario transition.
	 */
	@FunctionalInterface public interface ClientsExecutor {
		void execute(IMarioClientData data, boolean isSelf, long seed);
	}
}