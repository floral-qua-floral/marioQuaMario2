package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.actions.TransitionPhase;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;

import java.util.Objects;

public class MarioServerPlayerData extends MarioMoveableData implements IMarioAuthoritativeData {
	private ServerPlayerEntity mario;
	public MarioServerPlayerData() {
		super();
	}
	@Override public ServerPlayerEntity getMario() {
		return this.mario;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.mario = (ServerPlayerEntity) mario;
		super.setMario(mario);
	}

	@Override public void setEnabled(boolean enable) {

	}

	@Override public boolean transitionToAction(Identifier actionID) {
		long seed = RandomSeed.getSeed();
		AbstractParsedAction toAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		if(this.setAction(this.getAction(), toAction, seed, false, false)) {
			MarioDataPackets.transitionToActionS2C(this.getMario(), true, this.getAction(), toAction, seed);
		}
		return false;
	}
	@Override public boolean transitionToAction(String actionID) {
		return this.transitionToAction(Identifier.of(actionID));
	}

	@Override public void assignAction(Identifier actionID) {
		AbstractParsedAction newAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		this.setActionTransitionless(newAction);
		MarioDataPackets.assignActionS2C(this.getMario(), true, newAction);
	}
	@Override public void assignAction(String actionID) {
		this.assignAction(Identifier.of(actionID));
	}

	@Override public void empowerTo(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		this.setPowerUp(newPowerUp, false, seed);
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, false, seed);
	}
	@Override public void empowerTo(String powerUpID) {
		this.empowerTo(Identifier.of(powerUpID));
	}

	@Override public void revertTo(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		this.setPowerUp(newPowerUp, true, seed);
		MarioDataPackets.empowerRevertS2C(this.getMario(), newPowerUp, true, seed);
	}
	@Override public void revertTo(String powerUpID) {
		this.revertTo(Identifier.of(powerUpID));
	}

	@Override public void assignPowerUp(Identifier powerUpID) {
		ParsedPowerUp newPowerUp = Objects.requireNonNull(RegistryManager.POWER_UPS.get(powerUpID),
				"Target power-up (" + powerUpID + ") doesn't exist!");

		this.setPowerUpTransitionless(newPowerUp);
		MarioDataPackets.assignPowerUpS2C(this.getMario(), newPowerUp);
	}
	@Override public void assignPowerUp(String powerUpID) {
		this.assignPowerUp(Identifier.of(powerUpID));
	}

	@Override public void assignCharacter(Identifier characterID) {
		ParsedCharacter newCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(characterID),
				"Target character (" + characterID + ") doesn't exist!");

		this.setCharacter(newCharacter);
		MarioDataPackets.assignCharacterS2C(this.getMario(), newCharacter);
	}
	@Override public void assignCharacter(String characterID) {
		this.assignCharacter(Identifier.of(characterID));
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.getAction().serverTick(this);
		this.getPowerUp().serverTick(this);
		this.getCharacter().serverTick(this);
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);

		this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.getMario().move(MovementType.SELF, this.getMario().getVelocity());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);

		this.getMario().updateLimbs(false);
		return true;
	}

	@Override public MarioInputs getInputs() {
		return PHONY_INPUTS;
	}

	private static final MarioInputs PHONY_INPUTS;
	static {
		MarioInputs.MarioButton phonyButton = new MarioInputs.MarioButton() {
			@Override public boolean isPressed() {
				return false;
			}
			@Override public boolean isHeld() {
				return false;
			}
		};
		PHONY_INPUTS = new MarioInputs(phonyButton, phonyButton, phonyButton) {
			@Override public double getForwardInput() {
				return 0;
			}
			@Override public double getStrafeInput() {
				return 0;
			}
			@Override public boolean isReal() {
				return false;
			}
		};
	}
}
