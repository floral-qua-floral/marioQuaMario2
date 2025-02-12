package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class TailStall extends Fall implements AirborneActionDefinition {
	private static final Identifier ID = MarioQuaMarioContent.makeID("tail_stall");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.x -= factor;
			arrangement.roll += 100 * factor + MathHelper.sin(progress) * 5;
			arrangement.pitch += factor * MathHelper.cos(progress) * 11;
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += factor * MathHelper.cos(progress) * 70F;
		});
	}
	public static LimbAnimation makeTailAnimation(boolean useProgress) {
		return new LimbAnimation(
				false, (data, arrangement, progress) -> {
					float value;
					if(useProgress) value = progress;
					else value = data.getVars(ActionTimerVars.class).actionTimer * 1.1F;
					arrangement.setAngles(
							MathHelper.sin(value * 1.2F) * 38.3F,
							MathHelper.sin(value * 0.6F) * 52,
							0
					);
				}
		);
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> ticksPassed * 1.1F),
				null,
				null,
				null,
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				makeTailAnimation(true)
		);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.IF_ALREADY_SPRINTING;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat FALL_ACCEL = new CharaStat(-0.0125, NORMAL_GRAVITY, POWER_UP);
	public static final CharaStat FALL_SPEED = new CharaStat(-0.4, TERMINAL_VELOCITY, POWER_UP);

	public static void tailWaggleTick(IMarioClientData data) {
		if(data.getVars(ActionTimerVars.class).actionTimer++ % 4 == 0)
			data.playSound(MarioContentSFX.TAIL_FLY, 1F, 0.48F, data.getMario().getRandom().nextLong());
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		tailWaggleTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
	}

	protected static final TransitionDefinition END_STALLING = new TransitionDefinition(
			MarioQuaMarioContent.makeID("fall"),
			data -> !data.getInputs().JUMP.isHeld() || data.getYVel() > 0,
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("special_fall"), // special fall coming in CLUTCH!
						data -> !data.hasPower(Powers.TAIL_STALL),
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				END_STALLING
		);
	}

	public static final CharaStat STALL_THRESHOLD = new CharaStat(-0.299, THRESHOLD, POWER_UP);
	private static final TransitionDefinition STALL_TRANSITION = new TransitionDefinition(
			MarioQuaMarioContent.makeID("tail_stall"),
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& !data.getMario().isSneaking()
					&& !data.getActionID().equals(ID)
					&& !data.getActionID().equals(PJump.ID)
					&& (data.isServer() || (
							data.getYVel() < STALL_THRESHOLD.get(data)
							&& data.getInputs().JUMP.isHeld()
					)),
			EvaluatorEnvironment.CLIENT_CHECKED,
			data -> data.setYVel(STALL_THRESHOLD.get(data) * 0.2),
			null
	);
	private static final TransitionDefinition DUCK_STALL_TRANSITION = STALL_TRANSITION.variate(
			MarioQuaMarioContent.makeID("tail_stall_duck"),
			data ->
					data.hasPower(Powers.TAIL_STALL)
					&& data.getMario().isSneaking()
					&& !data.getActionID().equals(ID)
					&& !data.getActionID().equals(PJump.ID)
					&& (data.isServer() || (
							data.getYVel() < STALL_THRESHOLD.get(data)
							&& data.getInputs().JUMP.isHeld()
					))
	);
	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition( // TODO: Change this to be after transitions into mqm:ground_pound.
						TransitionInjectionDefinition.InjectionPlacement.AFTER,
						MarioQuaMarioContent.makeID("sub_walk"),
						TransitionInjectionDefinition.ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> STALL_TRANSITION
				),
				new TransitionInjectionDefinition( // TODO: Change this to be after transitions into mqm:fall or mqm:jump
						TransitionInjectionDefinition.InjectionPlacement.AFTER,
						MarioQuaMarioContent.makeID("duck_waddle"),
						TransitionInjectionDefinition.ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> DUCK_STALL_TRANSITION
				)
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
