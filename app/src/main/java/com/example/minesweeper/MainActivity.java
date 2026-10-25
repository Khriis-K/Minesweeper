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
                tv.setTextColor(Color.BLACK);

                // store the cell index on the view so clicks can look it up directly
                tv.setTag(i * COLUMN_COUNT + j);
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

        // new views start blank, so draw them from the saved game state
        drawBoard();
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

    // redraw every cell and the flag count from the Board
    private void drawBoard() {
        Board board = game.board;
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = cell_tvs.get(i * COLUMN_COUNT + j);
                if (board.isRevealed(i, j) || !game.running) {
                    tv.setBackgroundColor(Color.LTGRAY);
                } else {
                    tv.setBackgroundColor(Color.GREEN);
                }

                if (board.isFlagged(i, j)) {
                    tv.setText(getString(R.string.flag));
                }
                // show mines that were hit, or all unflagged mines once the game is over
                else if (board.isMine(i, j) && (board.isRevealed(i, j) || !game.running)) {
                    tv.setText(getString(R.string.mine));
                }
                else if (board.isRevealed(i, j) && board.getAdjacentMineCount(i, j) > 0) {
                    tv.setText(String.valueOf(board.getAdjacentMineCount(i, j)));
                }
                else {
                    tv.setText("");
                }
            }
        }

        TextView flagCount = findViewById(R.id.flagCount);
        flagCount.setText(String.valueOf(board.getFlagsLeft()));
    }

    private void updateClock() {
        TextView timeView = findViewById(R.id.clock);
        timeView.setText(String.valueOf(game.clock));
    }

    // click handler for TextView
    public void onClickTV(View view){
        if (!game.running){
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("won", game.won);
            intent.putExtra("time", game.clock);
            startActivity(intent);
            return;
        }

        int n = (int) view.getTag();
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;

        if (game.isFlagging) {
            game.board.toggleFlag(i, j);
        }
        // picking; hitting a mine ends the game
        else if (game.board.reveal(i, j)) {
            game.running = false;
        }
        // all non-mine cells are revealed
        else if (game.board.isWon()) {
            game.won = true;
            game.running = false;
        }

        drawBoard();
    }
}
