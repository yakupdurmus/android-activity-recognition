package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionReceiver extends BroadcastReceiver {

    public static final String ACTION_ACTIVITY_UPDATE = "com.example.myapplication.ACTION_ACTIVITY_UPDATE";
    public static final String EXTRA_ACTIVITY_MESSAGE = "com.example.myapplication.EXTRA_ACTIVITY_MESSAGE";



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("TAG", "Received action: " + action);
        String message = "";

        if ("ACTIVITY_TRANSITION_ACTION".equals(action)) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    message += "Transition: " + getTransitionType(event.getTransitionType()) +
                            " for activity: " + getActivityString(event.getActivityType()) + "\n";
                }
            }
        } else if ("ACTIVITY_RECOGNITION_ACTION".equals(action)) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                message = "Activity: " + getActivityString(mostProbableActivity.getType()) +
                        " Confidence: " + mostProbableActivity.getConfidence() + "%";


            }
        }

        Log.e("TAG",message);

        Intent localIntent = new Intent(ACTION_ACTIVITY_UPDATE);
        localIntent.putExtra(EXTRA_ACTIVITY_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private String getTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    private String getActivityString(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown";
        }
    }
}