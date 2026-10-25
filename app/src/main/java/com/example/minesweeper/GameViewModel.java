package com.example.minesweeper;

import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

// Holds all game state so it survives configuration changes (e.g. rotation).
// The Activity is destroyed and recreated on rotation, but the ViewModel is kept.
public class GameViewModel extends ViewModel {

    static final int COLUMN_COUNT = 10;
    static final int ROW_COUNT = 10;
    static final int NUM_BOMBS = 5;

    final Set<Integer> mines = new HashSet<Integer>();

    // 2D revealed and flagged arrays
    final boolean[][] revealed = new boolean[ROW_COUNT][COLUMN_COUNT];
    final boolean[][] flagged = new boolean[ROW_COUNT][COLUMN_COUNT];

    int clock = 0;
    boolean running = true;

    // false = picking, true = flagging
    boolean isFlagging = false;

    boolean won = false;

    int flagsLeft = NUM_BOMBS;

    public GameViewModel() {
        // generate unique numbers between 0 and 99 and store in Set mines
        Random rand = new Random();
        while (mines.size() < NUM_BOMBS) {
            // Set ignores duplicates, so keep adding until there are enough mines
            mines.add(rand.nextInt(ROW_COUNT * COLUMN_COUNT));
        }
    }
}
