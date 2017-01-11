/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.dense.fixed;

import org.ejml.data.DMatrixFixed3x3_F64;
import org.ejml.data.DMatrixFixed4x4_F64;
import org.ejml.data.DMatrixFixed6x6_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.ops.ConvertMatrixStruct_F64;

import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkMultiplicationFixed {
    private static Random rand = new Random(234);

    private static DMatrixRow_F64 dm3x3_a = new DMatrixRow_F64(3,3);
    private static DMatrixRow_F64 dm3x3_b = new DMatrixRow_F64(3,3);
    private static DMatrixRow_F64 dm3x3_c = new DMatrixRow_F64(3,3);

    private static DMatrixRow_F64 dm4x4_a = new DMatrixRow_F64(4,4);
    private static DMatrixRow_F64 dm4x4_b = new DMatrixRow_F64(4,4);
    private static DMatrixRow_F64 dm4x4_c = new DMatrixRow_F64(4,4);

    private static DMatrixRow_F64 dm6x6_a = new DMatrixRow_F64(6,6);
    private static DMatrixRow_F64 dm6x6_b = new DMatrixRow_F64(6,6);
    private static DMatrixRow_F64 dm6x6_c = new DMatrixRow_F64(6,6);


    private static DMatrixFixed3x3_F64 fixed3x3_a = new DMatrixFixed3x3_F64();
    private static DMatrixFixed3x3_F64 fixed3x3_b = new DMatrixFixed3x3_F64();
    private static DMatrixFixed3x3_F64 fixed3x3_c = new DMatrixFixed3x3_F64();

    private static DMatrixFixed4x4_F64 fixed4x4_a = new DMatrixFixed4x4_F64();
    private static DMatrixFixed4x4_F64 fixed4x4_b = new DMatrixFixed4x4_F64();
    private static DMatrixFixed4x4_F64 fixed4x4_c = new DMatrixFixed4x4_F64();

    private static DMatrixFixed6x6_F64 fixed6x6_a = new DMatrixFixed6x6_F64();
    private static DMatrixFixed6x6_F64 fixed6x6_b = new DMatrixFixed6x6_F64();
    private static DMatrixFixed6x6_F64 fixed6x6_c = new DMatrixFixed6x6_F64();

    public static long benchmark(DMatrixRow_F64 a, DMatrixRow_F64 b , DMatrixRow_F64 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps_R64.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrixFixed3x3_F64 a, DMatrixFixed3x3_F64 b , DMatrixFixed3x3_F64 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps3_F64.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrixFixed4x4_F64 a, DMatrixFixed4x4_F64 b , DMatrixFixed4x4_F64 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps4_F64.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(DMatrixFixed6x6_F64 a, DMatrixFixed6x6_F64 b , DMatrixFixed6x6_F64 c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            FixedOps6_F64.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static void main( String arg[] ) {
        RandomMatrices_R64.setRandom(dm3x3_a,rand);
        RandomMatrices_R64.setRandom(dm3x3_b,rand);
        RandomMatrices_R64.setRandom(dm3x3_c,rand);

        RandomMatrices_R64.setRandom(dm4x4_a,rand);
        RandomMatrices_R64.setRandom(dm4x4_b,rand);
        RandomMatrices_R64.setRandom(dm4x4_c,rand);

        RandomMatrices_R64.setRandom(dm6x6_a,rand);
        RandomMatrices_R64.setRandom(dm6x6_b,rand);
        RandomMatrices_R64.setRandom(dm6x6_c,rand);

        ConvertMatrixStruct_F64.convert(dm3x3_a,fixed3x3_a);
        ConvertMatrixStruct_F64.convert(dm3x3_b,fixed3x3_b);
        ConvertMatrixStruct_F64.convert(dm3x3_c,fixed3x3_c);

        ConvertMatrixStruct_F64.convert(dm4x4_a,fixed4x4_a);
        ConvertMatrixStruct_F64.convert(dm4x4_b,fixed4x4_b);
        ConvertMatrixStruct_F64.convert(dm4x4_c,fixed4x4_c);

        ConvertMatrixStruct_F64.convert(dm6x6_a,fixed6x6_a);
        ConvertMatrixStruct_F64.convert(dm6x6_b,fixed6x6_b);
        ConvertMatrixStruct_F64.convert(dm6x6_c,fixed6x6_c);

        int numTrials = 100000000;

        System.out.println("Dense 3x3 = "+benchmark(dm3x3_a,dm3x3_b,dm3x3_c,numTrials));
        System.out.println("Fixed 3x3 = "+benchmark(fixed3x3_a,fixed3x3_b,fixed3x3_c,numTrials));

        numTrials = 50000000;


        System.out.println("Dense 4x4 = "+benchmark(dm4x4_a,dm4x4_b,dm4x4_c,numTrials));
        System.out.println("Fixed 4x4 = "+benchmark(fixed4x4_a,fixed4x4_b,fixed4x4_c,numTrials));

        numTrials = 10000000;

        System.out.println("Dense 6x6 = "+benchmark(dm6x6_a,dm6x6_b,dm6x6_c,numTrials));
        System.out.println("Fixed 6x6 = "+benchmark(fixed6x6_a,fixed6x6_b,fixed6x6_c,numTrials));
    }
}