package com.example.minesweeper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Pure game logic for a Minesweeper board. No Android dependencies,
// so it can be unit tested on the JVM.
public class Board {

    private final int rows;
    private final int columns;

    // mines are stored as cell indices: row * columns + column
    private final Set<Integer> mines;

    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private int flagCount = 0;

    // creates a board with numMines placed at random
    public Board(int rows, int columns, int numMines, Random rand) {
        this(rows, columns, randomMines(rows * columns, numMines, rand));
    }

    // creates a board with mines at the given cell indices (used by tests)
    public Board(int rows, int columns, Set<Integer> mines) {
        this.rows = rows;
        this.columns = columns;
        this.mines = new HashSet<Integer>(mines);
        this.revealed = new boolean[rows][columns];
        this.flagged = new boolean[rows][columns];
    }

    private static Set<Integer> randomMines(int numCells, int numMines, Random rand) {
        Set<Integer> mines = new HashSet<Integer>();
        while (mines.size() < numMines) {
            // Set ignores duplicates, so keep adding until there are enough mines
            mines.add(rand.nextInt(numCells));
        }
        return mines;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isMine(int i, int j) {
        return mines.contains(i * columns + j);
    }

    public boolean isRevealed(int i, int j) {
        return revealed[i][j];
    }

    public boolean isFlagged(int i, int j) {
        return flagged[i][j];
    }

    public int getFlagsLeft() {
        return mines.size() - flagCount;
    }

    // number of mines in the 3x3 square centered on [i][j]
    public int getAdjacentMineCount(int i, int j) {
        int count = 0;
        for (int k = i - 1; k <= i + 1; k++) {
            for (int l = j - 1; l <= j + 1; l++) {
                if (inBounds(k, l) && isMine(k, l)) {
                    ++count;
                }
            }
        }
        return count;
    }

    // flag or unflag a hidden cell; revealed cells can't be flagged
    public void toggleFlag(int i, int j) {
        if (revealed[i][j]) {
            return;
        }
        flagged[i][j] = !flagged[i][j];
        flagCount += flagged[i][j] ? 1 : -1;
    }

    // reveal a cell; returns true if it was a mine
    // cells with no adjacent mines also reveal their neighbors (flood fill)
    public boolean reveal(int i, int j) {
        if (revealed[i][j] || flagged[i][j]) {
            return false;
        }
        if (isMine(i, j)) {
            revealed[i][j] = true;
            return true;
        }

        // iterative BFS instead of recursion, so large boards can't overflow the stack
        Deque<int[]> queue = new ArrayDeque<int[]>();
        revealed[i][j] = true;
        queue.add(new int[]{i, j});
        while (!queue.isEmpty()) {
            int[] cell = queue.remove();
            if (getAdjacentMineCount(cell[0], cell[1]) > 0) {
                continue;
            }
            for (int k = cell[0] - 1; k <= cell[0] + 1; k++) {
                for (int l = cell[1] - 1; l <= cell[1] + 1; l++) {
                    if (inBounds(k, l) && !revealed[k][l] && !flagged[k][l]) {
                        revealed[k][l] = true;
                        queue.add(new int[]{k, l});
                    }
                }
            }
        }
        return false;
    }

    // won when every non-mine cell is revealed
    public boolean isWon() {
        for (int k = 0; k < rows; k++) {
            for (int l = 0; l < columns; l++) {
                if (!revealed[k][l] && !isMine(k, l)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean inBounds(int i, int j) {
        return i >= 0 && i < rows && j >= 0 && j < columns;
    }
}
