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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button getActivityRequest;
    TextView activityText;

    private ActivityRecognitionReceiver receiver;

    private boolean runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private BroadcastReceiver activityUpdateReceiver;


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
        receiver = new ActivityRecognitionReceiver();
        registerReceiver(receiver, new IntentFilter("ACTIVITY_RECOGNITION_DATA"));



        getActivityRequest.setOnClickListener(view -> {
            String[] activity = {Manifest.permission.ACTIVITY_RECOGNITION};
            String[] foreground = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            String[] background = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};


            boolean perm1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PermissionChecker.PERMISSION_DENIED && runningQOrLater;
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
            } else {
                Log.e("TAG", "permission ok");


                requestActivityRecognitionUpdates();
            }

        });

        activityUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ActivityRecognitionReceiver.ACTION_ACTIVITY_UPDATE)) {
                    String message = intent.getStringExtra(ActivityRecognitionReceiver.EXTRA_ACTIVITY_MESSAGE);
                    activityText.setText(message);
                }
            }
        };
    }

        @Override
    protected void onResume() {
        super.onResume();
        // Register the local broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
                activityUpdateReceiver,
                new IntentFilter(ActivityRecognitionReceiver.ACTION_ACTIVITY_UPDATE)
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityUpdateReceiver);
    }

    private ActivityTransitionRequest setTransistor() {
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());


        return new ActivityTransitionRequest(transitions);


    }

    private void requestActivityRecognitionUpdates() {
        Intent transitionIntent = new Intent(this, ActivityRecognitionReceiver.class);
        transitionIntent.setAction("ACTIVITY_TRANSITION_ACTION");
        PendingIntent transitionPendingIntent = PendingIntent.getBroadcast(this, 0, transitionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        Intent recognitionIntent = new Intent(this, ActivityRecognitionReceiver.class);
        recognitionIntent.setAction("ACTIVITY_RECOGNITION_ACTION");
        PendingIntent recognitionPendingIntent = PendingIntent.getBroadcast(this, 1, recognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("TAG", "permission error");
            return;
        }

        ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(setTransistor(), transitionPendingIntent)
                .addOnSuccessListener(result -> Log.e("TEST", "Activity transition update request successful"))
                .addOnFailureListener(e -> Log.e("TEST", "Failed to request activity transition updates", e));

        ActivityRecognition.getClient(this)
                .requestActivityUpdates(5000, recognitionPendingIntent)
                .addOnSuccessListener(result -> Log.e("TEST", "Activity recognition update request successful"))
                .addOnFailureListener(e -> Log.e("TEST", "Failed to request activity recognition updates", e));
    }

}