package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;


public class NFCRead extends AppCompatActivity implements Serializable {

    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter readTagFilters[];
    Tag mytag;
    NdefMessage tagContents;
    Context ctx;
    ArrayList<String> messageArray;
    String takeAction="", playerType="", owner="", theMessage ="", colour="", id="", type="", retheMessage = "", reowner = "", reid = "",
            recolour = "", retype = "", retakeAction = "", speakInstruction;
    boolean commit = false, errorOccurred = false, posession = false, readMode = false, caught = false;
    ReadTagFragment readTagFragment;
    ShowTagContentFragment showTagContentFragment;
    ListView storyList;
    File[] filesOnTag;
    FragmentTransaction ft;
    int mode;


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_nfc);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getSupportActionBar().setTitle("Play Tagged Story");
        InitFragments();
        NFCSetup();

//        storyList = (ListView) findViewById(R.id.list_view);
    }

    void InitFragments() {

        readTagFragment = new ReadTagFragment();
        showTagContentFragment = new ShowTagContentFragment();
        //Open first fragment in the fragment array list
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, readTagFragment);
        ft.commit();
    }

    public void NFCSetup(){
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        readTagFilters = new IntentFilter[] { tagDetected };
    }

    private void read(Tag tag) throws IOException, FormatException, IndexOutOfBoundsException, NullPointerException {

        String s = null;
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        tagContents = ndef.getNdefMessage();

        // get NDEF message details
        NdefMessage ndefMesg = ndef.getCachedNdefMessage();
        NdefRecord[] ndefRecords = ndefMesg.getRecords();
        int len = ndefRecords.length;

        byte[] mesg = null;
        String[] recTypes = new String[len];     // will contain the NDEF record types
        for (int i = 0; i < len; i++)
        {
            recTypes[i] = new String(ndefRecords[i].getType());
            mesg = ndefRecords[i].getPayload();
            s = new String(mesg);
        }

        s = s.substring(3);

        PackageManager m = getPackageManager();
        String packageName = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(packageName, 0);
            packageName = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("yourtag", "Error Package name not found ", e);
        }

        ndef.close();

//        String path = packageName.toString()+"/files";
        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+s;
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }

        filesOnTag = files;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, "Tag Discovered", Toast.LENGTH_LONG ).show();
        }


        try {
            if (mytag == null) {

                takeAction = "Do Nothing";
                Toast.makeText(this, "Tag Null", Toast.LENGTH_LONG ).show();
            }

            else {
//                if(filesOnTag.length>0) {
//
//                    filesOnTag = null;
//                }
                read(mytag);
                Toast.makeText(this, "Tag Read", Toast.LENGTH_LONG ).show();

                Bundle bundle = new Bundle();
                bundle.putInt("Orientation", mode);
                bundle.putSerializable("filesOnTag", filesOnTag);
                showTagContentFragment.setArguments(bundle);
                ft = getSupportFragmentManager().beginTransaction();
                ft.detach(showTagContentFragment);
                ft.attach(showTagContentFragment);
                ft.replace(R.id.fragment_frame, showTagContentFragment);
                ft.commit();
            }

        } catch (IOException e) {
           // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            errorOccurred = true;
            System.out.println("Fail 1");
        } catch (FormatException e) {
           // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            errorOccurred = true;
            System.out.println("Fail 2");
        } catch (IndexOutOfBoundsException e) {
           // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            errorOccurred = true;
            System.out.println("Fail 3");
        } catch (NullPointerException e) {
           // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            errorOccurred = true;
            System.out.println("Fail 4");
        }

    }

    @Override
    public void onPause(){
        super.onPause();
        adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(NFCRead.this, MainMenu.class);
        intent.putExtra("Orientation", mode);
        NFCRead.this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}