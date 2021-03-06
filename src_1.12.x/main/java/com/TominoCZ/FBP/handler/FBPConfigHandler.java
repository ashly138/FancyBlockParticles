package com.TominoCZ.FBP.handler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import com.TominoCZ.FBP.FBP;

import net.minecraft.util.math.MathHelper;
import scala.reflect.io.Path;

public class FBPConfigHandler {
	static FileInputStream fis;
	static InputStreamReader isr;
	static BufferedReader br;

	public static void init() {
		try {
			defaults(false);

			if (!FBP.config.exists()) {
				if (!Path.apply(FBP.config.getParent()).exists())
					Path.apply(FBP.config.getParent()).createDirectory(true, false);

				FBP.config.createNewFile();

				write();
			}

			if (!FBP.animExceptionsFile.exists()) {
				if (!Path.apply(FBP.animExceptionsFile.getParent()).exists())
					Path.apply(FBP.animExceptionsFile.getParent()).createDirectory(true, false);

				FBP.animExceptionsFile.createNewFile();
			}
			if (!FBP.particleExceptionsFile.exists()) {
				if (!Path.apply(FBP.particleExceptionsFile.getParent()).exists())
					Path.apply(FBP.particleExceptionsFile.getParent()).createDirectory(true, false);

				FBP.particleExceptionsFile.createNewFile();
			}

			read();
			readAnimExceptions();
			readParticleExceptions();

			write();
			writeAnimExceptions();
			writeParticleExceptions();

			closeStreams();
		} catch (IOException e) {
			closeStreams();

			write();
		}
	}

	public static void write() {
		try {
			check();

			PrintWriter writer = new PrintWriter(FBP.config.getPath(), "UTF-8");
			writer.println("enabled=" + FBP.enabled);
			writer.println("weatherParticleDensity=" + FBP.weatherParticleDensity);
			writer.println("particlesPerAxis=" + FBP.particlesPerAxis);
			writer.println("fancyFlame=" + FBP.fancyFlame);
			writer.println("fancySmoke=" + FBP.fancySmoke);
			writer.println("fancyRain=" + FBP.fancyRain);
			writer.println("fancySnow=" + FBP.fancySnow);
			writer.println("spawnPlaceParticles=" + FBP.spawnPlaceParticles);
			writer.println("fancyPlaceAnim=" + FBP.fancyPlaceAnim);
			writer.println("animSmoothLighting=" + FBP.animSmoothLighting);
			writer.println("smartBreaking=" + FBP.smartBreaking);
			writer.println("lowTraction=" + FBP.lowTraction);
			writer.println("bounceOffWalls=" + FBP.bounceOffWalls);
			writer.println("showInMillis=" + FBP.showInMillis);
			writer.println("randomRotation=" + FBP.randomRotation);
			writer.println("cartoonMode=" + FBP.cartoonMode);
			writer.println("entityCollision=" + FBP.entityCollision);
			writer.println("smoothTransitions=" + FBP.smoothTransitions);
			writer.println("randomFadingSpeed=" + FBP.randomFadingSpeed);
			writer.println("spawnRedstoneBlockParticles=" + FBP.spawnRedstoneBlockParticles);
			writer.println("spawnWhileFrozen=" + FBP.spawnWhileFrozen);
			writer.println("infiniteDuration=" + FBP.infiniteDuration);
			writer.println("minAge=" + FBP.minAge);
			writer.println("maxAge=" + FBP.maxAge);
			writer.println("scaleMult=" + FBP.scaleMult);
			writer.println("gravityMult=" + FBP.gravityMult);
			writer.print("rotationMult=" + FBP.rotationMult);
			writer.close();
		} catch (Exception e) {
			closeStreams();

			if (!FBP.config.exists()) {
				if (!Path.apply(FBP.config.getParent()).exists())
					Path.apply(FBP.config.getParent()).createDirectory(true, false);

				try {
					FBP.config.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			write();
		}
	}

	public static void writeAnimExceptions() {
		try {
			PrintWriter writer = new PrintWriter(FBP.animExceptionsFile.getPath(), "UTF-8");

			for (String ex : FBP.INSTANCE.blockAnimExceptions)
				writer.println(ex);

			writer.close();
		} catch (Exception e) {
			closeStreams();

			if (!FBP.animExceptionsFile.exists()) {
				if (!Path.apply(FBP.animExceptionsFile.getParent()).exists())
					Path.apply(FBP.animExceptionsFile.getParent()).createDirectory(true, false);

				try {
					FBP.animExceptionsFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static void writeParticleExceptions() {
		try {
			PrintWriter writer = new PrintWriter(FBP.particleExceptionsFile.getPath(), "UTF-8");

			for (String ex : FBP.INSTANCE.blockParticleExceptions)
				writer.println(ex);

			writer.close();
		} catch (Exception e) {
			closeStreams();

			if (!FBP.particleExceptionsFile.exists()) {
				if (!Path.apply(FBP.particleExceptionsFile.getParent()).exists())
					Path.apply(FBP.particleExceptionsFile.getParent()).createDirectory(true, false);

				try {
					FBP.particleExceptionsFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	static void read() {
		try {
			fis = new FileInputStream(FBP.config);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			while ((line = br.readLine()) != null) {
				line = line.replaceAll(" ", "");

				if (line.contains("enabled="))
					FBP.enabled = Boolean.valueOf(line.replace("enabled=", ""));
				else if (line.contains("weatherParticleDensity="))
					FBP.weatherParticleDensity = Double.valueOf(line.replace("weatherParticleDensity=", ""));
				else if (line.contains("particlesPerAxis="))
					FBP.particlesPerAxis = Integer.valueOf(line.replace("particlesPerAxis=", ""));
				else if (line.contains("fancyFlame="))
					FBP.fancyFlame = Boolean.valueOf(line.replace("fancyFlame=", ""));
				else if (line.contains("fancySmoke="))
					FBP.fancySmoke = Boolean.valueOf(line.replace("fancySmoke=", ""));
				else if (line.contains("fancyRain="))
					FBP.fancyRain = Boolean.valueOf(line.replace("fancyRain=", ""));
				else if (line.contains("fancySnow="))
					FBP.fancySnow = Boolean.valueOf(line.replace("fancySnow=", ""));
				else if (line.contains("spawnPlaceParticles="))
					FBP.spawnPlaceParticles = Boolean.valueOf(line.replace("spawnPlaceParticles=", ""));
				else if (line.contains("fancyPlaceAnim="))
					FBP.fancyPlaceAnim = Boolean.valueOf(line.replace("fancyPlaceAnim=", ""));
				else if (line.contains("animSmoothLighting="))
					FBP.animSmoothLighting = Boolean.valueOf(line.replace("animSmoothLighting=", ""));
				else if (line.contains("smartBreaking="))
					FBP.smartBreaking = Boolean.valueOf(line.replace("smartBreaking=", ""));
				else if (line.contains("lowTraction="))
					FBP.lowTraction = Boolean.valueOf(line.replace("lowTraction=", ""));
				else if (line.contains("bounceOffWalls="))
					FBP.bounceOffWalls = Boolean.valueOf(line.replace("bounceOffWalls=", ""));
				else if (line.contains("showInMillis="))
					FBP.showInMillis = Boolean.valueOf(line.replace("showInMillis=", ""));
				else if (line.contains("randomRotation="))
					FBP.randomRotation = Boolean.valueOf(line.replace("randomRotation=", ""));
				else if (line.contains("cartoonMode="))
					FBP.cartoonMode = Boolean.valueOf(line.replace("cartoonMode=", ""));
				else if (line.contains("entityCollision="))
					FBP.entityCollision = Boolean.valueOf(line.replace("entityCollision=", ""));
				else if (line.contains("randomFadingSpeed="))
					FBP.randomFadingSpeed = Boolean.valueOf(line.replace("randomFadingSpeed=", ""));
				else if (line.contains("smoothTransitions="))
					FBP.smoothTransitions = Boolean.valueOf(line.replace("smoothTransitions=", ""));
				else if (line.contains("spawnWhileFrozen="))
					FBP.spawnWhileFrozen = Boolean.valueOf(line.replace("spawnWhileFrozen=", ""));
				else if (line.contains("spawnRedstoneBlockParticles="))
					FBP.spawnRedstoneBlockParticles = Boolean.valueOf(line.replace("spawnRedstoneBlockParticles=", ""));
				else if (line.contains("infiniteDuration="))
					FBP.infiniteDuration = Boolean.valueOf(line.replace("infiniteDuration=", ""));
				else if (line.contains("minAge="))
					FBP.minAge = Integer.valueOf(line.replace("minAge=", ""));
				else if (line.contains("maxAge="))
					FBP.maxAge = Integer.valueOf(line.replace("maxAge=", ""));
				else if (line.contains("scaleMult="))
					FBP.scaleMult = Double.valueOf(line.replace("scaleMult=", ""));
				else if (line.contains("gravityMult="))
					FBP.gravityMult = Double.valueOf(line.replace("gravityMult=", ""));
				else if (line.contains("rotationMult="))
					FBP.rotationMult = Double.valueOf(line.replace("rotationMult=", ""));
			}

			closeStreams();

			check();
		} catch (Exception e) {
			closeStreams();

			check();

			write();
		}
	}

	static void readAnimExceptions() {
		try {
			fis = new FileInputStream(FBP.animExceptionsFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			FBP.INSTANCE.resetExceptions(false);

			while ((line = br.readLine()) != null && !(line = line.replaceAll(" ", "").toLowerCase()).equals(""))
				FBP.INSTANCE.addException(line, false);
		} catch (Exception e) {

		}

		closeStreams();
	}

	static void readParticleExceptions() {
		try {
			fis = new FileInputStream(FBP.particleExceptionsFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			FBP.INSTANCE.resetExceptions(true);

			while ((line = br.readLine()) != null && !(line = line.replaceAll(" ", "").toLowerCase()).equals(""))
				FBP.INSTANCE.addException(line, true);
		} catch (Exception e) {

		}

		closeStreams();
	}

	static void closeStreams() {
		try {
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void defaults(boolean write) {
		FBP.minAge = 10;
		FBP.maxAge = 45;
		FBP.scaleMult = 1.0;
		FBP.gravityMult = 1.0;
		FBP.rotationMult = 1.0;
		FBP.particlesPerAxis = 4;
		FBP.weatherParticleDensity = 1.0;
		FBP.lowTraction = false;
		FBP.bounceOffWalls = true;
		FBP.randomRotation = true;
		FBP.cartoonMode = false;
		FBP.entityCollision = false;
		FBP.smoothTransitions = true;
		FBP.randomFadingSpeed = true;
		FBP.spawnRedstoneBlockParticles = false;
		FBP.infiniteDuration = false;
		FBP.spawnWhileFrozen = true;
		FBP.smartBreaking = true;
		FBP.fancyPlaceAnim = false;
		FBP.animSmoothLighting = false;
		FBP.spawnPlaceParticles = true;
		FBP.fancyRain = true;
		FBP.fancySnow = true;
		FBP.fancySmoke = true;
		FBP.fancyFlame = true;

		if (write)
			write();
	}

	public static void check() {
		FBP.maxAge = MathHelper.clamp(FBP.maxAge, 10, 100);
		FBP.minAge = MathHelper.clamp(FBP.minAge, 10, FBP.maxAge);

		FBP.particlesPerAxis = MathHelper.clamp(FBP.particlesPerAxis, 2, 5);

		FBP.scaleMult = MathHelper.clamp(FBP.scaleMult, 0.75D, 1.25D);

		FBP.gravityMult = MathHelper.clamp(FBP.gravityMult, 0.5D, 2.0D);

		FBP.rotationMult = MathHelper.clamp(FBP.rotationMult, 0.0D, 1.5D);

		FBP.weatherParticleDensity = MathHelper.clamp(FBP.weatherParticleDensity, 0.75D, 5.0D);
	}
}
