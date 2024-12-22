package com.example.laba6;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class ReminderListActivity extends AppCompatActivity {
    private ListView listViewReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        listViewReminders = findViewById(R.id.listViewReminders);

        loadReminders();
    }

    private void loadReminders() {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        Cursor cursor = dbHelper.getReadableDatabase().query(
                ReminderDatabaseHelper.TABLE_NAME,
                new String[]{
                        ReminderDatabaseHelper.COLUMN_ID,
                        ReminderDatabaseHelper.COLUMN_TITLE,
                        ReminderDatabaseHelper.COLUMN_MESSAGE,
                        ReminderDatabaseHelper.COLUMN_DATE
                },
                null, null, null, null, null
        );

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{ReminderDatabaseHelper.COLUMN_TITLE, ReminderDatabaseHelper.COLUMN_MESSAGE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );

        listViewReminders.setAdapter(adapter);

        listViewReminders.setOnItemClickListener((parent, view, position, id) -> {
            Cursor selectedCursor = (Cursor) parent.getItemAtPosition(position);
            String title = selectedCursor.getString(selectedCursor.getColumnIndex(ReminderDatabaseHelper.COLUMN_TITLE));

            new AlertDialog.Builder(this)
                    .setMessage("Вы уверены, что хотите удалить это напоминание?")
                    .setPositiveButton("Да", (dialog, which) -> deleteReminder(title))
                    .setNegativeButton("Нет", null)
                    .show();
        });
    }

    private void deleteReminder(String title) {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        dbHelper.getWritableDatabase().delete(
                ReminderDatabaseHelper.TABLE_NAME,
                ReminderDatabaseHelper.COLUMN_TITLE + " = ?",
                new String[]{title}
        );
        loadReminders();
    }
}
