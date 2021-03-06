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

package org.ejml.sparse.csc.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertDMatrixSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_DSCC {

    Random rand = new Random(234);

    @Test
    public void solveL_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 1, rand);
            DMatrixRMaj x = b.copy();

            TriangularSolver_DSCC.solveL(L, x.data);

            DMatrixRMaj found = x.createLike();
            CommonOps_DSCC.mult(L, x, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveU_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
            DMatrixSparseCSC U = new DMatrixSparseCSC(5, 5, L.nz_length);
            CommonOps_DSCC.transpose(L, U, null);

            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 1, rand);
            DMatrixRMaj x = b.copy();

            TriangularSolver_DSCC.solveU(U, x.data);

            DMatrixRMaj found = x.createLike();
            CommonOps_DSCC.mult(U, x, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solve_sparseX_vector() {
        solve_sparseX_vector(true);
        solve_sparseX_vector(false);
    }

    public void solve_sparseX_vector( boolean lower ) {
        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
                int lengthX = rand.nextInt(3)+3;

                DMatrixSparseCSC G;
                if( lower)
                    G = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
                else
                    G = RandomMatrices_DSCC.triangleUpper(5, 0, nz_size, -1, 1, rand);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(5, 1,lengthX, rand);
                DMatrixRMaj x = new DMatrixRMaj(b.numRows,b.numCols);

                int ret = TriangularSolver_DSCC.solve(G,lower, b,0, x.data, null, null);
                assertTrue(5-lengthX >= ret);

                DMatrixRMaj found = x.createLike();
                CommonOps_DSCC.mult(G, x, found);

                DMatrixRMaj expected = ConvertDMatrixSparse.convert(b,(DMatrixRMaj)null);
                assertTrue(MatrixFeatures_DDRM.isEquals(found, expected, UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void solve_sparseX_matrix() {
        solve_sparseX_matrix(true);
        solve_sparseX_matrix(false);
    }

    public void solve_sparseX_matrix( boolean lower ) {
        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
                nz_size = 8;
                int lengthX = rand.nextInt(3)+3;

                DMatrixSparseCSC G;
                if( lower)
                    G = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
                else
                    G = RandomMatrices_DSCC.triangleUpper(5, 0, nz_size, -1, 1, rand);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(5, 2,lengthX*2, rand);
                DMatrixSparseCSC x = new DMatrixSparseCSC(b.numRows,b.numCols,1);

                TriangularSolver_DSCC.solve(G,lower,b,x, null, null, null);

                DMatrixSparseCSC found = x.createLike();
                CommonOps_DSCC.mult(G, x, found);

                // Don't use a sparse test since the solution might contain 0 values due to cancellations
                EjmlUnitTests.assertEquals(found,b);
            }
        }
    }


    /**
     * Test a simple case where A is diagonal
     */
    @Test
    public void searchNzRowsInB_diag() {
        DMatrixSparseCSC A = CommonOps_DSCC.diag(1,2,3);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(3,1,3,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        // A is diagonal and B is filled in
        int top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);

        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal and B is empty
        B = new DMatrixSparseCSC(3,1,3);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(3,top);

        // A is diagonal and B has element 1 not zero
        B.set(1,0,2.0);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(2,top);
        assertEquals(1,xi[2]);

        // A is diagonal with one missing and B is full
        A.remove(1,1);
        B = RandomMatrices_DSCC.rectangle(3,1,3,-1,1,rand);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal with one missing and B is missing the same element
        B.remove(1,0);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(1,top);
        assertEquals(2,xi[1]);
        assertEquals(0,xi[2]);

    }

    /**
     * A is filled in triangular
     */
    @Test
    public void searchNzRowsInB_triangle() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.triangleLower(4,0,16, -1,1,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(4,1,4,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        int top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }

        // Add a hole which should be filled in
        B.remove(1,0);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }

        // add a hole on top.  This should not be filled in nor the one below it
        B.remove(0,0);
        top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(2,top);
        for (int i = 0; i < 2; i++) {
            assertEquals(i+2,xi[i]);
        }
    }


    /**
     * hand constructed system and verify that the results are as expected
     */
    @Test
    public void searchNzRowsInB_case0() {
        DMatrixRMaj D = UtilEjml.parse_DDRM(
                     "1 0 0 0 0 " +
                        "1 1 0 0 0 "+
                        "0 1 1 0 0 " +
                        "1 0 0 1 0 " +
                        "0 1 0 0 1",5);

        DMatrixSparseCSC A = ConvertDMatrixSparse.convert(D,(DMatrixSparseCSC)null);

        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(5,1,4,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        int top = TriangularSolver_DSCC.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        assertEquals(0,xi[0]); // hand traced through
        assertEquals(3,xi[1]);
        assertEquals(1,xi[2]);
        assertEquals(4,xi[3]);
        assertEquals(2,xi[4]);
    }

    /**
     * All elements in A are filled in.  ata = false
     */
    @Test
    public void eliminationTree_full_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 1 1 1 1 " +
                        "0 1 1 1 1 "+
                        "0 0 1 1 1 " +
                        "0 0 0 1 1 " +
                        "0 0 0 0 1",5);

        int parent[] = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        for (int i = 0; i < A.numCols-1; i++) {
            assertEquals(i+1,parent[i]);
        }
        assertEquals(-1,parent[A.numCols-1]);
    }

    /**
     * All elements in A are empty except the diagonal ones. ata = false
     */
    @Test
    public void eliminationTree_diagonal_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 0 0 0 " +
                        "0 1 0 0 0 "+
                        "0 0 1 0 0 " +
                        "0 0 0 1 0 " +
                        "0 0 0 0 1",5);

        int parent[] = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(-1,parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     */
    @Test
    public void eliminationTree_case0_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 0 0 0 " +
                        "0 1 1 1 0 "+
                        "0 0 1 0 1 " +
                        "0 0 0 1 0 " +
                        "0 0 0 0 1 ",5);

        int parent[] = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int expected[] = new int[]{-1,2,3,4,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     */
    @Test
    public void eliminationTree_case1_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 0 " +
                        "0 1 0 1 0 0 0 " +
                        "0 0 1 0 1 0 0 " +
                        "0 0 0 1 0 0 0 " +
                        "0 0 0 0 1 0 1 " +
                        "0 0 0 0 0 1 1 " +
                        "0 0 0 0 0 0 1 ",7);

        int parent[] = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int expected[] = new int[]{2,3,3,4,5,6,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     * This is designed to make sure I didn't cheat in the previous test
     */
    @Test
    public void eliminationTree_case2_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 " +
                        "0 1 0 1 0 0 " +
                        "0 0 1 0 1 0 " +
                        "0 0 0 1 0 0 " +
                        "0 0 0 0 1 0 " +
                        "0 0 0 0 0 1 " ,6);

        int parent[] = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int expected[] = new int[]{2,3,3,4,5,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Test the elimination tree from its definition.  See if it
     */
    @Ignore
    @Test
    public void eliminationTree_random_square() {
        fail("Implement");
    }

    /**
     * Test case for tall input arrays. ata = true
     */
    @Ignore
    @Test
    public void eliminationTree_case0_tall() {
        fail("implement");
    }

    /**
     * Hand constructed test case
     */
    @Test
    public void searchNzRowsElim_case0() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 0 " +
                        "0 1 0 1 0 0 0 " +
                        "0 0 1 0 1 0 0 " +
                        "0 0 0 1 0 0 0 " +
                        "0 0 0 0 1 0 1 " +
                        "0 0 0 0 0 1 1 " +
                        "0 0 0 0 0 0 1 ",7);

        int parent[] = new int[]{2,3,3,4,5,6,-1};

        int top, s[] = new int[7], w[] = new int[7];

        int expected[];

        // check each row one at a time
        top = TriangularSolver_DSCC.searchNzRowsElim(A,0,parent,s,w);
        assertEquals(top,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,1,parent,s,w);
        assertEquals(top,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,2,parent,s,w);
        assertEquals(top,A.numCols-1);
        expected = new int[]{0,0,0,0,0,0,0};
        assertSetEquals(expected,s,A.numCols-1,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,3,parent,s,w);
        assertEquals(top,A.numCols-3);
        expected = new int[]{0,0,0,0,0,1,2};
        assertSetEquals(expected,s,A.numCols-3,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,4,parent,s,w);
        assertEquals(top,A.numCols-2);
        expected = new int[]{0,0,0,0,0,2,3};
        assertSetEquals(expected,s,A.numCols-2,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,5,parent,s,w);
        assertEquals(top,A.numCols-4);
        expected = new int[]{0,0,0,0,2,3,4};
        assertSetEquals(expected,s,A.numCols-4,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,6,parent,s,w);
        assertEquals(top,A.numCols-2);
        expected = new int[]{0,0,0,0,0,4,5};
        assertSetEquals(expected,s,A.numCols-2,A.numCols);
    }

    /**
     * Makes sure the same elements are contained in the two list but order doesn't matter
     */
    private static void assertSetEquals( int expected[] , int found[], int start , int end ) {
        boolean matched[] = new boolean[end];
        for (int i = start; i < end; i++) {
            if( matched[i] )
                fail("matched twice");
            matched[found[i]] = true;
        }

        for (int i = start; i < end; i++) {
            assertTrue(matched[expected[i]]);
        }
    }



}