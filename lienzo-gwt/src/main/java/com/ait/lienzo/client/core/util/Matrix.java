/*
   Copyright (c) 2017 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.client.core.util;

/**
 * Matrix class with basic matrix operations.
 *
 * @see http://introcs.cs.princeton.edu/java/95linear/Matrix.java.html
 * <p>
 * Do not try to bend the spoon, that's impossible. Instead, only try to realize the truth: there is no spoon.
 */
public final class Matrix {

    private final int m_rows;

    private final int m_columns;

    private final double[][] m_data;   // [rows][columns] array

    /**
     * Creates a matrix with zeroes.
     *
     * @param rows
     * @param columns
     */
    public Matrix(int rows, int columns) {
        m_rows = rows;

        m_columns = columns;

        m_data = new double[rows][columns];
    }

    /**
     * Creates a matrix from 2-dimensional array
     *
     * @param data
     */
    public Matrix(double[][] data) {
        m_rows = data.length;

        m_columns = data[0].length;

        m_data = new double[m_rows][m_columns];

        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                m_data[i][j] = data[i][j];
            }
        }
    }

    /**
     * Copy constructor
     *
     * @param A
     */
    private Matrix(Matrix A) {
        this(A.m_data);
    }

    /**
     * Creates and returns the N-by-N identity matrix
     *
     * @param N
     * @return
     */
    public static Matrix identity(int N) {
        Matrix I = new Matrix(N, N);

        for (int i = 0; i < N; i++) {
            I.m_data[i][i] = 1;
        }
        return I;
    }

    /**
     * Swaps rows i and j
     *
     * @param i
     * @param j
     */
    private void swap(int i, int j) {
        double[] temp = m_data[i];

        m_data[i] = m_data[j];

        m_data[j] = temp;
    }

    /**
     * Creates and returns the transpose of the matrix
     *
     * @return
     */
    public Matrix transpose() {
        Matrix A = new Matrix(m_columns, m_rows);

        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                A.m_data[j][i] = this.m_data[i][j];
            }
        }
        return A;
    }

    /**
     * Returns C = A + B
     *
     * @param B
     * @return new Matrix C
     * @throws GeometryException if the matrix dimensions don't match
     */
    public Matrix plus(Matrix B) {
        Matrix A = this;

        if (B.m_rows != A.m_rows || B.m_columns != A.m_columns) {
            throw new GeometryException("Illegal matrix dimensions");
        }
        Matrix C = new Matrix(m_rows, m_columns);

        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                C.m_data[i][j] = A.m_data[i][j] + B.m_data[i][j];
            }
        }
        return C;
    }

    /**
     * Returns C = A - B
     *
     * @param B
     * @return new Matrix C
     * @throws GeometryException if the matrix dimensions don't match
     */
    public Matrix minus(Matrix B) {
        Matrix A = this;

        if (B.m_rows != A.m_rows || B.m_columns != A.m_columns) {
            throw new GeometryException("Illegal matrix dimensions");
        }
        Matrix C = new Matrix(m_rows, m_columns);

        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                C.m_data[i][j] = A.m_data[i][j] - B.m_data[i][j];
            }
        }
        return C;
    }

    /**
     * Returns whether the matrix is the same as this matrix.
     *
     * @param B
     * @return
     */
    public boolean eq(Matrix B) {
        Matrix A = this;

        if (B.m_rows != A.m_rows || B.m_columns != A.m_columns) {
            return false;
        }
        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                if (A.m_data[i][j] != B.m_data[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns C = A * B
     *
     * @param B
     * @return new Matrix C
     * @throws GeometryException if the matrix dimensions don't match
     */
    public Matrix times(Matrix B) {
        Matrix A = this;

        if (A.m_columns != B.m_rows) {
            throw new GeometryException("Illegal matrix dimensions");
        }
        Matrix C = new Matrix(A.m_rows, B.m_columns);

        for (int i = 0; i < C.m_rows; i++) {
            for (int j = 0; j < C.m_columns; j++) {
                for (int k = 0; k < A.m_columns; k++) {
                    C.m_data[i][j] += (A.m_data[i][k] * B.m_data[k][j]);
                }
            }
        }
        return C;
    }

    /**
     * Returns x = A^-1 b, assuming A is square and has full rank
     *
     * @param rhs
     * @return Matrix x
     * @throws GeometryException if the matrix dimensions don't match
     */
    public Matrix solve(Matrix rhs) {
        if (m_rows != m_columns || rhs.m_rows != m_columns || rhs.m_columns != 1) {
            throw new GeometryException("Illegal matrix dimensions");
        }
        Matrix coefficients = new Matrix(this);
        Matrix values = new Matrix(rhs);
        for (int column = 0; column < m_columns; column++) {
            eliminateColumn(coefficients, values, column);
        }
        return backSubstitute(coefficients, values);
    }

    private void eliminateColumn(Matrix coefficients, Matrix values, int column) {
        int pivot = column;
        for (int row = column + 1; row < m_columns; row++) {
            if (Math.abs(coefficients.m_data[row][column]) > Math.abs(coefficients.m_data[pivot][column])) {
                pivot = row;
            }
        }
        coefficients.swap(column, pivot);
        values.swap(column, pivot);
        if (coefficients.m_data[column][column] == 0.0) {
            throw new RuntimeException("Matrix is singular.");
        }

        for (int row = column + 1; row < m_columns; row++) {
            values.m_data[row][0] -= values.m_data[column][0] * coefficients.m_data[row][column]
                    / coefficients.m_data[column][column];
        }
        for (int row = column + 1; row < m_columns; row++) {
            double factor = coefficients.m_data[row][column] / coefficients.m_data[column][column];
            for (int col = column + 1; col < m_columns; col++) {
                coefficients.m_data[row][col] -= coefficients.m_data[column][col] * factor;
            }
            coefficients.m_data[row][column] = 0.0;
        }
    }

    private Matrix backSubstitute(Matrix coefficients, Matrix values) {
        Matrix solution = new Matrix(m_columns, 1);
        for (int row = m_columns - 1; row >= 0; row--) {
            double sum = 0.0;
            for (int col = row + 1; col < m_columns; col++) {
                sum += coefficients.m_data[row][col] * solution.m_data[col][0];
            }
            solution.m_data[row][0] = (values.m_data[row][0] - sum) / coefficients.m_data[row][row];
        }
        return solution;
    }

    /**
     * Formats the matrix data as a multi-line string for debugging purposes.
     */
    public String toString() {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < m_rows; i++) {
            for (int j = 0; j < m_columns; j++) {
                if (j > 0) {
                    b.append(", ");
                }
                b.append(m_data[i][j]);
            }
            b.append("\n");
        }
        return b.toString();
    }

    /**
     * Returns the matrix data as a 2-dimensional array with [rows][columns].
     *
     * @return double[][]
     */
    public double[][] getData() {
        return m_data;
    }
}
