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

public class Vector3d  {

    public final static Vector3d ZEROS = new Vector3d(0, 0, 0);
    public final static Vector3d ONES = new Vector3d(1, 1, 1);
    public final static Vector3d X = new Vector3d(1, 0, 0);
    public final static Vector3d Y = new Vector3d(0, 1, 0);
    public final static Vector3d Z = new Vector3d(0, 0, 1);
    public final static Vector3d X_NEG = new Vector3d(-1, 0, 0);
    public final static Vector3d Y_NEG = new Vector3d(0, -1, 0);
    public final static Vector3d Z_NEG = new Vector3d(0, 0, -1);

    public final double x, y, z;

    public Vector3d() {
        this(0, 0, 0);
    }

    public Vector3d(double fill) {
        this(fill, fill, fill);
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3d setX(double x) {
        return new Vector3d(x, y, z);
    }

    public Vector3d setY(double y) {
        return new Vector3d(x, y, z);
    }

    public Vector3d setZ(double z) {
        return new Vector3d(x, y, z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    /**
     * Get a new vector with negated values
     * @return a new instance with negated xyz values
     */
    public Vector3d negate() {
        return new Vector3d(-x, -y, -z);
    }

    /**
     * Computes the squared length of this vector
     * @return the squared length of this vector
     */
    public double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    /**
     * Computes length of this vector.
     *
     * @return the length of this vector
     */
    public double length() {
        return Math.sqrt(lengthSq());
    }
    
    /**
     * Sets the length of the vector
     * @param len the new length
     * @return a new Vector3d instance with the desired length
     */
    public Vector3d setLength(double len) {
        return normalize().multiply(len);
    }

    /**
     * Vector addition
     *
     * @param v the vector to add
     * @return a new vector
     */
    public Vector3d add(Vector3d v) {
        return new Vector3d(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Scalar add
     *
     * @param scalar number to add
     * @return a new vector
     */
    public Vector3d add(double scalar) {
        return new Vector3d(x + scalar, y + scalar, z + scalar);
    }

    /**
     * Vector subtraction
     *
     * @param v the vector to subtract
     * @return a new vector
     */
    public Vector3d sub(Vector3d v) {
        return new Vector3d(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Scalar subtraction
     *
     * @param scalar number to subtract
     * @return a new vector
     */
    public Vector3d sub(double scalar) {
        return new Vector3d(x - scalar, y - scalar, z - scalar);
    }

    /**
     * Vector multiplication
     *
     * @param v the vector to multiply
     * @return a new vector
     */
    public Vector3d multiply(Vector3d v) {
        return new Vector3d(x * v.x, y * v.y, z * v.z);
    }

    /**
     * Scalar multiplication
     *
     * @param scalar the scalar to multiply each vector element with
     * @return a new vector
     */
    public Vector3d multiply(double scalar) {
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Divides this vector by vector v.
     *
     * @param v the vector to divide with
     * @return the result
     */
    public Vector3d divide(Vector3d v) {
        return new Vector3d(this.x / v.x, this.y / v.y, this.z / v.z);
    }

    /**
     * Divides this vector by scalar s. Set vector to ( 0, 0, 0 ) if s == 0.
     *
     * @param scalar the scalar to divide with
     * @return the result
     */
    public Vector3d divide(double scalar) {
        if (scalar != 0) {
            return new Vector3d(this.x / scalar, this.y / scalar, this.z / scalar);
        } else {
            return Vector3d.ZEROS;
        }
    }

    /**
     * Normalizes this vector. Transforms this Vector into a Unit vector by
     * dividing the vector by it's length.
     *
     * @return a normalized vector
     */
    public Vector3d normalize() {
        return divide(this.length());
    }

    /**
     * Computes the cross product between this and v
     *
     * @param v the v vector
     * @return the cross product
     */
    public Vector3d cross(Vector3d v) {
        double xx = this.y * v.z - this.z * v.y;
        double yy = this.z * v.x - this.x * v.z;
        double zz = this.x * v.y - this.y * v.x;

        return new Vector3d(xx, yy, zz);
    }

    /**
     * AngleTo
     *
     * @param v the vector to measure angle to
     * @return the angle between this vector and vector v in radians.
     */
    public double angleTo(Vector3d v) {
        double theta = this.dot(v) / (this.length() * v.length());

        return Math.acos(MathUtil.clamp(theta, -1, 1));
    }

    /**
     * Computes dot product of this vector and v.
     *
     * @param v the v vector
     * @return the dot product between this and the v vector
     */
    public double dot(Vector3d v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    /**
     * Calculates the euclidian distance between this and the v vector
     *
     * @param v the v vector
     * @return the euclidian distance between this and the v vector
     */
    public double dist(Vector3d v) {
        return Math.sqrt(distSquared(v));
    }

    /**
     * Squared distance
     *
     * @param v the v vector
     * @return the squared distance. Use dist for metric distance
     */
    public double distSquared(Vector3d v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        double dz = this.z - v.z;

        return dx * dx + dy * dy + dz * dz;
    }

    public Vector3d applyFrame3(Matrix4d t) {

        double[] e = t.elements;
        return new Vector3d(
                e[0] * this.x + e[4] * this.y + e[8] * this.z,
                e[1] * this.x + e[5] * this.y + e[9] * this.z,
                e[2] * this.x + e[6] * this.y + e[810] * this.z);
    }

    public Vector3d applyFrame4(Matrix4d t) {

        double[] e = t.elements;
        return new Vector3d(
                e[0] * this.x + e[4] * this.y + e[8] * this.z + e[12],
                e[1] * this.x + e[5] * this.y + e[9] * this.z + e[13],
                e[2] * this.x + e[6] * this.y + e[10] * this.z + e[14]);
    }

    /**
     * Transforms the direction of this vector by the rotational part of a
     * transformation and then normalizes the result.
     *
     * @param m transformData
     * @return a new vector
     */
    public Vector3d transformDirection(Matrix4d m) {

        double[] e = m.elements;
        return new Vector3d(
                e[0] * x + e[4] * y + e[8] * z,
                e[1] * x + e[5] * y + e[9] * z,
                e[2] * x + e[6] * y + e[10] * z)
                .normalize();
    }

    public Vector3d applyQuaternion(Quaternion q) {

        double qx = q.x;
        double qy = q.y;
        double qz = q.z;
        double qw = q.w;

        // calculate quat * vector
        double ix = qw * x + qy * z - qz * y;
        double iy = qw * y + qz * x - qx * z;
        double iz = qw * z + qx * y - qy * x;
        double iw = -qx * x - qy * y - qz * z;

        // calculate result * inverse quat
        double x_ = ix * qw + iw * -qx + iy * -qz - iz * -qy;
        double y_ = iy * qw + iw * -qy + iz * -qx - ix * -qz;
        double z_ = iz * qw + iw * -qz + ix * -qy - iy * -qx;

        return new Vector3d(x_, y_, z_);
    }

    public Vector3d floor() {
        return new Vector3d(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    public Vector3d round() {
        return new Vector3d(Math.round(x), Math.round(y), Math.round(z));
    }

    public Vector3d ceil() {
        return new Vector3d(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    public Vector3d clamp(Vector3d min, Vector3d max) {
        // This function assumes min < max, if this assumption isn't true it will not operate correctly

        return new Vector3d(
                MathUtil.clamp(x, min.x, max.x),
                MathUtil.clamp(y, min.y, max.y),
                MathUtil.clamp(z, min.z, max.z));
    }

    /**
     * Creates a column matrix of this
     *
     * @return a column matrix with 3 columns 1 row
     */
    public double[][] toColumnMatrix3() {
        return new double[][]{{x}, {y}, {z}};
    }

    /**
     * Creates a column matrix of this
     *
     * @return a column matrix with 4 columns 1 row
     */
    public double[][] toColumnMatrix4() {
        return new double[][]{{x}, {y}, {z}, {1}};
    }

    /**
     * Creates a row matrix of this
     *
     * @return a row matrix with 1 column 3 rows
     */
    public double[][] toRowMatrix3() {
        return new double[][]{{x, y, z}};
    }

    /**
     * Creates a row matrix of this
     *
     * @return a row matrix with 1 column 4 rows
     */
    public double[][] toRowMatrix4() {
        return new double[][]{{x, y, z, 1}};
    }

    /**
     * Is this a unit vector (length = 1) ?
     *
     * @return true if yes, false otherwise
     */
    public boolean isUnitLength() {
        return Math.abs(length() - 1) < 1E-4;
    }

    /**
     * Creates a new vector with positive numbers only
     * @return a new instance with positive xyz values
     */
    public Vector3d abs() {
        return new Vector3d(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public Vector3d lerp(Vector3d v, double alpha) {
        double _x = this.x + (v.x - this.x) * alpha;
        double _y = this.y + (v.y - this.y) * alpha;
        double _z = this.z + (v.z - this.z) * alpha;

        return new Vector3d(_x, _y, _z);
    }

    /**
     * Assigns this vector's x value to array[0]. Assigns this vector's y value
     * to array[1]. Assigns this vector's z value to array[2].
     *
     * @return the created array
     */
    public double[] toArray() {
        return new double[]{x, y, z};
    }
   
    /**
     * Lerp function
     *
     * @param a start
     * @param b end
     * @param fraction
     * @return the interpolation point
     */
    public static Vector3d lerp(Vector3d a, Vector3d b, double fraction) {
        return b.sub(a).multiply(fraction).add(a);
    }

    /**
     * Produces a new vector using the min values from v1 and c2
     *
     * @param v1 a vector
     * @param v2 a vector
     * @return a new vector
     */
    public static Vector3d min(Vector3d v1, Vector3d v2) {
        return new Vector3d(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
    }

    /**
     * Produces a new vector using the max values from v1 and c2
     *
     * @param v1 a vector
     * @param v2 a vector
     * @return a new vector
     */
    public static Vector3d max(Vector3d v1, Vector3d v2) {
       return new Vector3d(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
    }

    public static Vector3d crossVectors(Vector3d v1, Vector3d v2) {
        double x = v1.y * v2.z - v1.z * v2.y;
        double y = v1.z * v2.x - v1.x * v2.z;
        double z = v1.x * v2.y - v1.y * v2.x;

        return new Vector3d(x, y, z);
    }
    
    /**
     * 
     * @param delimiter string to put between values
     * @param beforeAndAfter string to put before and after body (can be null)
     * @return a custom string representation of this vector
     */
    public String toSimpleString(String delimiter, String beforeAndAfter) {
        
        if (delimiter == null) {
            throw new IllegalArgumentException("Delimiter was null");
        }
        
        if (beforeAndAfter == null) {
            beforeAndAfter = "";
        }
        
        return beforeAndAfter + x + delimiter + y + delimiter + z + delimiter + beforeAndAfter;
        
    }

    @Override
    public String toString() {
        return "Vector3d{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }

   
}