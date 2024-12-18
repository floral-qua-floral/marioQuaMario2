package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioClientHelperManager;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.actions.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.parsed.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Set;

public class ParsedActionHelper {
	public static AbstractParsedAction parseAction(
			IncompleteActionDefinition definition,
			HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections
	) {
		return switch (definition) {
			case GenericActionDefinition def -> new ParsedGenericAction(def, allInjections);
			case GroundedActionDefinition def -> new ParsedGroundedAction(def, allInjections);
			case AirborneActionDefinition def -> new ParsedAirborneAction(def, allInjections);
			case AquaticActionDefinition def -> new ParsedAquaticAction(def, allInjections);
			case WallboundActionDefinition def -> new ParsedWallboundAction(def, allInjections);
			default -> null;
		};
	}

	public static void attemptTransitions(MarioMoveableData data, TransitionPhase phase) {
		for(ParsedTransition transition : data.isClient() ? data.getAction().CLIENT_TRANSITIONS.get(phase) : data.getAction().SERVER_TRANSITIONS.get(phase)) {
			if(transition.evaluator().shouldTransition(data)) {
				long seed = data.getMario().getRandom().nextLong();

				if(!data.isClient()) {
					MarioDataPackets.setActionS2C((ServerPlayerEntity) data.getMario(), transition.fullyNetworked(),
							data.getAction(), transition.targetAction(), seed);
				}
				else if(transition.fullyNetworked()) {
					MarioClientHelperManager.packetSender.setActionC2S(data.getAction(), transition.targetAction(), seed);
				}

				executeTransition(data, transition, seed);
				return;
			}
		}
	}

	public static boolean attemptTransitionTo(MarioPlayerData data, AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
		ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
		if(transition == null) return false;

		executeTransition(data, transition, seed);
		return true;
	}

	public static void executeTransition(MarioPlayerData data, ParsedTransition transition, long seed) {
		MarioQuaMario.LOGGER.info("Executing transition for {} on {}:\n\t{} -> {} (seed: {})",
				data.getMario().getName().getString(), data.isClient() ? "CLIENT" : "SERVER",
				data.getActionID(), transition.targetAction().ID, seed
		);

		if(data instanceof MarioMoveableData moveableData && transition.travelExecutor() != null)
			transition.travelExecutor().execute(moveableData);
		if(data instanceof IMarioClientData clientData && transition.clientsExecutor() != null)
			transition.clientsExecutor().execute(clientData, data.getMario().isMainPlayer(), seed);

		data.setActionTransitionless(transition.targetAction());
	}

	public static AbstractParsedAction get(int ID) {
		return RegistryManager.ACTIONS.get(ID);
	}
}
