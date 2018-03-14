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

/**
 *
 * @author LarsIvar
 */
public class Euler {
     private static EulerOrder DEFAULT_ORDER = EulerOrder.XYZ;

    private EulerOrder order;
    private double x, y, z;

    public Euler() {
        this(0, 0, 0, Angle.Representation.RAD);
    }

    public Euler(Vector3d xyz, Angle.Representation repr) {
        this(xyz.x, xyz.y, xyz.z, repr);
    }

    public Euler(double x, double y, double z, Angle.Representation repr) {
        this(x, y, z, repr, DEFAULT_ORDER);
    }

    public Euler(double x, double y, double z, Angle.Representation repr, EulerOrder order) {
        if (repr == Angle.Representation.RAD) {
            this.x = x;
            this.y = y;
            this.z = z;
        } else {
            this.x = Math.toRadians(x);
            this.y = Math.toRadians(y);
            this.z = Math.toRadians(z);
        }
        this.order = order;
    }

    public EulerOrder getOrder() {
        return order;
    }

    public Angle getX() {
        return new Angle(x, Angle.Representation.RAD);
    }

    public Angle getY() {
        return new Angle(y, Angle.Representation.RAD);
    }

    public Angle getZ() {
        return new Angle(z, Angle.Representation.RAD);
    }

    public Euler setX(Angle x) {
        this.x = x.inRadians();
        return this;
    }

    public Euler setY(Angle y) {
        this.y = y.inRadians();
        return this;
    }

    public Euler setZ(Angle z) {
        this.z = z.inRadians();
        return this;
    }

    public Euler set(double x, double y, double z, Angle.Representation repr) {
        return set(x, y, z, repr, DEFAULT_ORDER);
    }

    public Euler set(double x, double y, double z, Angle.Representation repr, EulerOrder order) {
        this.order = order;
        if (repr == Angle.Representation.RAD) {
            this.x = x;
            this.y = y;
            this.z = z;
        } else {
            this.x = Math.toRadians(x);
            this.y = Math.toRadians(y);
            this.z = Math.toRadians(z);
        }

        return this;
    }

    public Euler sub(Euler e) {
        this.x -= e.x;
        this.y -= e.y;
        this.z -= e.z;

        return this;
    }

    public Euler add(Euler e) {
        this.x += e.x;
        this.y += e.y;
        this.z += e.z;

        return this;
    }

    /**
     * Sets the angles of this euler transform from a pure rotation matrix based
     * on default Order.XYZ
     *
     * @param m assumes upper 4x4 of matrix is a pure rotation matrix (i.e.
     * unscaled)
     * @return this
     */
    public Euler setFromRotationMatrix(Matrix4d m) {
        return this.setFromRotationMatrix(m, DEFAULT_ORDER);
    }

    /**
     * Sets the angles of this euler transform from a pure rotation matrix based
     * on the orientation specified by order.
     *
     * @param m assumes upper 4x4 of matrix is a pure rotation matrix (i.e.
     * unscaled)
     * @param order Order of axes, defaults to Order.XYZ
     * @return this
     */
    public Euler setFromRotationMatrix(Matrix4d m, EulerOrder order) {
        double[] te = m.elements;
        double m11 = te[0], m12 = te[4], m13 = te[8];
        double m21 = te[1], m22 = te[5], m23 = te[9];
        double m31 = te[2], m32 = te[6], m33 = te[10];

        if (order == EulerOrder.XYZ) {

            this.y = Math.asin(MathUtil.clamp(m13, -1, 1));

            if (Math.abs(m13) < 0.99999) {

                this.x = Math.atan2(-m23, m33);
                this.z = Math.atan2(-m12, m11);

            } else {

                this.x = Math.atan2(m32, m22);
                this.z = 0;

            }

        } else if (order == EulerOrder.YXZ) {

            this.x = Math.asin(-MathUtil.clamp(m23, - 1, 1));

            if (Math.abs(m23) < 0.99999) {

                this.y = Math.atan2(m13, m33);
                this.z = Math.atan2(m21, m22);

            } else {

                this.y = Math.atan2(-m31, m11);
                this.z = 0;

            }

        } else if (order == EulerOrder.ZXY) {

            this.x = Math.asin(MathUtil.clamp(m32, - 1, 1));

            if (Math.abs(m32) < 0.99999) {

                this.y = Math.atan2(-m31, m33);
                this.z = Math.atan2(-m12, m22);

            } else {

                this.y = 0;
                this.z = Math.atan2(m21, m11);

            }

        } else if (order == EulerOrder.ZYX) {

            this.y = Math.asin(-MathUtil.clamp(m31, - 1, 1));

            if (Math.abs(m31) < 0.99999) {

                this.x = Math.atan2(m32, m33);
                this.z = Math.atan2(m21, m11);

            } else {

                this.x = 0;
                this.z = Math.atan2(-m12, m22);

            }

        } else if (order == EulerOrder.YZX) {

            this.z = Math.asin(MathUtil.clamp(m21, - 1, 1));

            if (Math.abs(m21) < 0.99999) {

                this.x = Math.atan2(-m23, m22);
                this.y = Math.atan2(-m31, m11);

            } else {

                this.x = 0;
                this.y = Math.atan2(m13, m33);

            }

        } else if (order == EulerOrder.XZY) {

            this.z = Math.asin(-MathUtil.clamp(m12, - 1, 1));

            if (Math.abs(m12) < 0.99999) {

                this.x = Math.atan2(m32, m22);
                this.y = Math.atan2(m13, m11);

            } else {

                this.x = Math.atan2(-m23, m33);
                this.y = 0;

            }

        } else {

            throw new IllegalArgumentException("Euler: .setFromRotationMatrix() given unsupported order: " + order);

        }

        this.order = order;

        return this;
    }

    /**
     * Sets the angles of this euler transform from a normalized quaternion
     * using default Order XYZ
     *
     * @param q normalized quaternion
     * @return this
     */
    public Euler setFromQuaternion(Quaternion q) {
        return setFromQuaternion(q.normalize(), DEFAULT_ORDER);
    }

    /**
     * Sets the angles of this euler transform from a normalized quaternion
     * based on the orientation specified by order.
     *
     * @param q normalized quaternion
     * @param order Order of axes, defaults to Order.XYZ
     * @return this
     */
    public Euler setFromQuaternion(Quaternion q, EulerOrder order) {
        double sqx = q.x * q.x;
        double sqy = q.y * q.y;
        double sqz = q.z * q.z;
        double sqw = q.w * q.w;

        if (order == EulerOrder.XYZ) {

            this.x = Math.atan2(2 * (q.x * q.w - q.y * q.z), (sqw - sqx - sqy + sqz));
            this.y = Math.asin(MathUtil.clamp(2 * (q.x * q.z + q.y * q.w), - 1, 1));
            this.z = Math.atan2(2 * (q.z * q.w - q.x * q.y), (sqw + sqx - sqy - sqz));

        } else if (order == EulerOrder.YXZ) {

            this.x = Math.asin(MathUtil.clamp(2 * (q.x * q.w - q.y * q.z), - 1, 1));
            this.y = Math.atan2(2 * (q.x * q.z + q.y * q.w), (sqw - sqx - sqy + sqz));
            this.z = Math.atan2(2 * (q.x * q.y + q.z * q.w), (sqw - sqx + sqy - sqz));

        } else if (order == EulerOrder.ZXY) {

            this.x = Math.asin(MathUtil.clamp(2 * (q.x * q.w + q.y * q.z), - 1, 1));
            this.y = Math.atan2(2 * (q.y * q.w - q.z * q.x), (sqw - sqx - sqy + sqz));
            this.z = Math.atan2(2 * (q.z * q.w - q.x * q.y), (sqw - sqx + sqy - sqz));

        } else if (order == EulerOrder.ZYX) {

            this.x = Math.atan2(2 * (q.x * q.w + q.z * q.y), (sqw - sqx - sqy + sqz));
            this.y = Math.asin(MathUtil.clamp(2 * (q.y * q.w - q.x * q.z), - 1, 1));
            this.z = Math.atan2(2 * (q.x * q.y + q.z * q.w), (sqw + sqx - sqy - sqz));

        } else if (order == EulerOrder.YZX) {

            this.x = Math.atan2(2 * (q.x * q.w - q.z * q.y), (sqw - sqx + sqy - sqz));
            this.y = Math.atan2(2 * (q.y * q.w - q.x * q.z), (sqw + sqx - sqy - sqz));
            this.z = Math.asin(MathUtil.clamp(2 * (q.x * q.y + q.z * q.w), - 1, 1));

        } else if (order == EulerOrder.XZY) {

            this.x = Math.atan2(2 * (q.x * q.w + q.y * q.z), (sqw - sqx + sqy - sqz));
            this.y = Math.atan2(2 * (q.x * q.z + q.y * q.w), (sqw + sqx - sqy - sqz));
            this.z = Math.asin(MathUtil.clamp(2 * (q.z * q.w - q.x * q.y), - 1, 1));

        } else {

            throw new IllegalArgumentException("Euler: .setFromQuaternion() given unsupported order: " + order);

        }

        this.order = order;

        return this;
    }

    /**
     * Returns the euler's XYZ properties as a Vector3 (in radians).
     *
     * @param repr the desired representation (degree or radians)
     * @return a new vector
     */
    public Vector3d toVector3(Angle.Representation repr) {
        if (repr == Angle.Representation.RAD) {
        return new Vector3d(x, y, z);
        } else {
            return new Vector3d(Math.toDegrees(x), Math.toDegrees(y), Math.toDegrees(z));
        }
    }

    public Euler copyFrom(Euler e) {
        this.order = e.order;
        this.x = e.x;
        this.y = e.y;
        this.z = e.z;

        return this;
    }

    public Euler copy() {
        return new Euler(x, y, z, Angle.Representation.RAD, order);
    }

    @Override
    public String toString() {
        return "Euler{" + "order=" + order + ", x=" + getX().inDegrees() + ", y=" + getY().inDegrees() + ", z=" + getZ().inDegrees() + '}';
    }
}
