package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.stucollyn.nfc_play.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by StuCollyn on 14/05/2018.
 */

public class ShowTagContentFragment extends Fragment {


    FragmentActivity listener;
    ImageView nfc_transmit, nfc_tag_icon;
    ListView listView;
    Button pauseNplay;
    Animation nfc_transmit_animation;
    MediaPlayer mp = new MediaPlayer();
    boolean mpPlaying = false;
    boolean pauseNplayVisibility = false;
    boolean pauseOrplay = false;
    LinearLayout linearLayout;
    View view;
    File[] filesOnTag;
    File[] filesArray;
    File currentFile;
    int currentInt = 0;
    ArrayList<ImageButton> buttonList = new ArrayList<ImageButton>();
    HashMap<ImageButton, File> buttonToFileMap = new HashMap<ImageButton, File>();
    HashMap<ImageButton, String> buttonStringHashMap = new HashMap<ImageButton, String>();
    String callActivityName;
    int mode;

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
            ContactsContract.Data.DISPLAY_NAME};

    // This is the select criteria
    static final String SELECTION = "((" +
            ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
            ContactsContract.Data.DISPLAY_NAME + " != '' ))";

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (AppCompatActivity) context;
        }
    }

    // This event fires 2nd, before views are created for the fragment
    // The onCreate method is called when the Fragment instance is being created, or re-created.
    // Use onCreate for any standard setup that does not require the activity to be fully created
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.show_content_fragment, parent, false);
        return inflater.inflate(R.layout.show_content_fragment, parent, false);

    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        Bundle bundle = this.getArguments();
        filesOnTag = (File[]) bundle.getSerializable("filesOnTag");
        mode = (Integer) bundle.getInt("Orientation");
        linearLayout = (LinearLayout) view.findViewById(R.id.show_content_fragment_list);
        nfc_tag_icon = (ImageView) view.findViewById(R.id.nfc_tag_icon);
        nfc_transmit = (ImageView) view.findViewById(R.id.nfc_transmit);
        listCreator(filesOnTag);
//        nfc_tag_icon.setVisibility(View.VISIBLE);
//        nfc_transmit.setVisibility(View.VISIBLE);
        nfc_transmit_animation = AnimationUtils.loadAnimation(this.listener, R.anim.shrink);
        nfc_transmit.startAnimation(nfc_transmit_animation);

        Log.i("Files Oan Tag: ", filesOnTag.toString());
    }

    void listCreator(File[] files) {

        for(int i = 0; i<files.length; i++) {

            String extension = FilenameUtils.getExtension(files[i].toString());
            String fileName = files[i].toString();
            filesArray = files;
            ImageButton valueButton = new ImageButton(this.listener);
//            valueButton.setId(Integer.parseInt("@+id/"+ Integer.toString(i)));
            valueButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            ((LinearLayout) linearLayout).addView(valueButton);

            if(extension.equalsIgnoreCase("mp3")) {

                valueButton.setImageResource(R.drawable.audio_media);
                callActivityName = "ReviewAudioStory";
            }

            else if(extension.equalsIgnoreCase("mp4")) {

                valueButton.setImageResource(R.drawable.video_media);
                callActivityName = "ReviewVideoStory";
            }

            else if (extension.equalsIgnoreCase("txt")) {

                valueButton.setImageResource(R.drawable.written_media);
                callActivityName = "ReviewWrittenStory";
            }

            else if(extension.equalsIgnoreCase("jpg")) {

                valueButton.setImageResource(R.drawable.camera_media);
                callActivityName = "ReviewPictureStory";
            }

            buttonList.add(valueButton);
            buttonToFileMap.put(valueButton, files[i]);
            buttonStringHashMap.put(valueButton, callActivityName);
        }

        for(int i=0; i<buttonList.size(); i++) {

            buttonList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i("Button Pressed: ", buttonStringHashMap.get(v).toString());
//                    showContent(buttonToFileMap.get(v));
                    Intent intent;

                    if(buttonStringHashMap.get(v).equals("ReviewAudioStory")) {
                        intent = new Intent(getActivity(), ReviewAudioStory.class);
                        intent.putExtra("Orientation", mode);
                        intent.putExtra("AudioFile", buttonToFileMap.get(v));
                        getActivity().startActivity(intent);
                    }

                    else if(buttonStringHashMap.get(v).equals("ReviewVideoStory")) {
                        intent = new Intent(getActivity(), ReviewVideoStory.class);
                        intent.putExtra("Orientation", mode);
                        intent.putExtra("VideoFile", buttonToFileMap.get(v));
                        getActivity().startActivity(intent);
                    }

                    else if(buttonStringHashMap.get(v).equals("ReviewPictureStory")) {
                        intent = new Intent(getActivity(), ReviewPictureStory.class);
                        intent.putExtra("Orientation", mode);
                        intent.putExtra("PictureFile", buttonToFileMap.get(v));
                        getActivity().startActivity(intent);
                    }

                    else if(buttonStringHashMap.get(v).equals("ReviewWrittenStory")) {
                        intent = new Intent(getActivity(), ReviewWrittenStory.class);
                        intent.putExtra("Orientation", mode);
                        intent.putExtra("WrittenFile", buttonToFileMap.get(v));
                        getActivity().startActivity(intent);
                    }

                }
            });

        }

    }

    void showContent(File file) {

        Log.i("showContent(): ", file.toString());

    }

    // This method is called when the fragment is no longer connected to the Activity
    // Any references saved in onAttach should be nulled out here to prevent memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    // This method is called after the parent Activity's onCreate() method has completed.
    // Accessing the view hierarchy of the parent activity must be done in the onActivityCreated.
    // At this point, it is safe to search for activity View objects by their ID, for example.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void pauseNplay(View view){

        if(pauseOrplay==false) {

            pauseOrplay = true;
            pauseNplay.setText("\u25B6");
            mp.pause();
        }

        else if(pauseOrplay==true) {

            pauseOrplay = false;
            pauseNplay.setText("II");
            mp.start();
        }
    }

}


/*
 @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //Toast.makeText(this, "Tag Detected", Toast.LENGTH_LONG).show();
            Log.d("d", "Found it");

                mp.reset();
            }


        try {
            if (mytag == null) {
               // Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show();
                takeAction = "Do Nothing";

            } else {
                read(mytag);

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

 */


        /*
        pauseNplay.setText("II");
        nfc_transmit.setVisibility(View.INVISIBLE);
        nfc_transmit.animate().cancel();
        nfc_transmit.clearAnimation();

        if(pauseNplayVisibility==false) {
            pauseNplay.setVisibility(View.VISIBLE);
            pauseNplayVisibility = true;
        }

        mp.setDataSource(s);
        mp.prepare();
        mp.start();
        mpPlaying = true;




        /*
        Uri selectedUri = Uri.parse(s);
        Log.d("HATERZ", "URI: " + selectedUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

          startActivity(intent);


        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
        {
            startActivity(intent);
        }
        else
        {
            Log.d("HATERZ", "Feck");

        }
        */