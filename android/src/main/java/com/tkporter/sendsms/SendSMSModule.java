package com.tkporter.sendsms;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;

public class SendSMSModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final ReactApplicationContext reactContext;
    private Promise promise = null;
    private static final int REQUEST_CODE = 5235;

    public SendSMSModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "SendSMS";
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        //System.out.println("in module onActivityResult() request " + requestCode + " result " + resultCode);
        //canceled intent
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            sendCallback(false, true, false);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    public void sendCallback(Boolean completed, Boolean cancelled, Boolean error) {
        if (promise != null) {
            if (completed) {
              promise.resolve(true);
            }
            else {
              promise.reject("error");
            }
            promise = null;
        }
    }

    @ReactMethod
    public void send(ReadableMap options, final Promise promise) {
        try {
            this.promise = promise;
            new SendSMSObserver(reactContext, this, options).start();

            String body = options.hasKey("body") ? options.getString("body") : "";
            ReadableArray recipients = options.hasKey("recipients") ? options.getArray("recipients") : null;

            Intent sendIntent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(reactContext);
                sendIntent = new Intent(Intent.ACTION_SEND);
                if (defaultSmsPackageName != null){
                    sendIntent.setPackage(defaultSmsPackageName);
                }
                sendIntent.setType("text/plain");
            }else {
                sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setType("vnd.android-dir/mms-sms");
            }

            sendIntent.putExtra("sms_body", body);
            sendIntent.putExtra("exit_on_sent", true);

            //if recipients specified
            if (recipients != null) {
                //Samsung for some reason uses commas and not semicolons as a delimiter
                String separator = "; ";
                if(android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")){
                    separator = ", ";
                }
                String recipientString = "";
                int numRecipients = recipients.size();
                for (int i = 0; i < numRecipients; i++) {
                    recipientString += recipients.getString(i);
                    //Don't add separator on the last recipient
                    if (i < numRecipients - 1) {
                      recipientString += separator;
                    }
                }
                sendIntent.putExtra("address", recipientString);
            }

            reactContext.startActivityForResult(sendIntent, REQUEST_CODE, sendIntent.getExtras());
        } catch (Exception e) {
            //error!
            sendCallback(false, false, true);
            // throw e;
        }
    }

}
