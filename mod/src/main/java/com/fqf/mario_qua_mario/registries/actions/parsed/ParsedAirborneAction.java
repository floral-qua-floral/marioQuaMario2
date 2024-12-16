package com.fqf.mario_qua_mario.registries.actions.parsed;

import com.fqf.mario_qua_mario.definitions.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedAirborneAction extends AbstractParsedAction {
	private final AirborneActionDefinition AIRBORNE_DEFINITION;

	public ParsedAirborneAction(AirborneActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.AIRBORNE_DEFINITION = definition;
	}

	@Override
	public void travelHook(MarioMoveableData data) {
		this.AIRBORNE_DEFINITION.travelHook(data, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected TransitionInjectionDefinition.ActionCategory getCategory() {
		return TransitionInjectionDefinition.ActionCategory.AIRBORNE;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.AIRBORNE_DEFINITION.getBasicTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.AIRBORNE_DEFINITION.getInputTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.AIRBORNE_DEFINITION.getWorldCollisionTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}
}