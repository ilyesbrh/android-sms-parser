package com.joaquimley.smsparsing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "test";
    private EditText editTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextInput = findViewById(R.id.edit_text_input);
        editTextInput.setText(ReadFromFile("phoneNumber"));

    }

    public void create(View v) {
        String input = editTextInput.getText().toString();

        Intent serviceIntent = new Intent(this, SMSService.class);
        serviceIntent.putExtra("inputExtra", input);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void cancel(View v) {
        Intent serviceIntent = new Intent(this, SMSService.class);
        stopService(serviceIntent);
    }

    public void sendMessege(View v) {

        WriteToFile("phoneNumber", editTextInput.getText().toString());
    }


    private void handleSmsSending(String msg) {
        try {
            sendSms(this, "09000000000", msg);

            Log.d(TAG, "Sent the test sms");
        } catch (Exception e) {
            Log.d(TAG, "Exception : " + e.getClass() + " : " + e.getMessage());
        }

    }

    private void sendSms(Context context, String sender, String body) throws Exception {
        byte[] pdu = null;
        byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
        byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte[] dateBytes = new byte[7];
        Calendar calendar = new GregorianCalendar();
        dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
        dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
        dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
        dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
        dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) +
                calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        bo.write(lsmcs);
        bo.write(scBytes);
        bo.write(0x04);
        bo.write((byte) sender.length());
        bo.write(senderBytes);
        bo.write(0x00);
        bo.write(0x00);
        bo.write(dateBytes);

        String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
        Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
        Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod("stringToGsm7BitPacked", String.class);
        stringToGsm7BitPacked.setAccessible(true);
        byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null, body);
        bo.write(bodybytes);
        pdu = bo.toByteArray();

        // broadcast the SMS_RECEIVED to registered receivers
        broadcastSmsReceived(context, pdu);

        // or, directly send the message into the inbox and let the usual SMS handling happen - SMS appearing in Inbox, a notification with sound, etc.
        startSmsReceiverService(context, pdu);
    }

    private void broadcastSmsReceived(Context context, byte[] pdu) {
        Intent intent = new Intent();
        intent.setAction("android.provider.Telephony.SMS_RECEIVED");
        intent.putExtra("pdus", new Object[]{pdu});
        context.sendBroadcast(intent);
    }

    private void startSmsReceiverService(Context context, byte[] pdu) {
        Intent intent = new Intent();
        intent.setClassName("com.android.mms", "com.android.mms.transaction.SmsReceiverService");
        intent.setAction("android.provider.Telephony.SMS_RECEIVED");
        intent.putExtra("pdus", new Object[]{pdu});
        intent.putExtra("format", "3gpp");
        context.startService(intent);
    }

    public void WriteToFile(String fileName, String s) {

        FileOutputStream out = null;
        try {
            out = openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter sw = new OutputStreamWriter(out);
            sw.write(s);
            sw.flush();
            out.close();
        } catch (Exception e) {

        }
    }

    public String ReadFromFile(String fileName) {

        try {
            FileInputStream in = openFileInput(fileName);
            InputStreamReader sr = new InputStreamReader(in);
            String s = "";
            char[] charTab = new char[100];

            int read = sr.read(charTab);

            if (read > 0) {
                return String.copyValueOf(charTab, 0, read);
            }
            return s;

        } catch (Exception e) {
            return new String("");
        }
    }

    private byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }

}