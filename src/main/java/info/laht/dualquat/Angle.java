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

/**
 *
 * @author LarsIvar
 */
public class Angle {

    private static final Logger LOG = Logger.getLogger(Angle.class.getName());

    public static final Angle DEG_0 = new Angle(0, Representation.DEG);
    public static final Angle DEG_45 = new Angle(45, Representation.DEG);
    public static final Angle DEG_90 = new Angle(90, Representation.DEG);
    public static final Angle DEG_180 = new Angle(180, Representation.DEG);
    public static final Angle DEG_360 = new Angle(360, Representation.DEG);

    public enum Representation {

        DEG, RAD
    };

    private final double phi;

    public Angle(double phi, Representation repr) {
        this(ensureRadians(phi, repr));
    }

    private Angle(double phi) {
        this.phi = phi;

        if (Double.isNaN(phi)) {
            LOG.log(Level.WARNING, "phi is NaN!");
        }

    }

    public Angle negate() {
        return new Angle(-phi);
    }

    public Angle plus(Angle a) {
        return new Angle(phi + a.phi);
    }

    public Angle minus(Angle a) {
        return new Angle(phi - a.phi);
    }

    public Angle multiply(Angle a) {
        return new Angle(phi * a.phi);
    }

    public Angle divide(Angle a) {
        return new Angle(phi / a.phi);
    }

    public boolean isGreaterThan(Angle angle) {
        return phi > angle.phi;
    }

    public boolean isLessThan(Angle angle) {
        return phi < angle.phi;
    }

    public double inRadians() {
        return phi;
    }

    public double inDegrees() {
        return Math.toDegrees(phi);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.phi) ^ (Double.doubleToLongBits(this.phi) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Angle other = (Angle) obj;
        if (Double.doubleToLongBits(this.phi) != Double.doubleToLongBits(other.phi)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Angle{" + "inDegrees=" + inDegrees() + ", inRadians=" + inRadians() + '}';
    }

    public static Angle meanAngle(Angle a1, Angle a2) {
        return new Angle((a1.phi + a2.phi) / 2);
    }

    private static double ensureRadians(double phi, Representation repr) {
        switch (repr) {
            case DEG:
                return Math.toRadians(phi);
            case RAD:
                return phi;
            default:
                throw new IllegalArgumentException();
        }

    }

}
