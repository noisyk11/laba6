package com.example.laba6;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        Log.d("ReminderReceiver", "onReceive called with title: " + title + ", message: " + message);

        // Создаем Intent для запуска активности при нажатии на уведомление
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Строим уведомление
        Notification notification = new NotificationCompat.Builder(context, "reminder_channel")  // Канал уведомлений
                .setContentTitle("Напоминание: " + title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)  // Замените на свой ресурс иконки
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Приоритет уведомления
                .setAutoCancel(true)  // Уведомление будет автоматически удалено по клику
                .setContentIntent(pendingIntent)  // Создание PendingIntent для перехода в MainActivity
                .build();

        // Получаем NotificationManager и показываем уведомление
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Проверка, чтобы уведомления работали на устройствах с Android 8.0 (API 26) и выше
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Notifications";
            String description = "Channel for reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
            channel.setDescription(description);
            // Регистрация канала
            notificationManager.createNotificationChannel(channel);
        }

        // Показываем уведомление
        notificationManager.notify(1, notification);  // Уникальный ID для уведомления
    }
}
