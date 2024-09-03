package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button getActivityRequest;
    TextView activityText;

    private ActivityRecognitionReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getActivityRequest = findViewById(R.id.getActivityRequest);
        activityText = findViewById(R.id.activity_text);


        getActivityRequest.setOnClickListener(view -> {
            String[] activity = {Manifest.permission.ACTIVITY_RECOGNITION};
            String[] foreground = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            String[] background = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};


            boolean perm1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PermissionChecker.PERMISSION_DENIED;
            boolean perm2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_DENIED;
            boolean perm3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_DENIED;
            boolean perm4 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PermissionChecker.PERMISSION_DENIED;


            if (perm2 || perm3) {
                Log.e("tag", "2");
                ActivityCompat.requestPermissions(this, foreground, 200);
            } else if (perm4) {
                Log.e("tag", "3");
                ActivityCompat.requestPermissions(this, background, 300);
            } else if (perm1) {
                Log.e("tag", "1");
                ActivityCompat.requestPermissions(this, activity, 100);
            }else{
                Log.e("TAG","permission ok");

                receiver = new ActivityRecognitionReceiver();
                registerReceiver(receiver, new IntentFilter("ACTIVITY_RECOGNITION_DATA"));

                requestActivityRecognitionUpdates();
            }

        });
    }

    private void requestActivityRecognitionUpdates() {
        Intent intent = new Intent(this, ActivityRecognitionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 550, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
         Log.e("TAG","permission error");
            return;
        }

        ActivityRecognition.getClient(this).requestActivityUpdates(3000, pendingIntent);

        Log.e("TEST","---->");
    }

}