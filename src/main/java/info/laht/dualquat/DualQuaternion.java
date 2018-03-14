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
public class DualQuaternion {

    private Quaternion real, dual;

    public DualQuaternion() {
        this(new Quaternion(0, 0, 0, 1), new Quaternion(0, 0, 0, 0));
    }

    public DualQuaternion(Quaternion real, Quaternion dual) {
        this.real = real;
        this.dual = dual;

        this.normalize();
    }

    public DualQuaternion(Quaternion real, Vector3d d) {
        this.real = real.copy();
        this.dual = new Quaternion(d.x, d.y, d.z, 0).multiply(real).multiplyScalar(0.5);

        this.normalize();
    }

    public DualQuaternion setPosition(Vector3d v) {
        this.dual.set(v.x, v.y, v.z, 0).multiply(real).multiplyScalar(0.5);

        return this.normalize();
    }

    public Vector3d getPosition() {
        Quaternion t = dual.copy().multiplyScalar(2).multiply(this.real.copy().conjucate());

        return new Vector3d(t.x, t.y, t.z);
    }

    public DualQuaternion multiplyScalar(double s) {
        this.real.multiplyScalar(s);
        this.dual.multiplyScalar(s);

        return this;
    }

    public DualQuaternion multiply(DualQuaternion q) {
        return multiplyDualQuaternions(this, q);
    }

    public DualQuaternion multiplyDualQuaternions(DualQuaternion q1, DualQuaternion q2) {
        Quaternion real_ = new Quaternion().copyFrom(q1.real).multiply(q2.real);
        Quaternion dual_ = new Quaternion().copyFrom(q1.real).multiply(q2.dual).add(q1.dual.copy().multiply(q2.real));

        this.real.copyFrom(real_);
        this.dual.copyFrom(dual_);

        return this.normalize();
    }

    public double dot(DualQuaternion q) {
        return this.real.dot(q.real);
    }

    public DualQuaternion normalize() {

        double mag = Math.sqrt(real.dot(real));

        this.real.multiplyScalar(1.0 / mag);
        this.dual.multiplyScalar(1.0 / mag);

        return this;
    }

    public DualQuaternion conjugate() {
        this.real.conjucate();
        this.dual.conjucate();

        return this;
    }

    public DualQuaternion invert() {
        return this.normalize().conjugate();
    }

    public DualQuaternion makeTranslation(Vector3d v) {
        return makeTranslation(v.x, v.y, v.z);
    }

    public DualQuaternion makeTranslation(double x, double y, double z) {
        this.real.set(0, 0, 0, 1);
        this.dual.set(x, y, z, 0).multiply(this.real).multiplyScalar(0.5);

        return this;
    }

    public DualQuaternion makeTransformation(Vector3d axis, Angle angle, Vector3d v) {
        this.real.setFromAxisAngle(axis, angle);
        this.dual.set(v.x, v.y, v.z, 0).multiply(this.real).multiplyScalar(0.5);

        return this;
    }

    public DualQuaternion makeRotationFromEuler(Euler euler) {
        this.real.setFromEuler(euler);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion makeRotationFromQuaternion(Quaternion q) {
        this.real.set(q.x, q.y, q.z, q.w);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion makeRotationAxis(Vector3d axis, Angle angle) {
        this.real.setFromAxisAngle(axis, angle);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion makeRotationX(Angle angle) {
        this.real.setFromAxisAngle(Vector3d.X, angle);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion makeRotationY(Angle angle) {
        this.real.setFromAxisAngle(Vector3d.Y, angle);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion makeRotationZ(Angle angle) {
        this.real.setFromAxisAngle(Vector3d.Z, angle);
        this.dual.set(0, 0, 0, 0);

        return this;
    }

    public DualQuaternion copy() {
        return new DualQuaternion().copyFrom(this);
    }

    public DualQuaternion copyFrom(DualQuaternion q) {
        this.real.copyFrom(q.real);
        this.dual.copyFrom(q.dual);

        return this.normalize();

    }

    public Quaternion getQuaternion() {
        return real.copy();
    }

    public Euler getEulerAngles() {
        return new Euler().setFromQuaternion(real.copy());
    }

    public Matrix4d getMatrixRepresentation() {
        Matrix4d mat = new Matrix4d().makeRotationFromQuaternion(real.copy());

        return mat.setPosition(getPosition());
    }

    public DualQuaternion getDualQuaternionRepresentation() {
        return this;
    }

    public DualQuaternion multiply(Matrix4d m) {
        return multiplyDualQuaternions(this, m.getDualQuaternionRepresentation());
    }

    public DualQuaternion setRotation(Quaternion q) {

        Vector3d position = getPosition();
        makeRotationFromQuaternion(q);
        return setPosition(position);

    }

    @Override
    public String toString() {
        return "DualQuaternion{" + "position=" + getPosition() + ", euler=" + getEulerAngles() + '}';
    }
}
