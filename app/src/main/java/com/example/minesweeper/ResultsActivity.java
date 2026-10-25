package com.example.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        boolean won = intent.getBooleanExtra("won", false);
        String time = String.valueOf(intent.getIntExtra("time", 0));

        TextView timeUsedTextView = findViewById(R.id.timeUsedTextView);
        String timeUsed = "Used " + time + " seconds.";
        timeUsedTextView.setText(timeUsed);

        TextView winLossTextView = findViewById(R.id.winLossTextView);
        if (won) {
            String winMessage = "You won.\nGood job!";
            winLossTextView.setText(winMessage);
        }
        else {
            String lossMessage = "You lost.\nTry again!";
            winLossTextView.setText(lossMessage);
        }
    }

    public void backToMain(View view) {
        // return to the existing MainActivity instead of stacking a new one on top;
        // CLEAR_TOP finishes this screen and the old game, then starts a fresh MainActivity
        // (the finished Activity's ViewModel is cleared, so a new board is created)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}