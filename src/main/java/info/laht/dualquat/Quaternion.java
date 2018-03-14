/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lars Ivar Hatledal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package info.laht.dualquat;

import geometry.primitives.Point;

/**
 *
 * @author LarsIvar
 */
public class Quaternion {

	public double x, y, z, w;

	/**
	 * Construct an identity quaternion
	 */
	public Quaternion() {
		this(0, 0, 0, 1);
	}

	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quaternion(Vector3d v, double w) {
		this(v.x, v.y, v.z, w);
	}

	/**
	 * Sets values of this quaternion.
	 *
	 * @param x x
	 * @param y y
	 * @param z z
	 * @param w w
	 * @return this
	 */
	public Quaternion set(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/**
	 * Sets this quaternion to identity
	 *
	 * @return this
	 */
	public Quaternion identity() {
		return this.set(0, 0, 0, 1);
	}

	/**
	 * Quaternion addition
	 *
	 * @param q other Quaternion
	 * @return result of addition
	 */
	public Quaternion add(Quaternion q) {
		this.x += q.x;
		this.y += q.y;
		this.z += q.z;
		this.w += q.w;

		return this;
	}

	/**
	 * Normalizes this quaternion.
	 *
	 * @return this
	 */
	public Quaternion normalize() {

		double l = this.length();

		if (l == 0) {

			this.x = 0;
			this.y = 0;
			this.z = 0;
			this.w = 1;

		} else {

			l = 1 / l;

			this.x *= l;
			this.y *= l;
			this.z *= l;
			this.w *= l;

		}

		return this;
	}

	/**
	 * Computes length of this quaternion.
	 *
	 * @return length
	 */
	public double length() {
		return Math.sqrt(lengthSquared());
	}

	/**
	 * Calculates the squared length of the quaternion.
	 *
	 * @return squared length
	 */
	public double lengthSquared() {
		return w * w + x * x + y * y + z * z;
	}

	/**
	 * Calculates the dot product between this and the other quaternion
	 *
	 * @param q the other quaternion
	 * @return the dot product
	 */
	public double dot(Quaternion q) {
		return x * q.x + y * q.y + z * q.z + w * q.w;
	}

	/**
	 * Returns the rotational conjugate of this quaternion. The conjugate of a quaternion represents the same rotation
	 * in the opposite direction about the rotational axis
	 *
	 * @return this
	 */
	public Quaternion conjucate() {
		this.x *= -1;
		this.y *= -1;
		this.z *= -1;

		return this;
	}

	/**
	 * Negates this quaternion
	 *
	 * @return this
	 */
	public Quaternion negate() {
		this.x *= -1;
		this.y *= -1;
		this.z *= -1;
		this.w *= -1;

		return this;
	}

	/**
	 * Inverts this quaternion.
	 *
	 * @return this
	 */
	public Quaternion inverse() {
		return this.conjucate().normalize();
	}

	/**
	 * Multiplies this Quaternion with a scalar value
	 *
	 * @param s scalar value
	 * @return the result
	 */
	public Quaternion multiplyScalar(double s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		this.w *= s;

		return this;

	}

	/**
	 * Multiplies this quaternion by q.
	 *
	 * @param q
	 * @return this
	 */
	public Quaternion multiply(Quaternion q) {
		return this.multiplyQuaternions(this, q);
	}

	/**
	 * Sets this quaternion to a x b Adapted from
	 * http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/code/index.htm.
	 *
	 * @param a
	 * @param b
	 * @return this
	 */
	public Quaternion multiplyQuaternions(Quaternion a, Quaternion b) {
		double qax = a.x, qay = a.y, qaz = a.z, qaw = a.w;
		double qbx = b.x, qby = b.y, qbz = b.z, qbw = b.w;

		this.x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby;
		this.y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz;
		this.z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx;
		this.w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz;

		return this;
	}

	/**
	 * Sets this quaternion from rotation specified by axis and angle. Adapted from
	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm. Axis is asumed to
	 * be normalized.
	 *
	 * @param axis axis
	 * @param angle angle
	 * @return this
	 */
	public Quaternion setFromAxisAngle(Vector3d axis, Angle angle) {
		double halfAngle = angle.inRadians() / 2.0;
		double s = Math.sin(halfAngle);

		this.x = axis.x * s;
		this.y = axis.y * s;
		this.z = axis.z * s;
		this.w = Math.cos(halfAngle);

		return this;
	}

	/**
	 * Sets this quaternion from rotation component of m. Adapted from
	 * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm.
	 *
	 * @param m rotation matrix
	 * @return this
	 */
	public Quaternion setFromRotationMatrix(Matrix4d m) {
		double[] te = m.elements;
		double m11 = te[0], m12 = te[4], m13 = te[8],
			m21 = te[1], m22 = te[5], m23 = te[9],
			m31 = te[2], m32 = te[6], m33 = te[10];

		double trace = m11 + m22 + m33;
		double s;

		if (trace > 0) {

			s = 0.5 / Math.sqrt(trace + 1.0);

			this.w = 0.25 / s;
			this.x = (m32 - m23) * s;
			this.y = (m13 - m31) * s;
			this.z = (m21 - m12) * s;

		} else if (m11 > m22 && m11 > m33) {

			s = 2.0 * Math.sqrt(1.0 + m11 - m22 - m33);

			this.w = (m32 - m23) / s;
			this.x = 0.25 * s;
			this.y = (m12 + m21) / s;
			this.z = (m13 + m31) / s;

		} else if (m22 > m33) {

			s = 2.0 * Math.sqrt(1.0 + m22 - m11 - m33);

			this.w = (m13 - m31) / s;
			this.x = (m12 + m21) / s;
			this.y = 0.25 * s;
			this.z = (m23 + m32) / s;

		} else {

			s = 2.0 * Math.sqrt(1.0 + m33 - m11 - m22);

			this.w = (m21 - m12) / s;
			this.x = (m13 + m31) / s;
			this.y = (m23 + m32) / s;
			this.z = 0.25 * s;

		}

		return this;
	}

	/**
	 * Sets this quaternion from rotation specified by Euler angle.
	 *
	 * @param euler
	 * @return this
	 */
	public Quaternion setFromEuler(Euler euler) {
		double c1 = Math.cos(euler.getX().inRadians() / 2);
		double c2 = Math.cos(euler.getY().inRadians() / 2);
		double c3 = Math.cos(euler.getZ().inRadians() / 2);
		double s1 = Math.sin(euler.getX().inRadians() / 2);
		double s2 = Math.sin(euler.getY().inRadians() / 2);
		double s3 = Math.sin(euler.getZ().inRadians() / 2);

		if (euler.getOrder() == EulerOrder.XYZ) {

			this.x = s1 * c2 * c3 + c1 * s2 * s3;
			this.y = c1 * s2 * c3 - s1 * c2 * s3;
			this.z = c1 * c2 * s3 + s1 * s2 * c3;
			this.w = c1 * c2 * c3 - s1 * s2 * s3;

		} else if (euler.getOrder() == EulerOrder.YXZ) {

			this.x = s1 * c2 * c3 + c1 * s2 * s3;
			this.y = c1 * s2 * c3 - s1 * c2 * s3;
			this.z = c1 * c2 * s3 - s1 * s2 * c3;
			this.w = c1 * c2 * c3 + s1 * s2 * s3;

		} else if (euler.getOrder() == EulerOrder.ZXY) {

			this.x = s1 * c2 * c3 - c1 * s2 * s3;
			this.y = c1 * s2 * c3 + s1 * c2 * s3;
			this.z = c1 * c2 * s3 + s1 * s2 * c3;
			this.w = c1 * c2 * c3 - s1 * s2 * s3;

		} else if (euler.getOrder() == EulerOrder.ZYX) {

			this.x = s1 * c2 * c3 - c1 * s2 * s3;
			this.y = c1 * s2 * c3 + s1 * c2 * s3;
			this.z = c1 * c2 * s3 - s1 * s2 * c3;
			this.w = c1 * c2 * c3 + s1 * s2 * s3;

		} else if (euler.getOrder() == EulerOrder.YZX) {

			this.x = s1 * c2 * c3 + c1 * s2 * s3;
			this.y = c1 * s2 * c3 + s1 * c2 * s3;
			this.z = c1 * c2 * s3 - s1 * s2 * c3;
			this.w = c1 * c2 * c3 - s1 * s2 * s3;

		} else if (euler.getOrder() == EulerOrder.XZY) {

			this.x = s1 * c2 * c3 - c1 * s2 * s3;
			this.y = c1 * s2 * c3 - s1 * c2 * s3;
			this.z = c1 * c2 * s3 + s1 * s2 * c3;
			this.w = c1 * c2 * c3 + s1 * s2 * s3;

		}

		return this;

	}

	public Quaternion setFromUnitVectors(Point a, Point b) {
		return setFromUnitVectors(new Vector3d(a.x, a.y, a.z), new Vector3d(b.x, b.y, b.z));
	}

	/**
	 * Sets this quaternion to the rotation required to rotate direction vector vFrom to direction vector vTo. Adapted
	 * from http://lolengine.net/blog/2013/09/18/beautiful-maths-quaternion-from-vectors. vFrom and vTo are assumed to
	 * be normalized.
	 *
	 * @param vFrom
	 * @param vTo
	 * @return this
	 */
	public Quaternion setFromUnitVectors(Vector3d vFrom, Vector3d vTo) {

		Vector3d v1;

		double EPS = 0.000001;
		double r = vFrom.dot(vTo) + 1;

		if (r < EPS) {
			r = 0;
			if (Math.abs(vFrom.x) > Math.abs(vFrom.z)) {
				v1 = new Vector3d(-vFrom.y, vFrom.x, 0);
			} else {
				v1 = new Vector3d(0, -vFrom.z, vFrom.y);
			}
		} else {
			v1 = Vector3d.crossVectors(vFrom, vTo);
		}

		return this.set(v1.x, v1.y, v1.z, r).normalize();

	}

	/**
	 * Handles the spherical linear interpolation between this quaternion's configuration and that of qb. t represents
	 * how close to the current (0) or target (1) rotation the result should be.
	 *
	 * @param qb Target quaternion rotation.
	 * @param t Normalized [0..1] interpolation factor.
	 * @return this
	 */
	public Quaternion slerp(Quaternion qb, double t) {
		if (t == 0) {
			return this;
		} else if (t == 1) {
			return this.copyFrom(qb);
		}

		double x = this.x, y = this.y, z = this.z, w = this.w;

		// http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/
		double cosHalfTheta = w * qb.w + x * qb.x + y * qb.y + z * qb.z;

		if (cosHalfTheta < 0) {

			this.w = -qb.w;
			this.x = -qb.x;
			this.y = -qb.y;
			this.z = -qb.z;

			cosHalfTheta = -cosHalfTheta;

		} else {

			this.copyFrom(qb);

		}

		if (cosHalfTheta >= 1.0) {

			this.w = w;
			this.x = x;
			this.y = y;
			this.z = z;

			return this;

		}

		double halfTheta = Math.acos(cosHalfTheta);
		double sinHalfTheta = Math.sqrt(1.0 - cosHalfTheta * cosHalfTheta);

		if (Math.abs(sinHalfTheta) < 0.001) {

			this.w = 0.5 * (w + this.w);
			this.x = 0.5 * (x + this.x);
			this.y = 0.5 * (y + this.y);
			this.z = 0.5 * (z + this.z);

			return this;

		}

		double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta,
			ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

		this.w = (w * ratioA + this.w * ratioB);
		this.x = (x * ratioA + this.x * ratioB);
		this.y = (y * ratioA + this.y * ratioB);
		this.z = (z * ratioA + this.z * ratioB);

		return this;
	}

	public Quaternion copyFrom(Quaternion q) {
		return this.set(q.x, q.y, q.z, q.w);
	}

	/**
	 * Copy this Quaternion
	 *
	 * @return a new Quaternion instance
	 */
	public Quaternion copy() {
		return new Quaternion(x, y, z, w);
	}

	@Override
	public String toString() {
		return "Quaternion{" + "x=" + x + ", y=" + y + ", z=" + z + ", w=" + w + ", euler=" + new Euler().setFromQuaternion(this) + '}';
	}

	public float[] toFloats() {
		float[] floats = {(float) x, (float) y, (float) z, (float) w};
		return floats;
	}
}
