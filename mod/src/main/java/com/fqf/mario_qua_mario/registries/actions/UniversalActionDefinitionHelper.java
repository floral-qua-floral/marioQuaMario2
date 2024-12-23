package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class UniversalActionDefinitionHelper implements
		GroundedActionDefinition.GroundedActionHelper,
		AirborneActionDefinition.AirborneActionHelper,
		AquaticActionDefinition.AquaticActionHelper,
		WallboundActionDefinition.WallboundActionHelper,
		MountedActionDefinition.MountedActionHelper {
	public static final UniversalActionDefinitionHelper INSTANCE = new UniversalActionDefinitionHelper();
	private UniversalActionDefinitionHelper() {}

	@Override
	public void groundAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		double slipFactor = getSlipFactor(data);

		data.approachAngleAndAccel(
				forwardAccelStat.get(data) * slipFactor, forwardSpeedStat.get(data) * data.getInputs().getForwardInput(),
				strafeAccelStat.get(data) * slipFactor, strafeSpeedStat.get(data) * data.getInputs().getStrafeInput(),
				forwardAngleContribution, strafeAngleContribution, redirectStat.get(data) * slipFactor
		);
	}

	@Override
	public void applyDrag(
			IMarioTravelData data,
			CharaStat drag, CharaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirection
	) {
		double dragValue = drag.get(data);
		boolean dragInverted = dragValue < 0;
		double slipFactor = getSlipFactor(data);
		double dragMinValue = dragMin.get(data) * slipFactor;
		if(!dragInverted) dragValue *= slipFactor;


		Vector2d deltaVelocities = new Vector2d(
				-dragValue * data.getForwardVel(),
				-dragValue * data.getStrafeVel()
		);
		double dragVelocitySquared = deltaVelocities.lengthSquared();
		if(dragVelocitySquared != 0 && dragVelocitySquared < dragMinValue * dragMinValue)
			deltaVelocities.normalize(dragMinValue);

		if(dragInverted) {
			data.setForwardStrafeVel(data.getForwardVel() + deltaVelocities.x, data.getStrafeVel() + deltaVelocities.y);
		}
		else {
			data.approachAngleAndAccel(
					deltaVelocities.x, 0,
					deltaVelocities.y, 0,
					forwardAngleContribution,
					strafeAngleContribution,
					redirection.get(data) * slipFactor
			);
		}
	}

	@Override
	public double getSlipFactor(IMarioReadableMotionData data) {
		return Math.pow(0.6 / getFloorSlipperiness(data.getMario()), 3);
	}
	private static float getFloorSlipperiness(Entity stepper) {
		if(stepper.isOnGround()) {
			BlockPos blockPos = stepper.getVelocityAffectingPos();
			return stepper.getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}

	@Override
	public void performJump(IMarioTravelData data, CharaStat jumpVel, @Nullable CharaStat speedAddend) {
		double newYVel = jumpVel.get(data);
		if(speedAddend != null) newYVel += speedAddend.get(data) * getSpeedFactor(data);
		data.setYVel(newYVel);
	}
	private double getSpeedFactor(IMarioTravelData data) {
		double scaledForwardVel = data.getForwardVel();
		if(scaledForwardVel < 0) return scaledForwardVel * 0.2;
		if(scaledForwardVel < 1) return scaledForwardVel * scaledForwardVel;
		return scaledForwardVel;
	}

	@Override public void applyGravity(
			IMarioTravelData data,
			CharaStat gravity, @Nullable CharaStat jumpingGravity,
			CharaStat terminalVelocity
	) {
		this.applyGravity(data, (jumpingGravity == null || data.getTimers().jumpCapped) ? gravity : jumpingGravity, terminalVelocity);
	}

	@Override public void airborneAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {

	}

	@Override
	public TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold) {
		return null;
	}

	@Override
	public void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity) {
		double maxFallSpeed = terminalVelocity.get(data);
		double yVel = data.getYVel();
		if(yVel > maxFallSpeed) {
			yVel += gravity.get(data);
			data.setYVel(Math.max(maxFallSpeed, yVel));
		}
	}

	@Override
	public void applyWaterDrag(IMarioTravelData data, CharaStat drag, CharaStat dragMin) {

	}

	@Override
	public void aquaticAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {

	}

	@Override
	public WallboundActionDefinition.WallInfo getWallInfo(IMarioReadableMotionData data) {
		return ((MarioMoveableData) data).getWallInfo();
	}

	@Override public void climbWall(
			IMarioTravelData data,
			CharaStat ascendSpeedStat,CharaStat ascendAccelStat,
			CharaStat descendSpeedStat, CharaStat descendAccelStat,
			CharaStat sidleSpeedStat, CharaStat sidleAccelStat
	) {

	}

	@Override
	public void setSidleVel(IMarioTravelData data, double sidleVel) {

	}

	@Override
	public Entity getMount(IMarioReadableMotionData data) {
		return data.getMario().getVehicle();
	}

	@Override
	public void dismount(IMarioTravelData data, boolean reposition) {
		((MarioPlayerData) data).attemptDismount = reposition
				? MarioPlayerData.DismountType.VANILLA_DISMOUNT
				: MarioPlayerData.DismountType.DISMOUNT_IN_PLACE;
	}

	@Override
	public double getSlipFactor(Entity mount) {
		return mount.isOnGround() ? 1.0 : Math.pow(0.6 / getFloorSlipperiness(mount), 3);
	}
}
