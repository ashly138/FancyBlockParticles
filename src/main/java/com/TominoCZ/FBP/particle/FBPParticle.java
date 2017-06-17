package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticle extends ParticleDigging {
	private final IBlockState sourceState;

	Minecraft mc;

	int vecIndex;

	double scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;

	double angleX, angleY, angleZ, prevAngleX, prevAngleY, prevAngleZ, randomXd, randomYd, randomZd;

	boolean modeDebounce = false, wasFrozen = false, destroyed = false;

	boolean spawned = false, dying = false, killToggle = false;

	Vec2f[] par;

	double endMult = 1;

	float brightness = 1;

	long tick = 2;

	protected FBPParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state, @Nullable EnumFacing facing, float scale) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
		mc = Minecraft.getMinecraft();

		randomXd = FBP.random.nextDouble();
		randomYd = FBP.random.nextDouble();
		randomZd = FBP.random.nextDouble();

		try {
			FBP.setSourcePos.invokeExact((ParticleDigging) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		double step = FBP.rotationMult * getMult();

		if (randomXd <= 0.5)
			angleX += step;
		else
			angleX -= step;

		if (randomYd <= 0.5)
			angleY += step;
		else
			angleY -= step;

		if (randomZd <= 0.5)
			angleZ += step;
		else
			angleZ -= step;

		if (scale > -1)
			particleScale = scale;

		if (scale < -1) {
			if (facing != null) {
				tick = 0;
				if (facing == EnumFacing.UP && FBP.smartBreaking) {
					motionX *= 1.18D;
					motionY *= -0.15D;
					motionZ *= 1.18D;

					double particleSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);

					double x = FBPMathHelper.add(cameraViewDir.xCoord, 0.001D);
					double z = FBPMathHelper.add(cameraViewDir.zCoord, 0.001D);

					motionX = x * particleSpeed;
					motionZ = z * particleSpeed;
				}
			}
		}

		if (modeDebounce = !FBP.randomRotation) {
			angleX = angleZ = angleY = 0;
			calculateYAngle();
		}

		this.sourceState = state;

		Block b = state.getBlock();

		particleGravity = (float) (b.blockParticleGravity * FBP.gravityMult);

		particleScale *= FBP.scaleMult * 2.0F;
		particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);
		this.particleRed = this.particleGreen = this.particleBlue = 0.7F + (0.25F * mc.gameSettings.gammaSetting);

		scaleAlpha = particleScale * 0.75;

		BlockModelShapes blockModelShapes = mc.getBlockRendererDispatcher().getBlockModelShapes();

		// GET THE TOP TEXTURE OF THE BLOCK
		if (!(destroyed = (facing == null))) {
			try {
				List<BakedQuad> quads = blockModelShapes.getModelForState(state).getQuads(state, facing, 0);

				if (quads != null && !quads.isEmpty()) {
					this.particleTexture = quads.get(0).getSprite();

					if (!state.isNormalCube() || (b.equals(Blocks.GRASS) && facing.equals(EnumFacing.UP)))
						multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));
				}
			} catch (Exception e) {
			}
		}

		if (particleTexture == null || particleTexture.getIconName() == "missingno")
			this.setParticleTexture(blockModelShapes.getTexture(state));

		if (!state.isNormalCube())
			multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.925, 1.21);
	}

	public Particle MultiplyVelocity(float multiplier) {
		this.motionX *= (double) multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= (double) multiplier;
		return this;
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.worldObj, p_187154_1_, 0);
		this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		if (!spawned)
			tick++;

		if (!FBP.frozen && FBP.bounceOffWalls && !mc.isGamePaused()) {
			if (!wasFrozen && spawned && (MathHelper.abs((float) motionX) > 0.00001D)) {
				boolean xCollided = (prevPosX == posX);
				boolean zCollided = (prevPosZ == posZ);

				if (xCollided)
					motionX = -prevMotionX;
				if (zCollided)
					motionZ = -prevMotionZ;

				if (!FBP.randomRotation && (xCollided || zCollided))
					calculateYAngle();
			} else
				wasFrozen = false;
		}
		if (FBP.frozen && FBP.bounceOffWalls && !wasFrozen)
			wasFrozen = true;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevAngleX = angleX;
		prevAngleY = angleY;
		prevAngleZ = angleZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!mc.isGamePaused() && (!FBP.frozen || killToggle)) {
			boolean allowedToMove = MathHelper.abs((float) motionX) > 0.00001D;

			if (!killToggle) {
				if (!FBP.randomRotation) {
					if (!modeDebounce) {
						modeDebounce = true;

						// angleX = 0;
						// angleY = 0;
						angleZ = 0;

						calculateYAngle();
					}

					if (allowedToMove) {
						double step = FBP.rotationMult * getMult();

						if (motionX > 0) { // CHANGE ANGLES
							if (motionZ > 0)
								angleX -= step;
							else if (motionZ < 0)
								angleX += step;
						} else if (motionX < 0) {
							if (motionZ < 0)
								angleX += step;
							else if (motionZ > 0) {
								angleX -= step;
							}
						}
					}
				} else {
					if (modeDebounce) {
						modeDebounce = false;
						randomYd = FBP.random.nextDouble();
						randomZd = FBP.random.nextDouble();
					}

					if (allowedToMove) {
						double step = FBP.rotationMult * getMult();

						if (randomXd <= 0.5)
							angleX += step;
						else
							angleX -= step;

						if (randomYd <= 0.5)
							angleY += step;
						else
							angleY -= step;

						if (randomZd <= 0.5)
							angleZ += step;
						else
							angleZ -= step;
					}
				}
			}

			if (!FBP.infiniteDuration)
				particleAge++;

			if (this.particleAge >= this.particleMaxAge || killToggle) {
				if (!dying)
					dying = true;

				if (FBP.randomFadingSpeed)
					particleScale *= 0.75F * endMult;
				else
					particleScale *= 0.75F;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.65F * endMult;
					else
						particleAlpha *= 0.65F;
				}

				if (particleAlpha <= 0.01)
					setExpired();
			}

			if (!killToggle) {
				if (isCollided)
					motionY = -0.08322508594922069D;
				else
					motionY -= 0.04D * (double) particleGravity;

				if (allowedToMove)
					moveEntity(motionX, motionY, motionZ, false); // <<-- THIS
				else
					moveEntity(0, motionY, 0, true);

				// CAN SET
				// MOTION
				// TO ZERO
				if (MathHelper.abs((float) motionX) > 0.00001D) {
					prevMotionX = motionX;
					prevMotionZ = motionZ;
				}

				if (allowedToMove) {
					motionX *= 0.9800000190734863D;
					motionZ *= 0.9800000190734863D;
				}

				motionY *= 0.9800000190734863D;

				// PHYSICS
				if (FBP.entityCollision) {
					List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox());

					for (Entity entityIn : list) {
						if (!entityIn.noClip) {
							double d0 = this.posX - entityIn.posX;
							double d1 = this.posZ - entityIn.posZ;
							double d2 = MathHelper.abs_max(d0, d1);

							if (d2 >= 0.009999999776482582D) {
								d2 = (double) Math.sqrt(d2);
								d0 /= d2;
								d1 /= d2;

								double d3 = 1.0D / d2;

								if (d3 > 1.0D)
									d3 = 1.0D;

								this.motionX += d0 * d3 / 20;
								this.motionZ += d1 * d3 / 20;

								if (!FBP.randomRotation)
									calculateYAngle();
								if (!FBP.frozen)
									this.isCollided = false;
							}
						}
					}
				}

				if (isCollided) {
					if (FBP.rollParticles) {
						motionX *= 0.932515086137662D;
						motionZ *= 0.932515086137662D;
					} else {
						motionX *= 0.654999988079071D;
						motionZ *= 0.654999988079071D;
					}
				}
			}
		}

		if (!spawned && tick >= 2)
			spawned = true;
	}

	public void moveEntity(double x, double y, double z, boolean YOnly) {
		double X = x;
		double Y = y;
		double Z = z;
		double d0 = y;

		if (this.canCollide) {
			List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes(null,
					this.getEntityBoundingBox().addCoord(x, y, z));

			for (AxisAlignedBB aabb : list) {
				y = aabb.calculateYOffset(this.getEntityBoundingBox(), y);

				if (!YOnly) {
					x = aabb.calculateXOffset(this.getEntityBoundingBox(), x);
					z = aabb.calculateZOffset(this.getEntityBoundingBox(), z);
				}
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(YOnly ? 0.0D : x, y, YOnly ? 0.0D : z));
		} else
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));

		this.resetPositionToBB();

		this.isCollided = y != Y && d0 < 0.0D;

		if (!FBP.rollParticles && !FBP.bounceOffWalls) {
			if (x != X)
				motionX *= 0.699999988079071D;
			if (z != Z)
				motionZ *= 0.699999988079071D;
		}
	}

	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		if (FBPKeyBindings.FBPSweep.isKeyDown() && !killToggle)
			killToggle = true;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f4 = particleScale;

		if (particleTexture != null) {
			if (!FBP.cartoonMode) {
				f = particleTexture.getInterpolatedU((double) (particleTextureJitterX / 4 * 16));
				f2 = particleTexture.getInterpolatedV((double) (particleTextureJitterY / 4 * 16));
			}

			f1 = particleTexture.getInterpolatedU((double) ((particleTextureJitterX + 1) / 4 * 16));
			f3 = particleTexture.getInterpolatedV((double) ((particleTextureJitterY + 1) / 4 * 16));
		} else {
			f = ((float) particleTextureIndexX + particleTextureJitterX / 4) / 16;
			f1 = f + 0.015609375F;
			f2 = ((float) particleTextureIndexY + particleTextureJitterY / 4) / 16;
			f3 = f2 + 0.015609375F;
		}

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		par = new Vec2f[] { new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3) };

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if ((dying && FBP.smoothTransitions && !FBP.frozen) || (FBP.frozen && killToggle && FBP.smoothTransitions)) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

		double AngleX = 0, AngleY = 0, AngleZ = 0;

		if (FBP.rotationMult > 0) {
			AngleY = angleY;
			AngleZ = angleZ;

			if (!FBP.randomRotation)
				AngleX = angleX;

			// SMOOTH ROTATION
			if (FBP.smoothTransitions && !FBP.frozen) {
				if (FBP.randomRotation) {
					AngleY = prevAngleY + (angleY - prevAngleY) * partialTicks;

					AngleZ = prevAngleZ + (angleZ - prevAngleZ) * partialTicks;
				} else
					AngleX = prevAngleX + (angleX - prevAngleX) * partialTicks;
			}
		}

		// RENDER
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		worldRendererIn.setTranslation(f5, f6, f7);

		if (spawned)
			putCube(worldRendererIn, f4 / 20, (float) AngleX, (float) AngleY, (float) AngleZ, i >> 16 & 65535,
					i & 65535, particleRed, particleGreen, particleBlue, alpha, FBP.cartoonMode);

		worldRendererIn.setTranslation(0, 0, 0);
	}

	public void putCube(VertexBuffer buff, double scale, double rotX, double rotY, double rotZ, int j, int k, float r,
			float g, float b, float a, boolean cartoon) { // put all vertexes to
															// buffer
															// to form a cube
		brightness = 1;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < FBP.CUBE.length; i += 4) {
			Vec3d v1 = FBP.CUBE[i];
			Vec3d v2 = FBP.CUBE[i + 1];
			Vec3d v3 = FBP.CUBE[i + 2];
			Vec3d v4 = FBP.CUBE[i + 3];
			// Vec3d normal = getNormalD(v1, v2, v3);

			v1 = rotatef(v1, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v2 = rotatef(v2, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v3 = rotatef(v3, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v4 = rotatef(v4, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));

			R = r * brightness;
			G = g * brightness;
			B = b * brightness;

			brightness *= 0.935;

			if (!cartoon) {
				addVt(buff, scale, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v2, par[1].x, par[1].y, j, k, R, G, B, a);
				addVt(buff, scale, v3, par[2].x, par[2].y, j, k, R, G, B, a);
				addVt(buff, scale, v4, par[3].x, par[3].y, j, k, R, G, B, a);
			} else {
				addVt(buff, scale, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v2, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v3, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v4, par[0].x, par[0].y, j, k, R, G, B, a);
			}
		}
	}

	private void addVt(VertexBuffer buff, double scale, Vec3d pos, double u, double v, int j, int k, float r, float g,
			float b, float a) { // add vertex to buffer
		buff.pos(pos.xCoord * scale, pos.yCoord * scale, pos.zCoord * scale).tex(u, v).color(r, g, b, a)
				// .normal((float) normal.xCoord, (float) normal.yCoord, (float)
				// normal.zCoord)
				.lightmap(j, k).endVertex();
	}

	/*
	 * public static Vec3d getNormalD(Vec3d pos0, Vec3d pos1, Vec3d pos2) { //
	 * calculate // normals Vec3d vec3 = pos1.subtractReverse(pos0); Vec3d vec31
	 * = pos1.subtractReverse(pos2); return
	 * vec31.crossProduct(vec3).normalize(); }
	 */

	Vec3d rotatef(Vec3d vec, float AngleX, float AngleY, float AngleZ) {
		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		vec = new Vec3d(vec.xCoord, vec.yCoord * cosAngleX - vec.zCoord * sinAngleX,
				vec.yCoord * sinAngleX + vec.zCoord * cosAngleX);
		vec = new Vec3d(vec.xCoord * cosAngleY + vec.zCoord * sinAngleY, vec.yCoord,
				vec.xCoord * sinAngleY - vec.zCoord * cosAngleY);
		vec = new Vec3d(vec.xCoord * cosAngleZ - vec.yCoord * sinAngleZ,
				vec.xCoord * sinAngleZ + vec.yCoord * cosAngleZ, vec.zCoord);

		return vec;
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return (new FBPParticle(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn,
					Block.getStateById(p_178902_15_[0]), null, -1)).init();
		}
	}

	private void calculateYAngle() {
		double angleSin = Math.toDegrees(Math.asin(motionX / Math.sqrt(motionX * motionX + motionZ * motionZ)));

		if (motionX > 0) {
			if (motionZ > 0)
				angleY = -angleSin;
			else
				angleY = angleSin;
		} else {
			if (motionZ > 0)
				angleY = -angleSin;
			else
				angleY = angleSin;
		}
	}

	double getMult() {
		if (FBP.randomRotation) {
			if (destroyed)
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 200;
			else
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
		} else {
			if (FBP.rollParticles) {
				if (destroyed)
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
				else
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1150;
			} else {
				if (destroyed)
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
				else
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1000;
			}
		}

	}
}