package com.example.laba6;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // Объявление переменных для UI-элементов
    private EditText editTextTitle, editTextMessage;
    private Button buttonSetDate, buttonSetTime, buttonSaveReminder, buttonViewReminders;
    private Calendar reminderDate;

    // Метод, который выполняется при запуске Activity
    @SuppressLint("MissingInflatedId")  // Отключение предупреждения для поиска элементов по ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Установка layout для активити
        // Инициализация UI-элементов
        editTextTitle = findViewById(R.id.editTextTitle);  // Заголовок напоминания
        editTextMessage = findViewById(R.id.editTextMessage);  // Сообщение напоминания
        buttonSetDate = findViewById(R.id.buttonSetDate);  // Кнопка для установки даты
        buttonSetTime = findViewById(R.id.buttonSetTime);  // Кнопка для установки времени
        buttonSaveReminder = findViewById(R.id.buttonSaveReminder);  // Кнопка для сохранения напоминания
        buttonViewReminders = findViewById(R.id.buttonViewReminders);  // Кнопка для просмотра списка напоминаний

        // Инициализация календаря для напоминания (по умолчанию текущее время)
        reminderDate = Calendar.getInstance();

        // Установка обработчиков для кнопок
        buttonSetDate.setOnClickListener(v -> showDatePicker());  // Открытие DatePicker
        buttonSetTime.setOnClickListener(v -> showTimePicker());  // Открытие TimePicker
        buttonSaveReminder.setOnClickListener(v -> saveReminder());  // Сохранение напоминания
        buttonViewReminders.setOnClickListener(v -> {
            // Переход на экран с отображением списка напоминаний
            Intent intent = new Intent(MainActivity.this, ReminderListActivity.class);
            startActivity(intent);
        });
    }

    // Метод для отображения диалога выбора даты
    private void showDatePicker() {
        // Создаем DatePickerDialog для выбора даты
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Устанавливаем выбранную дату в объект Calendar
                    reminderDate.set(Calendar.YEAR, year);
                    reminderDate.set(Calendar.MONTH, month);
                    reminderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                },
                reminderDate.get(Calendar.YEAR),  // Текущий год
                reminderDate.get(Calendar.MONTH),  // Текущий месяц
                reminderDate.get(Calendar.DAY_OF_MONTH)  // Текущий день
        );
        // Показываем диалог
        datePickerDialog.show();
    }

    // Метод для отображения диалога выбора времени
    private void showTimePicker() {
        // Создаем TimePickerDialog для выбора времени
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Устанавливаем выбранное время в объект Calendar
                    reminderDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderDate.set(Calendar.MINUTE, minute);
                },
                reminderDate.get(Calendar.HOUR_OF_DAY),  // Текущий час
                reminderDate.get(Calendar.MINUTE),  // Текущая минута
                true  // Используем 24-часовой формат
        );
        // Показываем диалог
        timePickerDialog.show();
    }

    // Метод для сохранения напоминания
    private void saveReminder() {
        String title = editTextTitle.getText().toString();  // Заголовок напоминания
        String message = editTextMessage.getText().toString();  // Сообщение напоминания
        long date = reminderDate.getTimeInMillis();  // Время напоминания в миллисекундах

        // Проверка на заполненность полей
        if (title.isEmpty() || message.isEmpty()) {
            // Показываем уведомление, если поля пустые
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Подготовка данных для сохранения в базу данных
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        ContentValues values = new ContentValues();
        values.put(ReminderDatabaseHelper.COLUMN_TITLE, title);
        values.put(ReminderDatabaseHelper.COLUMN_MESSAGE, message);
        values.put(ReminderDatabaseHelper.COLUMN_DATE, String.valueOf(date));

        // Вставка данных в базу данных
        long newRowId = dbHelper.getWritableDatabase().insert(ReminderDatabaseHelper.TABLE_NAME, null, values);

        // Проверка успешности вставки
        if (newRowId != -1) {
            // Если напоминание сохранено, показываем уведомление и устанавливаем будильник
            Toast.makeText(this, "Напоминание сохранено", Toast.LENGTH_SHORT).show();
            setReminderAlarm(date, title, message);  // Устанавливаем напоминание с будильником
        } else {
            // Если возникла ошибка при сохранении
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для установки напоминания с использованием AlarmManager
    private void setReminderAlarm(long reminderTime, String title, String message) {
        // Получаем системный сервис AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Toast.makeText(this, "Не удалось получить AlarmManager", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка разрешения для установки точных будильников
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // Создаем Intent для передачи данных в приемник (Receiver)
                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("title", title);
                intent.putExtra("message", message);

                // Создаем PendingIntent, который будет отправлен AlarmManager для срабатывания будильника
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) reminderTime,  // Используем время напоминания в качестве уникального ID
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Устанавливаем точное время для напоминания
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                // Если разрешение не предоставлено, показываем предупреждение
                Toast.makeText(this, "Разрешение на установку точных будильников не предоставлено.", Toast.LENGTH_SHORT).show();
                // Направляем пользователя в настройки для предоставления разрешения
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        } else {
            // Для старых версий Android можно устанавливать точные будильники без дополнительных проверок
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) reminderTime,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }
}
