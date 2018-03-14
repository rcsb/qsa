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

import java.util.logging.Level;
import java.util.logging.Logger;

public class Matrix4d {

    private static final Logger LOG = Logger.getLogger(Matrix4d.class.getName());

    public double[] elements;

    /**
     * Constructs an identity matrix
     */
    public Matrix4d() {
        this.elements = new double[16];
        setIdentity();
    }

    public Matrix4d(double diag) {
        this();
        setDiagonal(diag);
    }

    /**
     * Sets this matrix to identity
     *
     * @return this
     */
    public final Matrix4d setIdentity() {
        return setDiagonal(1);
    }

    public final Matrix4d setDiagonal(double diag) {
        return this.set(new double[]{
            diag, 0, 0, 0,
            0, diag, 0, 0,
            0, 0, diag, 0,
            0, 0, 0, diag
        });
    }

    public final void clear() {
        this.elements = new double[16];
    }

    public double get(int row, int col) {
        return elements[col * 4 + row];
    }

    /**
     * Sets all fields of this matrix to the supplied row-major values n11..n44.
     *
     * @param elements n11 .. n44
     * @return this
     */
    public Matrix4d set(double... elements) {
        if (elements.length != 16) {
            throw new IllegalArgumentException("This matrix type takes exactly 16 (4x4) arguments");
        }
        double[] te = this.elements;

        te[0] = elements[0];
        te[4] = elements[1];
        te[8] = elements[2];
        te[12] = elements[3];
        te[1] = elements[4];
        te[5] = elements[5];
        te[9] = elements[6];
        te[13] = elements[7];
        te[2] = elements[8];
        te[6] = elements[9];
        te[10] = elements[10];
        te[14] = elements[11];
        te[3] = elements[12];
        te[7] = elements[13];
        te[11] = elements[14];
        te[15] = elements[15];

        return this;
    }

    public Matrix4d set(int row, int col, double value) {
        if (row < 0 || row > 3) {
            throw new IndexOutOfBoundsException("Row not between 0 and 3");
        }
        if (col < 0 || col > 3) {
            throw new IndexOutOfBoundsException("Column not between 0 and 3");
        }

        this.elements[col * 4 + row] = value;

        return this;
    }

    public Matrix4d makeLookAt(Vector3d eye, Vector3d target, Vector3d up) {

        setIdentity();

        Vector3d z = eye.sub(target).normalize();

        if (z.length() == 0) {
            z = new Vector3d(z.x, z.y, 1);
        }

        Vector3d x = up.cross(z).normalize();

        if (x.length() == 0) {
            z = z.add(Vector3d.X.multiply(0.0001));
            x = up.cross(z).normalize();
        }

        Vector3d y = z.cross(x);

        elements[0] = x.x;
        elements[4] = y.x;
        elements[8] = z.x;
        elements[1] = x.y;
        elements[5] = y.y;
        elements[9] = z.y;
        elements[2] = x.z;
        elements[6] = y.z;
        elements[10] = z.z;

        return this;

    }

    public Matrix4d makeFrustum(double left, double right, double bottom, double top, double near, double far) {

        double[] te = this.elements;
        double x = 2 * near / (right - left);
        double y = 2 * near / (top - bottom);

        double a = (right + left) / (right - left);
        double b = (top + bottom) / (top - bottom);
        double c = -(far + near) / (far - near);
        double d = - 2 * far * near / (far - near);

        te[0] = x;
        te[4] = 0;
        te[8] = a;
        te[12] = 0;
        te[1] = 0;
        te[5] = y;
        te[9] = b;
        te[13] = 0;
        te[2] = 0;
        te[6] = 0;
        te[10] = c;
        te[14] = d;
        te[3] = 0;
        te[7] = 0;
        te[11] = - 1;
        te[15] = 0;

        return this;

    }

    public Matrix4d makePerspective(double fov, double aspect, double near, double far) {
        double ymax = near * Math.tan(Math.toRadians(fov * 0.5));
        double ymin = -ymax;
        double xmin = ymin * aspect;
        double xmax = ymax * aspect;

        return this.makeFrustum(xmin, xmax, ymin, ymax, near, far);
    }

    /**
     * Multiplies this matrix by m.
     *
     * @param m matrix to multiply with
     * @return this
     */
    public Matrix4d multiply(Matrix4d m) {
        return this.multiplyMatrices(this, m);
    }

    public Matrix4d multiply(DualQuaternion dq) {
        return this.multiplyMatrices(this, dq.getMatrixRepresentation());
    }

    /**
     * Sets this matrix to a x b.
     *
     * @param a first matrix
     * @param b second matrix
     * @return this
     */
    public Matrix4d multiplyMatrices(Matrix4d a, Matrix4d b) {
        double[] ae = a.elements;
        double[] be = b.elements;

        double a11 = ae[0], a12 = ae[4], a13 = ae[8], a14 = ae[12];
        double a21 = ae[1], a22 = ae[5], a23 = ae[9], a24 = ae[13];
        double a31 = ae[2], a32 = ae[6], a33 = ae[10], a34 = ae[14];
        double a41 = ae[3], a42 = ae[7], a43 = ae[11], a44 = ae[15];

        double b11 = be[0], b12 = be[4], b13 = be[8], b14 = be[12];
        double b21 = be[1], b22 = be[5], b23 = be[9], b24 = be[13];
        double b31 = be[2], b32 = be[6], b33 = be[10], b34 = be[14];
        double b41 = be[3], b42 = be[7], b43 = be[11], b44 = be[15];

        this.elements[0] = a11 * b11 + a12 * b21 + a13 * b31 + a14 * b41;
        this.elements[4] = a11 * b12 + a12 * b22 + a13 * b32 + a14 * b42;
        this.elements[8] = a11 * b13 + a12 * b23 + a13 * b33 + a14 * b43;
        this.elements[12] = a11 * b14 + a12 * b24 + a13 * b34 + a14 * b44;

        this.elements[1] = a21 * b11 + a22 * b21 + a23 * b31 + a24 * b41;
        this.elements[5] = a21 * b12 + a22 * b22 + a23 * b32 + a24 * b42;
        this.elements[9] = a21 * b13 + a22 * b23 + a23 * b33 + a24 * b43;
        this.elements[13] = a21 * b14 + a22 * b24 + a23 * b34 + a24 * b44;

        this.elements[2] = a31 * b11 + a32 * b21 + a33 * b31 + a34 * b41;
        this.elements[6] = a31 * b12 + a32 * b22 + a33 * b32 + a34 * b42;
        this.elements[10] = a31 * b13 + a32 * b23 + a33 * b33 + a34 * b43;
        this.elements[14] = a31 * b14 + a32 * b24 + a33 * b34 + a34 * b44;

        this.elements[3] = a41 * b11 + a42 * b21 + a43 * b31 + a44 * b41;
        this.elements[7] = a41 * b12 + a42 * b22 + a43 * b32 + a44 * b42;
        this.elements[11] = a41 * b13 + a42 * b23 + a43 * b33 + a44 * b43;
        this.elements[15] = a41 * b14 + a42 * b24 + a43 * b34 + a44 * b44;

        return this;
    }

    /**
     * Multiplies this matrix by s.
     *
     * @param s scalar to multiply with
     * @return this
     */
    public Matrix4d multiplyScalar(double s) {
        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] *= s;
        }

        return this;
    }

    /**
     * Computes determinant of this matrix. Based on
     * http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm
     *
     * @return the matrix's determinant
     */
    public double determinant() {

        double n11 = this.elements[0], n12 = this.elements[4], n13 = this.elements[8], n14 = this.elements[12];
        double n21 = this.elements[1], n22 = this.elements[5], n23 = this.elements[9], n24 = this.elements[13];
        double n31 = this.elements[2], n32 = this.elements[6], n33 = this.elements[10], n34 = this.elements[14];
        double n41 = this.elements[3], n42 = this.elements[7], n43 = this.elements[11], n44 = this.elements[15];

        return (n41 * (+n14 * n23 * n32
                - n13 * n24 * n32
                - n14 * n22 * n33
                + n12 * n24 * n33
                + n13 * n22 * n34
                - n12 * n23 * n34)
                + n42 * (+n11 * n23 * n34
                - n11 * n24 * n33
                + n14 * n21 * n33
                - n13 * n21 * n34
                + n13 * n24 * n31
                - n14 * n23 * n31)
                + n43 * (+n11 * n24 * n32
                - n11 * n22 * n34
                - n14 * n21 * n32
                + n12 * n21 * n34
                + n14 * n22 * n31
                - n12 * n24 * n31)
                + n44 * (-n13 * n22 * n31
                - n11 * n23 * n32
                + n11 * n22 * n33
                + n13 * n21 * n32
                - n12 * n21 * n33
                + n12 * n23 * n31));
    }

    /**
     * Transposes this matrix
     *
     * @return this
     */
    public Matrix4d transpose() {
        double[] te = this.elements;
        double tmp;

        tmp = te[1];
        te[1] = te[4];
        te[4] = tmp;
        tmp = te[2];
        te[2] = te[8];
        te[8] = tmp;
        tmp = te[6];
        te[6] = te[9];
        te[9] = tmp;

        tmp = te[3];
        te[3] = te[12];
        te[12] = tmp;
        tmp = te[7];
        te[7] = te[13];
        te[13] = tmp;
        tmp = te[11];
        te[11] = te[14];
        te[14] = tmp;

        return this;
    }

    /**
     * Get the translational (xyz) components of the matrix
     *
     * @return a new vector with the matrix's xyz components
     */
    public Vector3d getPosition() {
        return new Vector3d(this.elements[12], this.elements[13], this.elements[14]);
    }

    /**
     * Sets the translational components
     *
     * @param x
     * @param y
     * @param z
     * @return this
     */
    public Matrix4d setTranslate(double x, double y, double z) {
        this.elements[12] = x;
        this.elements[13] = y;
        this.elements[14] = z;

        return this;
    }

    public Matrix4d setPosition(Vector3d pos) {
        return setTranslate(pos.x, pos.y, pos.z);
    }

    public Matrix4d invert() {
        double[] te = this.elements;

        double n11 = this.elements[0], n12 = this.elements[4], n13 = this.elements[8], n14 = this.elements[12];
        double n21 = this.elements[1], n22 = this.elements[5], n23 = this.elements[9], n24 = this.elements[13];
        double n31 = this.elements[2], n32 = this.elements[6], n33 = this.elements[10], n34 = this.elements[14];
        double n41 = this.elements[3], n42 = this.elements[7], n43 = this.elements[11], n44 = this.elements[15];

        te[0] = n23 * n34 * n42 - n24 * n33 * n42 + n24 * n32 * n43 - n22 * n34 * n43 - n23 * n32 * n44 + n22 * n33 * n44;
        te[4] = n14 * n33 * n42 - n13 * n34 * n42 - n14 * n32 * n43 + n12 * n34 * n43 + n13 * n32 * n44 - n12 * n33 * n44;
        te[8] = n13 * n24 * n42 - n14 * n23 * n42 + n14 * n22 * n43 - n12 * n24 * n43 - n13 * n22 * n44 + n12 * n23 * n44;
        te[12] = n14 * n23 * n32 - n13 * n24 * n32 - n14 * n22 * n33 + n12 * n24 * n33 + n13 * n22 * n34 - n12 * n23 * n34;
        te[1] = n24 * n33 * n41 - n23 * n34 * n41 - n24 * n31 * n43 + n21 * n34 * n43 + n23 * n31 * n44 - n21 * n33 * n44;
        te[5] = n13 * n34 * n41 - n14 * n33 * n41 + n14 * n31 * n43 - n11 * n34 * n43 - n13 * n31 * n44 + n11 * n33 * n44;
        te[9] = n14 * n23 * n41 - n13 * n24 * n41 - n14 * n21 * n43 + n11 * n24 * n43 + n13 * n21 * n44 - n11 * n23 * n44;
        te[13] = n13 * n24 * n31 - n14 * n23 * n31 + n14 * n21 * n33 - n11 * n24 * n33 - n13 * n21 * n34 + n11 * n23 * n34;
        te[2] = n22 * n34 * n41 - n24 * n32 * n41 + n24 * n31 * n42 - n21 * n34 * n42 - n22 * n31 * n44 + n21 * n32 * n44;
        te[6] = n14 * n32 * n41 - n12 * n34 * n41 - n14 * n31 * n42 + n11 * n34 * n42 + n12 * n31 * n44 - n11 * n32 * n44;
        te[10] = n12 * n24 * n41 - n14 * n22 * n41 + n14 * n21 * n42 - n11 * n24 * n42 - n12 * n21 * n44 + n11 * n22 * n44;
        te[14] = n14 * n22 * n31 - n12 * n24 * n31 - n14 * n21 * n32 + n11 * n24 * n32 + n12 * n21 * n34 - n11 * n22 * n34;
        te[3] = n23 * n32 * n41 - n22 * n33 * n41 - n23 * n31 * n42 + n21 * n33 * n42 + n22 * n31 * n43 - n21 * n32 * n43;
        te[7] = n12 * n33 * n41 - n13 * n32 * n41 + n13 * n31 * n42 - n11 * n33 * n42 - n12 * n31 * n43 + n11 * n32 * n43;
        te[11] = n13 * n22 * n41 - n12 * n23 * n41 - n13 * n21 * n42 + n11 * n23 * n42 + n12 * n21 * n43 - n11 * n22 * n43;
        te[15] = n12 * n23 * n31 - n13 * n22 * n31 + n13 * n21 * n32 - n11 * n23 * n32 - n12 * n21 * n33 + n11 * n22 * n33;

        double det = n11 * te[0] + n21 * te[4] + n31 * te[8] + n41 * te[12];

        if (det == 0) {
            LOG.log(Level.WARNING, "Matrix is not invertable. Determinant is 0");
            return this.setIdentity();
        }

        return this.multiplyScalar(1 / det);

    }

    public Matrix4d makeTranslation(double x, double y, double z) {
        return this.set(
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1
        );
    }

    public Matrix4d makeTranslation(Vector3d v) {
        return makeTranslation(v.x, v.y, v.z);
    }

    public Matrix4d makeTransformation(Vector3d axis, Angle angle, Vector3d v) {
        this.makeRotationAxis(axis, angle);
        return this.setPosition(v);
    }

    public Matrix4d makeRotationX(Angle angle) {

        double rad = angle.inRadians();
        double c = Math.cos(rad), s = Math.sin(rad);

        return this.set(
                1, 0, 0, 0,
                0, c, -s, 0,
                0, s, c, 0,
                0, 0, 0, 1
        );
    }

    public Matrix4d makeRotationY(Angle angle) {

        double rad = angle.inRadians();
        double c = Math.cos(rad), s = Math.sin(rad);

        return this.set(
                c, 0, s, 0,
                0, 1, 0, 0,
                -s, 0, c, 0,
                0, 0, 0, 1
        );
    }

    public Matrix4d makeRotationZ(Angle angle) {

        double rad = angle.inRadians();
        double c = Math.cos(rad), s = Math.sin(rad);

        return this.set(
                c, -s, 0, 0,
                s, c, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public Matrix4d makeRotationAxis(Vector3d axis, Angle angle) {

        double rad = angle.inRadians();

        double c = Math.cos(rad);
        double s = Math.sin(rad);
        double t = 1 - c;
        double x = axis.x, y = axis.y, z = axis.z;
        double tx = t * x, ty = t * y;

        return this.set(
                tx * x + c, tx * y - s * z, tx * z + s * y, 0,
                tx * y + s * z, ty * y + c, ty * z - s * x, 0,
                tx * z - s * y, ty * z + s * x, t * z * z + c, 0,
                0, 0, 0, 1
        );
    }

    public Matrix4d makeRotationFromQuaternion(double x, double y, double z, double w) {

        double x2 = x + x, y2 = y + y, z2 = z + z;
        double xx = x * x2, xy = x * y2, xz = x * z2;
        double yy = y * y2, yz = y * z2, zz = z * z2;
        double wx = w * x2, wy = w * y2, wz = w * z2;

        this.elements[0] = 1 - (yy + zz);
        this.elements[4] = xy - wz;
        this.elements[8] = xz + wy;

        this.elements[1] = xy + wz;
        this.elements[5] = 1 - (xx + zz);
        this.elements[9] = yz - wx;

        this.elements[2] = xz - wy;
        this.elements[6] = yz + wx;
        this.elements[10] = 1 - (xx + yy);

        // last column
        this.elements[3] = 0;
        this.elements[7] = 0;
        this.elements[11] = 0;

        // bottom row
        this.elements[12] = 0;
        this.elements[13] = 0;
        this.elements[14] = 0;
        this.elements[15] = 1;

        return this;
    }

    public Matrix4d makeRotationFromQuaternion(Quaternion q) {
        return this.makeRotationFromQuaternion(q.x, q.y, q.z, q.w);
    }

    public Matrix4d makeRotationFromEuler(Euler euler) {
        double[] te = this.elements;

        double x = euler.getX().inRadians(), y = euler.getY().inRadians(), z = euler.getZ().inRadians();
        double a = Math.cos(x), b = Math.sin(x);
        double c = Math.cos(y), d = Math.sin(y);
        double e = Math.cos(z), f = Math.sin(z);

        if (euler.getOrder() == EulerOrder.XYZ) {

            double ae = a * e, af = a * f, be = b * e, bf = b * f;

            te[0] = c * e;
            te[4] = -c * f;
            te[8] = d;

            te[1] = af + be * d;
            te[5] = ae - bf * d;
            te[9] = -b * c;

            te[2] = bf - ae * d;
            te[6] = be + af * d;
            te[10] = a * c;

        } else if (euler.getOrder() == EulerOrder.YXZ) {

            double ce = c * e, cf = c * f, de = d * e, df = d * f;

            te[0] = ce + df * b;
            te[4] = de * b - cf;
            te[8] = a * d;

            te[1] = a * f;
            te[5] = a * e;
            te[9] = -b;

            te[2] = cf * b - de;
            te[6] = df + ce * b;
            te[10] = a * c;

        } else if (euler.getOrder() == EulerOrder.ZXY) {

            double ce = c * e, cf = c * f, de = d * e, df = d * f;

            te[0] = ce - df * b;
            te[4] = -a * f;
            te[8] = de + cf * b;

            te[1] = cf + de * b;
            te[5] = a * e;
            te[9] = df - ce * b;

            te[2] = -a * d;
            te[6] = b;
            te[10] = a * c;

        } else if (euler.getOrder() == EulerOrder.ZYX) {

            double ae = a * e, af = a * f, be = b * e, bf = b * f;

            te[0] = c * e;
            te[4] = be * d - af;
            te[8] = ae * d + bf;

            te[1] = c * f;
            te[5] = bf * d + ae;
            te[9] = af * d - be;

            te[2] = -d;
            te[6] = b * c;
            te[10] = a * c;

        } else if (euler.getOrder() == EulerOrder.YZX) {

            double ac = a * c, ad = a * d, bc = b * c, bd = b * d;

            te[0] = c * e;
            te[4] = bd - ac * f;
            te[8] = bc * f + ad;

            te[1] = f;
            te[5] = a * e;
            te[9] = -b * e;

            te[2] = -d * e;
            te[6] = ad * f + bc;
            te[10] = ac - bd * f;

        } else if (euler.getOrder() == EulerOrder.XZY) {

            double ac = a * c, ad = a * d, bc = b * c, bd = b * d;

            te[0] = c * e;
            te[4] = -f;
            te[8] = d * e;

            te[1] = ac * f + bd;
            te[5] = a * e;
            te[9] = ad * f - bc;

            te[2] = bc * f - ad;
            te[6] = b * e;
            te[10] = bd * f + ac;

        }

        // last column
        te[3] = 0;
        te[7] = 0;
        te[11] = 0;

        // bottom row
        te[12] = 0;
        te[13] = 0;
        te[14] = 0;
        te[15] = 1;

        return this;
    }

    public Matrix4d makeScale(Vector3d v) {
        return this.set(
                v.x, 0, 0, 0,
                0, v.y, 0, 0,
                0, 0, v.z, 0,
                0, 0, 0, 1
        );
    }

    public Quaternion getQuaternion() {
        return getQuaternion(null);
    }

    public Quaternion getQuaternion(Quaternion store) {

        if (store == null) {
            store = new Quaternion();
        }

        return store.setFromRotationMatrix(this);
    }

    public Euler getEulerAngles() {
        return getEulerAngles(null);
    }

    public Euler getEulerAngles(Euler store) {

        if (store == null) {
            store = new Euler();
        }

        return store.setFromRotationMatrix(this);
    }

    public Matrix4d setRotation(double x, double y, double z, double w) {
        Vector3d position = getPosition();
        this.makeRotationFromQuaternion(x, y, z, w);
        return setPosition(position);
    }

    public Matrix4d setRotation(Quaternion q) {
        return setRotation(q.x, q.y, q.z, q.w);
    }

    public Matrix4d copy() {
        return new Matrix4d().copyFrom(this);
    }

    public Matrix4d copyFrom(Matrix4d m) {
        double[] me = m.elements;

        this.set(
                me[0], me[4], me[8], me[12],
                me[1], me[5], me[9], me[13],
                me[2], me[6], me[10], me[14],
                me[3], me[7], me[11], me[15]
        );

        return this;
    }

    public double[] toArray() {
        return elements.clone();
    }

    public DualQuaternion getDualQuaternionRepresentation() {
        return new DualQuaternion(getQuaternion(), getPosition());
    }

    public String toString() {
        return "Matrix4x4{\n"
                + this.elements[0] + ", " + this.elements[4] + ", " + this.elements[8] + ", " + this.elements[12] + "\n"
                + this.elements[1] + ", " + this.elements[5] + ", " + this.elements[9] + ", " + this.elements[13] + "\n"
                + this.elements[2] + ", " + this.elements[6] + ", " + this.elements[10] + ", " + this.elements[14] + "\n"
                + this.elements[3] + ", " + this.elements[7] + ", " + this.elements[11] + ", " + this.elements[15] + "\n"
                + '}';
    }

}
