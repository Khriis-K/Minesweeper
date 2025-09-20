package com.example.minesweeper;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.gridlayout.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 10;

    private static final int NUM_BOMBS = 5;

    private Set<Integer> mines;

    private ArrayList<TextView> cell_tvs;

    private int clock = 0;
    private boolean running = true;

    // true = picking, false = flagging
    private boolean isFlagging = false;

    // helper function to convert dp to pixels
    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

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

        // generate 5 unique numbers between 0 and 99 and store in Set mines
        Random rand = new Random();
        int bombNum = rand.nextInt(ROW_COUNT * COLUMN_COUNT);
        mines = new HashSet<Integer>();
        while (mines.size() < NUM_BOMBS) {
            while (mines.contains(bombNum)) {
                bombNum = rand.nextInt(ROW_COUNT * COLUMN_COUNT);
            }
            mines.add(rand.nextInt(ROW_COUNT * COLUMN_COUNT));
        }

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

        // load clock value from savedInstanceState if it exists
        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }
        runTimer();

        TextView modeIcon = findViewById(R.id.modeIcon);
        modeIcon.setOnClickListener(v -> {
            if (isFlagging) {
                modeIcon.setText(R.string.pick);
                isFlagging = false;
            } else {
                modeIcon.setText(R.string.flag);
                isFlagging = true;
            }
        });
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
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
        if (!tv.isEnabled())
            return;

        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT;
        int j = n%COLUMN_COUNT;
//        tv.setText(String.valueOf(i)+String.valueOf(j));

//        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.LTGRAY);
        // check if mine
        if (mines.contains(i * COLUMN_COUNT + j)) {
            tv.setText(getString(R.string.mine));
            tv.setEnabled(false);
        }

        tv.setEnabled(false);
//        if (tv.getCurrentTextColor() == Color.GRAY) {
//            tv.setTextColor(Color.GREEN);
//            tv.setBackgroundColor(Color.parseColor("lime"));
//        }else {
//            tv.setTextColor(Color.GRAY);
//            tv.setBackgroundColor(Color.LTGRAY);
//        }
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.clock);
        // handler will be used to run the timer
        final Handler handler = new Handler();

        // run timer every second
        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = clock%60;
                @SuppressLint("DefaultLocale") String time = String.format("%02d", seconds);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                // run again after 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }
}