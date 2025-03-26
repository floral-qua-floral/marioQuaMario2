package com.fqf.mario_qua_mario.powerups;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.power.TailStall;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.util.Easing;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Raccoon implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("raccoon");
	}

	@Override public @Nullable Identifier getReversionTarget() {
		return MarioQuaMarioContent.makeID("super");
	}
	@Override public int getValue() {
		return 2;
	}

	@Override public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public float getVoicePitch() {
		return 1;
	}
	@Override public float getJumpPitch() {
		return 1F;
	}

	@Override public Set<String> getPowers() {
		return Set.of(
				Powers.SMB3_IDLE,
				Powers.TAIL_ATTACK,
				Powers.TAIL_STALL,
				Powers.TAIL_FLY
		);
	}

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		return helper.auto();
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	public static class RaccoonVars {
		public int flightTicks;
		public @Nullable Double stallStartVel;
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new RaccoonVars();
	}
	private void tick(IMarioData data) {
		RaccoonVars vars = data.getVars(RaccoonVars.class);
		if(data.getActionCategory() != ActionCategory.AIRBORNE && data.getActionCategory() != ActionCategory.WALLBOUND) {
			vars.stallStartVel = null;
			vars.flightTicks = 75;
		}
		else {
			if(vars.stallStartVel != null)
				vars.stallStartVel = Math.max(vars.stallStartVel + TailStall.FALL_ACCEL.get(data), TailStall.FALL_SPEED.get(data));
			vars.flightTicks--;
		}
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		tick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		tick(data);
	}

	private abstract static class TailAttack implements AttackInterceptionDefinition {
		private final Identifier ACTION_TARGET;
		private final PlayermodelAnimation ANIMATION;

		private TailAttack(Identifier actionTarget, PlayermodelAnimation animation) {
			this.ACTION_TARGET = actionTarget;
			this.ANIMATION = animation;
		}

		@Override
		public @Nullable Identifier getActionTarget() {
			return this.ACTION_TARGET;
		}

		@Override
		public @Nullable Hand getHandToSwing() {
			return null;
		}

		@Override
		public boolean shouldTriggerAttackCooldown() {
			return true;
		}

		@Override
		public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
			return weapon.isEmpty() && attackCooldownProgress >= 1 && this.testSwing(data);
		}

		protected abstract boolean testSwing(IMarioReadableMotionData data);

		@Override
		public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
			return miningTicks <= 3 ? MiningHandling.HOLD : MiningHandling.MINE;
		}

		@Override
		public void executeTravellers(IMarioTravelData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

		}

		@Override
		public void executeClients(IMarioClientData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed) {
			data.forceBodyAlignment(false);
			data.playSound(MarioContentSFX.TAIL_WHIP, seed);
			if(this.ACTION_TARGET == null) {
				data.voice(Voicelines.TAIL_WHIP, seed);
				data.playAnimation(this.ANIMATION, TAIL_WHIP_ANIMATION_DURATION);
			}
			else data.voice(Voicelines.TAIL_SPIN, seed);
		}

		@Override
		public void executeServer(IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress, ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {
			data.getMario().spawnSweepAttackParticles();
			if(entityTarget != null) {
				ServerPlayerEntity mario = data.getMario();
				DamageSource damageSource = mario.getDamageSources().playerAttack(mario);
				entityTarget.damage(damageSource, 5.75F);
				List<LivingEntity> sweepTargets = mario.getWorld().getNonSpectatingEntities(LivingEntity.class, entityTarget.getBoundingBox().expand(1.0, 0.25, 1.0));
				for(LivingEntity sweepTarget : sweepTargets) {
					if(
							sweepTarget != mario
							&& sweepTarget != entityTarget
							&& !mario.isTeammate(sweepTarget)
							&& !(sweepTarget instanceof ArmorStandEntity stand && stand.isMarker())
							&& mario.squaredDistanceTo(sweepTarget) < 9.0) {
						sweepTarget.takeKnockback(
								0.4F, MathHelper.sin(mario.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(mario.getYaw() * (float) (Math.PI / 180.0))
						);
						sweepTarget.damage(damageSource, 4);
					}
				}
			}
		}
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		PlayermodelAnimation tailWhipAnimation = makeTailWhipAnimation(animationHelper);

		return List.of(
				new TailAttack(MarioQuaMarioContent.makeID("tail_spin_grounded"), null) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return data.getMario().isOnGround() && data.getMario().isInSneakingPose();
					}
				},
				new TailAttack(MarioQuaMarioContent.makeID("tail_spin_fall"), null) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return !data.getMario().isOnGround() && data.getMario().isInSneakingPose();
					}
				},
				new TailAttack(null, tailWhipAnimation) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return true;
					}
				},
				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isEmpty();
					}
				}
		);
	}

	private static final int TAIL_WHIP_ANIMATION_DURATION = 7;
	private static PlayermodelAnimation makeTailWhipAnimation(AnimationHelper helper) {
		LimbAnimation armAnimation = new LimbAnimation(false, (data, arrangement, progress) -> {
			float factor = progress < 0.5 ? progress * 2 : progress * -2 + 2;
			arrangement.addPos(0, MathHelper.lerp(factor, 2, 4), MathHelper.lerp(factor, 2, 4));
			arrangement.pitch -= MathHelper.lerp(factor, 16, 90);
		});
		LimbAnimation legAnimation = new LimbAnimation(false, (data, arrangement, progress) -> {
			float factor = progress < 0.5 ? progress * 2 : progress * -2 + 2;
			arrangement.addPos(0, factor * 2, factor * 6);
			arrangement.pitch -= factor * 39;
		});
		return new PlayermodelAnimation(
				null,
				new ProgressHandler(TAIL_WHIP_ANIMATION_DURATION, false, Easing.SINE_IN_OUT),

				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) ->
						arrangement.yaw = progress * 360),
				new BodyPartAnimation((data, arrangement, progress) -> {
					float factor = progress < 0.5 ? progress * 2 : progress * -2 + 2;
					arrangement.addPos(0, factor * 4.3F, factor * -1);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					float factor = progress < 0.5 ? progress * 2 : progress * -2 + 2;
					arrangement.addPos(0, factor * 4.3F, factor * -1);
					arrangement.pitch += factor * 42;
				}),
				armAnimation, armAnimation,
				legAnimation, legAnimation,
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.pitch = helper.interpolateKeyframes(progress * 2, 0, MathHelper.clamp(data.getMario().getPitch() - 30, -80, 75), 20);
				})
		);
	}
}
