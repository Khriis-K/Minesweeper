package com.example.minesweeper;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.ViewModelProvider;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = GameViewModel.COLUMN_COUNT;
    private static final int ROW_COUNT = GameViewModel.ROW_COUNT;

    // game state lives in the ViewModel so it survives rotation
    private GameViewModel game;

    private ArrayList<TextView> cell_tvs;

    // handler will be used to run the timer; tied to the main thread's Looper
    private final Handler handler = new Handler(Looper.getMainLooper());

    // timer tick, posted every second while the Activity is in the foreground
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (game.running) {
                game.clock++;
            }
            updateClock();
            // run again after 1 second
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // entry point for application
        super.onCreate(savedInstanceState);
        // load XML layout
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // returns the existing ViewModel after a rotation, or creates a new game on first launch
        game = new ViewModelProvider(this).get(GameViewModel.class);

        cell_tvs = new ArrayList<TextView>();

        GridLayout grid = findViewById(R.id.gridLayout01);
        grid.post(() -> {
            // use screen width to make grid square
            grid.getLayoutParams().height = grid.getWidth();
            grid.requestLayout();
        });

        LayoutInflater li = LayoutInflater.from(this);
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
                tv.setBackgroundColor(Color.GREEN);
                tv.setTextColor(Color.BLACK);

                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i, 1f);
                lp.columnSpec = GridLayout.spec(j, 1f);
                lp.width = 0;
                lp.height = 0;
                tv.setLayoutParams(lp);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }

        // set click handler for modeIcon
        TextView modeIcon = findViewById(R.id.modeIcon);
        modeIcon.setText(game.isFlagging ? R.string.flag : R.string.pick);
        modeIcon.setOnClickListener(v -> {
            if (game.isFlagging) {
                modeIcon.setText(R.string.pick);
                game.isFlagging = false;
            } else {
                modeIcon.setText(R.string.flag);
                game.isFlagging = true;
            }
        });

        // new views start blank, so redraw them from the saved game state
        restoreBoard();
        updateFlagCount();
        updateClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start ticking only while visible
        handler.postDelayed(tick, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop the timer so the Runnable doesn't keep this Activity alive after it's gone
        handler.removeCallbacks(tick);
    }

    // redraw every cell from the ViewModel, used after the Activity is recreated
    private void restoreBoard() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = cell_tvs.get(i * COLUMN_COUNT + j);
                if (game.revealed[i][j]) {
                    tv.setBackgroundColor(Color.LTGRAY);
                    if (game.mines.contains(i * COLUMN_COUNT + j)) {
                        tv.setText(getString(R.string.mine));
                    } else {
                        int numAdjacentMines = getNumberOfAdjacentMines(i, j);
                        if (numAdjacentMines > 0) {
                            tv.setText(String.valueOf(numAdjacentMines));
                        }
                    }
                } else if (game.flagged[i][j]) {
                    tv.setText(getString(R.string.flag));
                }
            }
        }
        if (!game.running) {
            revealAllCells();
        }
    }

    private void updateFlagCount() {
        TextView flagCount = findViewById(R.id.flagCount);
        flagCount.setText(String.valueOf(game.flagsLeft));
    }

    private void updateClock() {
        TextView timeView = findViewById(R.id.clock);
        timeView.setText(String.valueOf(game.clock));
    }

    // helper function to find index of TextView in ArrayList
    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    // click handler for TextView
    public void onClickTV(View view){
        TextView tv = (TextView) view;
        if (!game.running){
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("won", game.won);
            intent.putExtra("time", game.clock);
            startActivity(intent);
            return;
        }

        if (!tv.isEnabled())
            return;

        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        // check if already revealed
        if (!game.revealed[i][j]) {
            // check if flagging or picking
            if (game.isFlagging) {
                // unflag the cell
                if (game.flagged[i][j]) {
                    tv.setText("");
                    game.flagged[i][j] = false;
                    game.flagsLeft++;
                }
                // flag the cell
                else {
                    tv.setText(getString(R.string.flag));
                    game.flagged[i][j] = true;
                    game.flagsLeft--;
                }
                updateFlagCount();
            }
            // picking
            else{
                // prohibit mining flagged cells
                if (game.flagged[i][j]) {
                    return;
                }

                game.revealed[i][j] = true;
                tv.setBackgroundColor(Color.LTGRAY);

                // check if mine
                if (game.mines.contains(i * COLUMN_COUNT + j)) {
                    tv.setText(getString(R.string.mine));
                    // end game
                    endGame();
                    return;
                }

                // update with number of adjacent mines
                int numAdjacentMines = getNumberOfAdjacentMines(i, j);
                if (numAdjacentMines > 0) {
                    tv.setText(String.valueOf(numAdjacentMines));
                }
                // number of adjacent mines is 0, so reveal all adjacent cells
                else{
                    for (int k = i - 1; k <= i + 1; k++) {
                        for (int l = j - 1; l <= j + 1; l++) {
                            if (k >= 0 && k < ROW_COUNT && l >= 0 && l < COLUMN_COUNT) {
                                if (!game.revealed[k][l]) {
                                    onClickTV(cell_tvs.get(k * COLUMN_COUNT + l));
                                }
                            }
                        }
                    }
                }

                // check if all non-mine cells are revealed
                for (int k = 0; k < ROW_COUNT; k++) {
                    for (int l = 0; l < COLUMN_COUNT; l++) {
                        if (!game.revealed[k][l] && !game.mines.contains(k * COLUMN_COUNT + l)) {
                            return;
                        }
                    }
                }
                // all non-mine cells are revealed
                game.won = true;
                endGame();
            }
        }
    }

    // helper function to get number of adjacent mines
    // i and j are the indices of the cell that was clicked
    private int getNumberOfAdjacentMines(int i, int j){
        // 3x3 matrix, [i][j] is the center cell
        int count = 0;
        for (int k = i - 1; k <= i + 1; k++) {
            for (int l = j - 1; l <= j + 1; l++) {
                if (k >= 0 && k < ROW_COUNT && l >= 0 && l < COLUMN_COUNT) {
                    if (game.mines.contains(k * COLUMN_COUNT + l)) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    private void endGame(){
        game.running = false;
        revealAllCells();
    }

    // reveal all cells; don't replace flagged cells with mines
    private void revealAllCells() {
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                cell_tvs.get(i * COLUMN_COUNT + j).setBackgroundColor(Color.LTGRAY);
                if (!game.flagged[i][j] && game.mines.contains(i * COLUMN_COUNT + j)) {
                    cell_tvs.get(i * COLUMN_COUNT + j).setText(getString(R.string.mine));
                }
            }
        }
    }
}
