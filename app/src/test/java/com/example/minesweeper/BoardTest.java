package com.example.minesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BoardTest {

    // mines are given as (row, column) pairs
    private static Board boardWithMines(int rows, int columns, int[]... mineCells) {
        Set<Integer> mines = new HashSet<Integer>();
        for (int[] cell : mineCells) {
            mines.add(cell[0] * columns + cell[1]);
        }
        return new Board(rows, columns, mines);
    }

    @Test
    public void adjacentCount_cornerMine() {
        Board board = boardWithMines(3, 3, new int[]{0, 0});
        assertEquals(1, board.getAdjacentMineCount(0, 1));
        assertEquals(1, board.getAdjacentMineCount(1, 1));
        assertEquals(0, board.getAdjacentMineCount(2, 2));
    }

    @Test
    public void adjacentCount_surroundedByEightMines() {
        Board board = boardWithMines(3, 3,
                new int[]{0, 0}, new int[]{0, 1}, new int[]{0, 2},
                new int[]{1, 0},                  new int[]{1, 2},
                new int[]{2, 0}, new int[]{2, 1}, new int[]{2, 2});
        assertEquals(8, board.getAdjacentMineCount(1, 1));
    }

    @Test
    public void adjacentCount_edgeCellsDontGoOutOfBounds() {
        Board board = boardWithMines(3, 3, new int[]{1, 1});
        assertEquals(1, board.getAdjacentMineCount(0, 0));
        assertEquals(1, board.getAdjacentMineCount(2, 2));
        assertEquals(1, board.getAdjacentMineCount(0, 2));
    }

    @Test
    public void reveal_mineReturnsTrue() {
        Board board = boardWithMines(3, 3, new int[]{1, 1});
        assertTrue(board.reveal(1, 1));
        assertTrue(board.isRevealed(1, 1));
    }

    @Test
    public void reveal_numberedCellRevealsOnlyItself() {
        Board board = boardWithMines(3, 3, new int[]{0, 0});
        assertFalse(board.reveal(1, 1));
        assertTrue(board.isRevealed(1, 1));
        assertFalse(board.isRevealed(2, 2));
    }

    @Test
    public void reveal_zeroCellFloodFillsToNumberBorder() {
        // mine in the top-left corner; revealing the far corner should open everything else
        Board board = boardWithMines(4, 4, new int[]{0, 0});
        assertFalse(board.reveal(3, 3));
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals("cell " + i + "," + j, !(i == 0 && j == 0), board.isRevealed(i, j));
            }
        }
    }

    @Test
    public void reveal_floodFillSkipsFlaggedCells() {
        Board board = boardWithMines(4, 4, new int[]{0, 0});
        board.toggleFlag(2, 2);
        board.reveal(3, 3);
        assertFalse(board.isRevealed(2, 2));
        assertTrue(board.isFlagged(2, 2));
    }

    @Test
    public void reveal_flaggedCellIsIgnored() {
        Board board = boardWithMines(3, 3, new int[]{1, 1});
        board.toggleFlag(1, 1);
        assertFalse(board.reveal(1, 1));
        assertFalse(board.isRevealed(1, 1));
    }

    @Test
    public void isWon_falseAtStart() {
        Board board = boardWithMines(3, 3, new int[]{0, 0});
        assertFalse(board.isWon());
    }

    @Test
    public void isWon_trueWhenAllSafeCellsRevealed() {
        Board board = boardWithMines(2, 2, new int[]{0, 0});
        board.reveal(0, 1);
        board.reveal(1, 0);
        assertFalse(board.isWon());
        board.reveal(1, 1);
        assertTrue(board.isWon());
    }

    @Test
    public void isWon_doesNotRequireFlags() {
        Board board = boardWithMines(4, 4, new int[]{0, 0});
        board.reveal(3, 3);
        assertTrue(board.isWon());
        assertFalse(board.isFlagged(0, 0));
    }

    @Test
    public void toggleFlag_updatesFlagsLeft() {
        Board board = boardWithMines(3, 3, new int[]{0, 0}, new int[]{2, 2});
        assertEquals(2, board.getFlagsLeft());
        board.toggleFlag(1, 1);
        assertEquals(1, board.getFlagsLeft());
        board.toggleFlag(1, 1);
        assertEquals(2, board.getFlagsLeft());
    }

    @Test
    public void toggleFlag_revealedCellCannotBeFlagged() {
        Board board = boardWithMines(3, 3, new int[]{0, 0});
        board.reveal(1, 1);
        board.toggleFlag(1, 1);
        assertFalse(board.isFlagged(1, 1));
    }

    @Test
    public void randomBoard_placesExactMineCount() {
        Board board = new Board(10, 10, 5, new Random(42));
        int count = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.isMine(i, j)) {
                    count++;
                }
            }
        }
        assertEquals(5, count);
        assertEquals(5, board.getFlagsLeft());
    }
}
