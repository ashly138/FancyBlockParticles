package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleRain extends ParticleDigging {
	private final IBlockState sourceState;

	Minecraft mc;

	double particleHeight;

	double scaleAlpha, prevParticleScale, prevParticleHeight, prevParticleAlpha;

	double angleX, angleY, angleZ, prevAngleX, prevAngleY, prevAngleZ, randomXd, randomYd, randomZd;

	boolean modeDebounce = false;
	
	double scaleMult = 1.45;

	double endMult = 1;

	float AngleY;

	float brightness = 1;

	Vec2f[] par;
	
	public FBPParticleRain(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		try {
			FBP.setSourcePos.invokeExact((ParticleDigging) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		angleY = FBP.random.nextDouble() * 45;

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;

		this.particleGravity = 0.025f;
		
		sourceState = state;

		mc = Minecraft.getMinecraft();

		particleMaxAge = (int) FBP.random.nextDouble(95, 115);
	
		scaleAlpha = particleScale * 0.75;

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.canCollide = true;
		
		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.85, 1);
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {

	}

	public Particle MultiplyVelocity(float multiplier) {
		this.motionX *= (double) multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= (double) multiplier;
		return this;
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, 0);
		this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		prevAngleX = angleX;
		prevAngleY = angleY;
		prevAngleZ = angleZ;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;
		prevParticleHeight = particleHeight;

		if (!mc.isGamePaused()) {
			particleAge++;

			if (this.particleAge < this.particleMaxAge) {
				if (!onGround) {
					if (particleScale < FBP.scaleMult * 1.5f) {
						if (FBP.randomFadingSpeed)
							particleScale += 0.75F * endMult;
						else
							particleScale += 0.75F;

						if (particleScale > 1)
							particleScale = 1;

						particleHeight = particleScale;
					}

					if (particleAlpha < 0.625f) {
						if (FBP.randomFadingSpeed)
							particleAlpha += 0.085F * endMult;
						else
							particleAlpha += 0.085F;

						if (particleAlpha > 0.625f)
							particleAlpha = 0.625f;
					}
				}
			} else
				setExpired();

			motionY -= 0.04D * (double) this.particleGravity;

			moveEntity(motionX, motionY, motionZ);

			motionY *= 1.00025000190734863D;

			if (onGround) {
				motionX = 0;
				motionY = -0.25f;
				motionZ = 0;

				if (particleHeight > 0.075f)
					particleHeight *= 0.85f;

				if (particleScale < FBP.scaleMult * 4.5f) {
					particleScale *= scaleMult;
					
					if (scaleMult > 1)
						scaleMult *= 0.95;
					if (scaleMult < 1)
						scaleMult = 1;
				}
				
				if (particleScale >= FBP.scaleMult * 2) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.75F * endMult;
					else
						particleAlpha *= 0.75F;
				}

				if (particleAlpha <= 0.001f)
					setExpired();
			}
		}
		
		Vec3d rgb = mc.world.getSkyColor(mc.player, mc.getRenderPartialTicks());
		
		this.particleRed = (float) rgb.xCoord;
		this.particleGreen = (float) rgb.yCoord * 1.5f;
		this.particleBlue = (float) rgb.zCoord * 1.75f;
		
		if (this.particleGreen > 1)
			particleGreen = 1;
		if (this.particleBlue > 1)
			particleBlue = 1;
	}
	
	public void moveEntity(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;
		double d0 = y;

		if (this.canCollide) {
			List<AxisAlignedBB> list = this.world.getCollisionBoxes(null,
					this.getBoundingBox().offset(x, y, z));

			for (AxisAlignedBB aabb : list) {
				x = aabb.calculateXOffset(this.getBoundingBox(), x);
				y = aabb.calculateYOffset(this.getBoundingBox(), y);
				z = aabb.calculateZOffset(this.getBoundingBox(), z);
			}

			this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		} else
			this.setBoundingBox(this.getBoundingBox().offset(x, y, z));

		this.resetPositionToBB();

		this.onGround = y != Y && d0 < 0.0D;

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

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

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

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);
		float height = (float) (prevParticleHeight + (particleHeight - prevParticleHeight) * partialTicks);

		
		// RENDER
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		
		par = new Vec2f[] { new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3) };

		worldRendererIn.setTranslation(f5, f6, f7);
		putCube(worldRendererIn, f4 / 20, height / 20, 0, angleY, 0, i >> 16 & 65535,
				i & 65535, particleRed, particleGreen, particleBlue, alpha, FBP.cartoonMode);

		worldRendererIn.setTranslation(0, 0, 0);
	}

	public void putCube(VertexBuffer worldRendererIn, double width, double height, double rotX, double rotY, double rotZ, int j,
			int k, float r, float g, float b, float a, boolean cartoon) {
		brightness = 1;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < FBP.CUBE.length; i += 4) {
			Vec3d v1 = FBP.CUBE[i];
			Vec3d v2 = FBP.CUBE[i + 1];
			Vec3d v3 = FBP.CUBE[i + 2];
			Vec3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef(v1, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v2 = rotatef(v2, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v3 = rotatef(v3, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));
			v4 = rotatef(v4, (float) Math.toRadians(rotX), (float) Math.toRadians(rotY), (float) Math.toRadians(rotZ));

			R = r * brightness;
			G = g * brightness;
			B = b * brightness;

			brightness *= 0.935;

			if (!cartoon) {
				addVt(worldRendererIn, width, height, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v2, par[1].x, par[1].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v3, par[2].x, par[2].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v4, par[3].x, par[3].y, j, k, R, G, B, a);
			} else {
				addVt(worldRendererIn, width, height, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v2, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v3, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(worldRendererIn, width, height, v4, par[0].x, par[0].y, j, k, R, G, B, a);
			}
		}
	}

	private void addVt(VertexBuffer worldRendererIn, double width, double height, Vec3d pos, double u, double v, int j, int k,
			float r, float g, float b, float a) {
		worldRendererIn.pos(pos.xCoord * width, pos.yCoord * height, pos.zCoord * width).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.endVertex();
	}

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

		if (this.world.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.world.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}
}