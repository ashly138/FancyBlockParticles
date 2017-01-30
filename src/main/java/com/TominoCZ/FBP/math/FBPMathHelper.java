package com.TominoCZ.FBP.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FBPMathHelper {
	static ArrayList newVec = new ArrayList();

	static List<double[]> cube;

	static float[] radsXYZ;

	static double sinAngleX, sinAngleY, sinAngleZ, cosAngleX, cosAngleY, cosAngleZ;

	public static ArrayList<double[]> rotateCubeXYZ(double AngleX, double AngleY, double AngleZ, double size) {
		cube = Arrays.asList(
				new double[][] { { (size / 2), -(size / 2), (size / 2) }, { (size / 2), (size / 2), (size / 2) },
						{ -(size / 2), (size / 2), (size / 2) }, { -(size / 2), -(size / 2), (size / 2) },

						{ -(size / 2), -(size / 2), -(size / 2) }, { -(size / 2), (size / 2), -(size / 2) },
						{ (size / 2), (size / 2), -(size / 2) }, { (size / 2), -(size / 2), -(size / 2) },

						{ -(size / 2), -(size / 2), (size / 2) }, { -(size / 2), (size / 2), (size / 2) },
						{ -(size / 2), (size / 2), -(size / 2) }, { -(size / 2), -(size / 2), -(size / 2) },

						{ (size / 2), -(size / 2), -(size / 2) }, { (size / 2), (size / 2), -(size / 2) },
						{ (size / 2), (size / 2), (size / 2) }, { (size / 2), -(size / 2), (size / 2) },

						{ -(size / 2), (size / 2), -(size / 2) }, { -(size / 2), (size / 2), (size / 2) },
						{ (size / 2), (size / 2), (size / 2) }, { (size / 2), (size / 2), -(size / 2) },

						{ -(size / 2), -(size / 2), (size / 2) }, { -(size / 2), -(size / 2), -(size / 2) },
						{ (size / 2), -(size / 2), -(size / 2) }, { (size / 2), -(size / 2), (size / 2) } });

		radsXYZ = new float[] { (float) Math.toRadians(AngleX), (float) Math.toRadians(AngleY),
				(float) Math.toRadians(AngleZ) };

		if (radsXYZ[0] + radsXYZ[1] + radsXYZ[2] != 0) {

			sinAngleX = MathHelper.sin(radsXYZ[0]);
			sinAngleY = MathHelper.sin(radsXYZ[1]);
			sinAngleZ = MathHelper.sin(radsXYZ[2]);

			cosAngleX = MathHelper.cos(radsXYZ[0]);
			cosAngleY = MathHelper.cos(radsXYZ[1]);
			cosAngleZ = MathHelper.cos(radsXYZ[2]);

			newVec.clear();

			cube.forEach(vec -> {
				double[] d = { ((double[]) vec)[0], vec[1] * cosAngleX - vec[2] * sinAngleX,
						vec[1] * sinAngleX + vec[2] * cosAngleX };

				d = new double[] { d[0] * cosAngleY + d[2] * sinAngleY, d[1], d[0] * sinAngleY - d[2] * cosAngleY };

				d = new double[] { d[0] * cosAngleZ - d[1] * sinAngleZ, d[0] * sinAngleZ + d[1] * cosAngleZ, d[2] };

				newVec.add(d);
			});
		}

		return newVec;
	}

	public static double round(double d) {
		int i = (int) Math.round(d * 10);
		return ((double) i) / 10;
	}

	public static double add(double d, double add) {
		return d < 0 ? d - add : d + add;
	}

	public static boolean isInBlock(BlockPos pos) {
		return isInBlock(pos.getX(), pos.getY(), pos.getZ());
	}

	public static boolean isInBlock(int X, int Y, int Z) {

		return false;
	}
}
