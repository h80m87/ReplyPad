package com.example.replypad;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {
    //変数
    Context context;
    File file;
    EditText editText;
    Button save, list, clear, notify;
    private boolean check = false;
    public static final String PATH = "reply.txt";

    //ライフサイクル
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        editText = findViewById(R.id.et);
        save = findViewById(R.id.sv);
        list = findViewById(R.id.lst);
        clear = findViewById(R.id.clr);
        notify = findViewById(R.id.ntf);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPermission();

        list.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TITLE, "memo.txt");
            ReadResult.launch(i);
        });

        save.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TITLE, "memo.txt");
            WriteResult.launch(i);
        });

        clear.setOnClickListener(view -> editText.setText(""));

        notify.setOnClickListener(view -> createMemoNotification(check));
    }

    @Override
    protected void onResume() {
        super.onResume();

        appendText(checkFile(check));

        stopNotify(check);
    }

    @Override
    protected void onPause() {
        super.onPause();

        startNotify(check);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopNotify(check);
    }

    //メソッド
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            check = true;

        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

            check = true;

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.POST_NOTIFICATIONS)
        ) {
            new AlertDialog.Builder(this)
                    .setMessage("通知欄のメモ入力バーを使用する場合\n許可が必要です")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        RequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        Toast.makeText(getApplicationContext(), "通知の権限を許可してください"
                                , Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("NOT USE", (dialogInterface, i) -> Toast.makeText(getApplicationContext(), "権限を許可せず使用します"
                            , Toast.LENGTH_SHORT).show())
                    .create().show();
        } else {
            RequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> RequestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    check = true;

                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

                    check = true;

                } else {
                    Toast.makeText(getApplicationContext(), "権限が許可されてません", Toast.LENGTH_SHORT).show();
                }
            }
    );

    void startNotify(Boolean check) {
        if (check) {
            startForegroundService(new Intent(this, MyService.class));
        }
    }

    void stopNotify(Boolean check) {
        if (check) {
            stopService(new Intent(this, MyService.class));
        }
    }

    private boolean checkFile(boolean check) {
        if (check) {
            file = new File(context.getFilesDir(), PATH);

            return true;
        }

        return false;
    }

    private void appendText(boolean check) {
        if (check) {
            StringBuilder str = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            openFileInput(PATH), StandardCharsets.UTF_8
                    ))) {

                String line;
                while ((line = reader.readLine()) != null) {

                    str.append(line);
                    str.append(System.getProperty("line.separator"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            editText.append(str.toString());
            clearReply();
        }
    }

    private void clearReply() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(openFileOutput(PATH, Context.MODE_PRIVATE), StandardCharsets.UTF_8)
        )) {

            writer.write("");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createMemoNotification(boolean check) {
        if (check && !(editText.getText().toString().isBlank())) {

            NotificationChannel channel = new NotificationChannel(
                    "MEMO",
                    "メモ",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MEMO")
                    .setSmallIcon(R.drawable.ic_memo)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("発行されたメモ")
                    .setSilent(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(editText.getText()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManagerCompat.notify(new SecureRandom().nextInt(2147483645) + 2, builder.build());
        }
    }

    ActivityResultLauncher<Intent> ReadResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent i = result.getData();
                if(i != null) {
                    Uri title = i.getData();
                    if(title != null && result.getResultCode() == RESULT_OK) {
                        StringBuilder str = new StringBuilder();
                        try(BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        getContentResolver().openInputStream(title), StandardCharsets.UTF_8))) {
                            String line;
                            while((line = reader.readLine()) != null) {
                                str.append(line);
                                str.append(System.getProperty("line.separator"));
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        editText.setText(str.toString());
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> WriteResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent i = result.getData();
                if(i != null) {
                    Uri title = i.getData();
                    if(title != null && result.getResultCode() == RESULT_OK) {
                        try(BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(
                                        getContentResolver().openOutputStream(title,"wt"), StandardCharsets.UTF_8))) {
                            writer.write(editText.getText().toString());

                        } catch(IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
    );
}