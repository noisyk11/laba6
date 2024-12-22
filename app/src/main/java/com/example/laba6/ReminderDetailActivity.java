package com.example.laba6;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReminderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_detail);

        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");

        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewMessage = findViewById(R.id.textViewMessage);

        textViewTitle.setText(title);
        textViewMessage.setText(message);
    }
}