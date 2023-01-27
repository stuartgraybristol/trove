package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.example.stucollyn.nfc_play.R;


/**
 * Created by StuCollyn on 14/05/2018.
 */

public class ShowTagContentFragmentList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    FragmentActivity listener;
    ImageView nfc_transmit;
    Button pauseNplay;
    Animation nfc_transmit_animation;
    MediaPlayer mp = new MediaPlayer();
    boolean mpPlaying = false;
    boolean pauseNplayVisibility = false;
    boolean pauseOrplay = false;

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
        return inflater.inflate(R.layout.show_content_fragment, parent, false);

    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("d", "Check");
        nfc_transmit = (ImageView) view.findViewById(R.id.nfc_transmit);
        //nfc_transmit_animation = AnimationUtils.loadAnimation(this, R.anim.flash);
        int visi = nfc_transmit.getVisibility();
        nfc_transmit.startAnimation(nfc_transmit_animation);
        pauseNplay = (Button) view.findViewById(R.id.button);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this.listener);
//        progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) view.findViewById(android.R.id.content);
        root.addView(progressBar);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this.listener,
                android.R.layout.simple_list_item_1, null,
                fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, (android.app.LoaderManager.LoaderCallbacks<Cursor>) this);
        
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this.listener, ContactsContract.Data.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
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