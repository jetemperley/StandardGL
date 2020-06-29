import com.jogamp.opengl.math.Matrix4;

public class Mat4Utl {

	private static float[] matrixar = null;
	private static float[] angles;
	private final static Matrix4 identity = new Matrix4();

	static Matrix4 getIdentity(){
		
		return identity;
	}

	static void bubbleSort(float[] arr){

		boolean done = false;
		int n = arr.length;
		float temp;

		while (!done){

			done = true;
			for (int i = 0; i < n-1; i++){
				if (arr[i] > arr[i+1]){
					temp = arr[i + 1];
					arr[i + 1] = arr[i];
					arr[i] = temp;
					done = false;
				}
			}
			n--;

		}
	}

	public static void writeMat4(Matrix4 mat4) {
		if (mat4 != null) {
			matrixar = mat4.getMatrix();
			printMatrix(matrixar);
		}
	}

	public static Matrix4 mult(Matrix4 left, Matrix4 right){
		Matrix4 out = new Matrix4();
		out.multMatrix(left);
		out.multMatrix(right);
		return out;
	}

	static void printMatrix(float[] mat) {
		
		for (int i = 0; i < mat.length; i++) {
			if (i % 4 == 0) {
				System.out.println();
			}
			System.out.print(mat[i] + " ");

		}
		System.out.println();
	}

	public static float[] getRotations(Matrix4 mat) {
		// writeMat4(mat);
		matrixar = mat.getMatrix();
		// System.out.println("l = " + curM4.length);

		angles = new float[3];

		angles[0] = (float) Math.atan2(matrixar[9], matrixar[10]);
		float c2 = (float) Math.sqrt((matrixar[0] * matrixar[0]) + (matrixar[4] * matrixar[4]));
		angles[1] = (float) Math.atan2(-matrixar[8], c2);
		float s1 = (float) Math.sin(angles[0]);
		float c1 = (float) Math.cos(angles[0]);
		angles[2] = (float) Math.atan2((s1 * matrixar[2]) - (c1 * matrixar[1]),
				(c1 * matrixar[5]) - (s1 * matrixar[6]));

		return angles;

	}

	public static Matrix4 getNegitiveMatrix(Matrix4 mat) {
		float[] angles = Mat4Utl.getRotations(mat);
		angles[0] = -angles[0];
		angles[1] = -angles[1];
		angles[2] = -angles[2];
		// System.out.println("angles "+angles[0]+" "+angles[1]+" "+angles[2]);

		Matrix4 negitiveRotation = new Matrix4();
		negitiveRotation.loadIdentity();
		negitiveRotation.rotate(angles[0], 1, 0, 0);
		negitiveRotation.rotate(angles[1], 0, 1, 0);
		negitiveRotation.rotate(angles[2], 0, 0, 1);
		return negitiveRotation;
	}

	// takes a column major 4x4 matrix
	public static float[] matrixToQuat(Matrix4 matrix) {

		float w, x, y, z;
		float[] mat = matrix.getMatrix();

		float diagonal = mat[0] + mat[5] + mat[10];
		if (diagonal > 0) {
			float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
			w = w4 / 4f;
			x = (mat[9] - mat[6]) / w4;
			y = (mat[2] - mat[8]) / w4;
			z = (mat[4] - mat[1]) / w4;
		} else if ((mat[0] > mat[5]) && (mat[0] > mat[10])) {
			float x4 = (float) (Math.sqrt(1f + mat[0] - mat[5] - mat[10]) * 2f);
			w = (mat[9] - mat[6]) / x4;
			x = x4 / 4f;
			y = (mat[1] + mat[4]) / x4;
			z = (mat[2] + mat[8]) / x4;
		} else if (mat[5] > mat[10]) {
			float y4 = (float) (Math.sqrt(1f + mat[5] - mat[0] - mat[10]) * 2f);
			w = (mat[2] - mat[8]) / y4;
			x = (mat[1] + mat[4]) / y4;
			y = y4 / 4f;
			z = (mat[6] + mat[9]) / y4;
		} else {
			float z4 = (float) (Math.sqrt(1f + mat[10] - mat[0] - mat[5]) * 2f);
			w = (mat[4] - mat[1]) / z4;
			x = (mat[2] + mat[8]) / z4;
			y = (mat[6] + mat[9]) / z4;
			z = z4 / 4f;
		}
		return new float[] { x, y, z, w };
	}

	static Matrix4 interpolateAngles(Matrix4 a, float blend, Matrix4 b) {

		// matricies to quats
		float[] qa = Mat4Utl.matrixToQuat(a);
		float[] qb = Mat4Utl.matrixToQuat(b);

		// interpolate
		float[] out = Mat4Utl.interpolateQuats(qa, blend, qb);

		// back to matrix and return
		return Mat4Utl.quatToMatrix(out);
	}

	static float[] interpolateQuats(float[] a, float blend, float[] b) {
		float[] result = { 0, 0, 0, 1 };
		float dot = a[3] * b[3] + a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
		float blendI = 1f - blend;
		if (dot < 0) {
			result[3] = blend * a[3] + blendI * -b[3];
			result[0] = blend * a[0] + blendI * -b[0];
			result[1] = blend * a[1] + blendI * -b[1];
			result[2] = blend * a[2] + blendI * -b[2];
		} else {
			result[3] = blend * a[3] + blendI * b[3];
			result[0] = blend * a[0] + blendI * b[0];
			result[1] = blend * a[1] + blendI * b[1];
			result[2] = blend * a[2] + blendI * b[2];
		}
		Mat4Utl.normalizeQuat(result);
		return result;
	}

	static float[] interpolatePos(float[] a, float blend, float[] b) {

		float[] out = new float[3];

		out[0] = a[0] + (b[0] - a[0]) * blend;
		out[1] = a[1] + (b[1] - a[1]) * blend;
		out[2] = a[2] + (b[2] - a[2]) * blend;

		return out;
	}

	static Matrix4 interpolatePos(Matrix4 a, float blend, Matrix4 b) {
		float[] mata = a.getMatrix();
		float[] matb = b.getMatrix();
		float[] out = Mat4Utl.interpolatePos(new float[] { mata[12], mata[13], mata[14] }, blend,
				new float[] { matb[12], matb[13], matb[14] });
		Matrix4 mat = new Matrix4();
		mat.translate(out[0], out[1], out[2]);
		return mat;
	}

	static Matrix4 interpolate(Matrix4 a, float blend, Matrix4 b) {
		float[] mata = a.getMatrix();
		float[] matb = b.getMatrix();
		Vec3 apos = new Vec3(mata[12], mata[13], mata[14]);
		Vec3 bpos = new Vec3(matb[12], matb[13], matb[14]);
		Vec3 outpos = Mat4Utl.interpolatePos(apos, blend, bpos);

		float[] aquat = matrixToQuat(a);
		float[] bquat = matrixToQuat(b);
		float[] outquat = Mat4Utl.interpolateQuats(aquat, blend, bquat);

		Matrix4 out = Mat4Utl.quatToMatrix(outquat);
		out.translate(outpos.x, outpos.y, outpos.z);
		return out;
	}

	static Vec3 interpolatePos(Vec3 a, float blend, Vec3 b) {
		Vec3 out = new Vec3();
		out.x = a.x + (b.x - a.x) * blend;
		out.y = a.y + (b.y - a.y) * blend;
		out.z = a.z + (b.z - a.z) * blend;
		return out;
	}

	static void normalizeQuat(float[] f) {
		float mag = (float) Math.sqrt(f[3] * f[3] + f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
		f[3] /= mag;
		f[0] /= mag;
		f[1] /= mag;
		f[2] /= mag;
	}

	// takes a quaternion, returns the rotation matrix
	static Matrix4 quatToMatrix(float[] q) {
		float[] mat = new float[16];
		final float xy = q[0] * q[1];
		final float xz = q[0] * q[2];
		final float xw = q[0] * q[3];
		final float yz = q[1] * q[2];
		final float yw = q[1] * q[3];
		final float zw = q[2] * q[3];
		final float xSquared = q[0] * q[0];
		final float ySquared = q[1] * q[1];
		final float zSquared = q[2] * q[2];
		mat[0] = 1 - 2 * (ySquared + zSquared);
		mat[4] = 2 * (xy - zw);
		mat[8] = 2 * (xz + yw);
		mat[12] = 0;
		mat[1] = 2 * (xy + zw);
		mat[5] = 1 - 2 * (xSquared + zSquared);
		mat[9] = 2 * (yz - xw);
		mat[13] = 0;
		mat[2] = 2 * (xz - yw);
		mat[6] = 2 * (yz + xw);
		mat[10] = 1 - 2 * (xSquared + ySquared);
		mat[14] = 0;
		mat[3] = 0;
		mat[7] = 0;
		mat[11] = 0;
		mat[15] = 1;
		Matrix4 matrix = new Matrix4();
		matrix.loadIdentity();
		matrix.multMatrix(mat);
		return matrix;
	}

}