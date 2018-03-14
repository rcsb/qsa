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
public class Test {

    public static void main(String[] args) {

        Matrix4d m1 = new Matrix4d().makeTranslation(0, 0, 5);
        Matrix4d m2 = new Matrix4d().makeRotationX(Angle.DEG_90);
        Matrix4d m3 = new Matrix4d().makeTranslation(0, 0, 2);

        Matrix4d matrixResult = m1.multiply(m2).multiply(m3);

        DualQuaternion d1 = new DualQuaternion().makeTranslation(0, 0, 5);
        DualQuaternion d2 = new DualQuaternion().makeRotationX(Angle.DEG_90);
        DualQuaternion d3 = new DualQuaternion().makeTranslation(0, 0, 2);

        DualQuaternion dqResult = d1.multiply(d2).multiply(d3);

        System.out.println(matrixResult.getPosition() + "\t" + matrixResult.getEulerAngles());
        System.out.println(dqResult.getPosition() + "\t" + dqResult.getEulerAngles());

    }

}
