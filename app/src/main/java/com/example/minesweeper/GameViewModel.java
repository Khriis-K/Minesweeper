package com.example.minesweeper;

import androidx.lifecycle.ViewModel;

import java.util.Random;

// Holds all game state so it survives configuration changes (e.g. rotation).
// The Activity is destroyed and recreated on rotation, but the ViewModel is kept.
public class GameViewModel extends ViewModel {

    static final int COLUMN_COUNT = 10;
    static final int ROW_COUNT = 10;
    static final int NUM_BOMBS = 5;

    final Board board = new Board(ROW_COUNT, COLUMN_COUNT, NUM_BOMBS, new Random());

    int clock = 0;
    boolean running = true;

    // false = picking, true = flagging
    boolean isFlagging = false;

    boolean won = false;
}
