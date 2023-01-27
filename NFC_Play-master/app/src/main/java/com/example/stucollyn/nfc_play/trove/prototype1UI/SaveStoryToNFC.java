package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SaveStoryToNFC extends AppCompatActivity {

    String action;
    String themessage;
    String owner;
    String type;
    String id;
    String colour;
    TextView instruction;
    static String display;
    boolean success = true;
    Tag mytag;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
   // File fileDirectory;
    String tag_data = "";
    int mode;
    Animation nfc_transmit_animation;
    ImageView nfc_transmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_story_to_nfc);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Save New Story To NFC");
       // fileDirectory = (File)getIntent().getExtras().get("StoryDirectory");
        tag_data = (String)getIntent().getExtras().get("TagData");

       // Log.i("FD: ", fileDirectory.toString());
        nfc_transmit = findViewById(R.id.nfc_transmit);
        nfc_transmit_animation = AnimationUtils.loadAnimation(this, R.anim.shrink);
        nfc_transmit.startAnimation(nfc_transmit_animation);

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    private void write(Tag tag) throws IOException, FormatException {

//        String fileToWrite = fileDirectory.getAbsolutePath();
        String fileToWrite = tag_data;
        NdefRecord[] records = {
                createRecord(fileToWrite)
        };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;

        // set status byte (see NDEF spec for actual bits)
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        //copy langbytes and textbytes into payload
        System.arraycopy(textBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);


        return recordNFC;
    }

    @Override
    protected void onNewIntent(Intent intent){
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, "Tag Discovered", Toast.LENGTH_LONG ).show();
        }

        doWrite();
    }

    public void doWrite(){

        try {
            if(mytag==null){
                Toast.makeText(this, "Error Detected", Toast.LENGTH_LONG ).show();
//                Toast.makeText(this, this.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
                success = false;
            }else{
                //write(message.getText().toString(),mytag);
                write(mytag);
                Toast.makeText(this, "Okay Writing", Toast.LENGTH_LONG ).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error Detected", Toast.LENGTH_LONG ).show();
            e.printStackTrace();
            success = false;
        } catch (FormatException e) {
            Toast.makeText(this, "Error Detected", Toast.LENGTH_LONG ).show();
            e.printStackTrace();
            success = false;
        }

        if(success) {

//            writeinstruction.setText("Object written successfully. Please press continue.");
//            continueButton.setVisibility(View.VISIBLE);

            Intent intent = new Intent(SaveStoryToNFC.this, SavedStoryConfirmation.class);
            intent.putExtra("Orientation", mode);
            SaveStoryToNFC.this.startActivity(intent);
            overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
        }

        else {

//            writeinstruction.setText("Error writing object. Please rescan.");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn(){
//        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
//        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }
}
