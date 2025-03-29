package com.fqf.mario_qua_mario.entity;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.entity.custom.MarioFireballProjectileEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class MarioFireballRenderer extends EntityRenderer<MarioFireballProjectileEntity> {
	protected MarioFireballModel model;
	private static final Identifier TEXTURE_ID = MarioQuaMarioContent.makeResID("textures/entity/fireball/fireball.png");

	public MarioFireballRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
		this.model = new MarioFireballModel(ctx.getPart(MarioFireballModel.FIREBALL));
	}

	@Override
	public void render(MarioFireballProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevAngle, entity.angle)));
		matrices.translate(0, -1.0f, 0);

		VertexConsumer vertexconsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers,
				this.model.getLayer(TEXTURE_ID), false, false);
		this.model.render(matrices, vertexconsumer, light, OverlayTexture.DEFAULT_UV);

		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	@Override
	public Identifier getTexture(MarioFireballProjectileEntity entity) {
		return TEXTURE_ID;
	}
}
