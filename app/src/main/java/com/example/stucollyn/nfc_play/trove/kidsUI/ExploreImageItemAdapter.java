package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 This class is used to present the scrollable ImageView adapter in the ExploreImageItem activity.
 */

public class ExploreImageItemAdapter extends  RecyclerView.Adapter<ExploreImageItemAdapter.SimpleViewHolder> {

    //Activity context
    private Context mContext;
    Activity storyGallery;


    //File storage structures
    LinkedHashMap<String, File> fileList;
    ArrayList<Bitmap> coverImages;
    ArrayList<String> storyType;
    ArrayList<File> storyFile;
    HashMap<String, Bitmap> imageMap;
    HashMap<String, ArrayList<ObjectStoryRecord>> folderToImageRef;
    LinkedHashMap<String, ArrayList<ObjectStoryRecord>> storyTypeMap;

    //Archive gallery view
    CustomImageView[] imageButtons;
    TextView[] imageDesc;
    int numberOfThumbs = 0;
    int[] colourCode;
    private List<String> elements;
    int[] shapeResource = new int[]{R.raw.archive_shape_1, R.raw.archive_shape_2, R.raw.archive_shape_1};
    int[] shapeResourceBackground = new int[]{R.drawable.kids_ui_archive_shape_1, R.drawable.kids_ui_archive_shape_2, R.drawable.kids_ui_archive_shape_1};
    int shapeResourceCounter=0;

    //Mediaplayer
    private MediaPlayer mPlayer = null;

    //Networked status

    //Commentary
    CommentaryInstruction commentaryInstruction;

    //Custom image adapter holder
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final CustomImageView imageView;
        public final ImageView imageViewBackground;

        public SimpleViewHolder(View view) {
            super(view);
            imageView = (CustomImageView) view.findViewById(R.id.grid_item_kids_ui);
            imageViewBackground = (ImageView) view.findViewById(R.id.grid_item_background_kids_ui);
        }
    }

    //Inflate the ImageView holder layout
    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.activity_story_gallery_grid_item_kids_ui, parent, false);
        return new SimpleViewHolder(view);
    }

    //Play the selected file - play audio or show video
    void PlayFile(int position) {

        File[] filesOnTag;
        filesOnTag = new File[1];
        filesOnTag[0] = storyFile.get(position);

        ShowStoryContent showStoryContent = new ShowStoryContent(mPlayer, mContext, storyGallery, filesOnTag);
        showStoryContent.checkFilesOnTag();
    }

    //This method populates the holder with the number of different archive items.
    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, final int position) {

        //If there are coverImages in existence
        if(!coverImages.isEmpty()) {

            //Set the background of each image view to be a custom shape, defined in shapeResourceBackground - this is displayed behind the ImageView which is cut
            //in the same shape.
            holder.imageViewBackground.setBackgroundResource(shapeResourceBackground[shapeResourceCounter]);
            //Display the image file in the same shape as its background shape
            holder.imageView.setCustomImageResource(shapeResource[shapeResourceCounter]);

            //Display a different file cover depending on the file type
            if(storyType.get(position).equals("PictureFile")) {

                //Present a preview of the immage
                Bitmap bitmap = coverImages.get(position);
                holder.imageView.setImageBitmap(bitmap);
            }

            else if(storyType.get(position).equals("AudioFile")) {

                //Present an audio media icon
                holder.imageView.setImageResource(R.drawable.audio_media);
            }
        }

        //OnClick open the file
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Interrupt any commentary instructions
                commentaryInstruction.stopPlaying();
                PlayFile(position);
            }
        });

        //Increment the current shape resource - this changes the current shape for every archive gallery item. After all 3 different shapes have been used, repeat.
        if(shapeResourceCounter<2) {
            shapeResourceCounter++;
        }
        else {
            shapeResourceCounter=0;
        }
    }

    //Return the current item's position
    @Override
    public long getItemId(int position) {
        return position;
    }

    //Get the number of archive items in the collection
    @Override
    public int getItemCount() {
        return this.elements.size();
    }

    //ExploreImageItemAdapter constructor - takes in given values which are used to setup the structures for the gallery
    public ExploreImageItemAdapter(Activity storyGallery, Context c, int numberOfThumbs, LinkedHashMap<String, File> fileList, int[] colourCode,
                                   HashMap<String, ArrayList<ObjectStoryRecord>> folderToImageRef, LinkedHashMap<String, Bitmap> imageMap,
                                   LinkedHashMap<String, String> storyTypeMap, CommentaryInstruction commentaryInstruction) {

        this.storyGallery = storyGallery;
        mContext = c;
        mPlayer = new MediaPlayer();
        this.numberOfThumbs = numberOfThumbs;   //The number of cover images there are
        imageButtons = new CustomImageView[numberOfThumbs];     //Create the number of CustomImageView items required in the archive gallery
        imageDesc = new TextView[numberOfThumbs];   //Create the number of TextView items required in the archive gallery
        this.fileList = fileList;    //FolderFiles lists the unique story identifier and relates it to the collection of files associated with the story, holding them in an arraylist
        this.colourCode = colourCode;   //The array of colours for each cover thumbnail - this colour changes with each increment
        this.imageMap = imageMap;   //A map of story folder names and the corresponding story cover image
        this.folderToImageRef = folderToImageRef; //The ArrayList of ObjectStoryRecords for every unique story folder file
        this.commentaryInstruction = commentaryInstruction; //The current commentary object in use

        //This is where the number of elements - holder items is defined
        this.elements = new ArrayList<String>();

        //Create the same number of elements as imageMap size
        for(int i = 0; i < imageMap.size(); i++) {
            this.elements.add(i, "Position : " + i);
        }

        //Get values from each imageMap ObjectStoryRecord and split this data into ArrayLists of coverImages, storyType, and storyFile
        coverImages = new ArrayList<Bitmap>();
        storyType = new ArrayList<String>();
        storyFile = new ArrayList<File>();

        /*For each object name add save cover image, file, and type */
        for (Map.Entry<String, Bitmap> entry : imageMap.entrySet()) {
            String key = entry.getKey();
            Bitmap value = entry.getValue();
            coverImages.add(value);
            storyFile.add(fileList.get(key));
            storyType.add(storyTypeMap.get(key));
        }
    }
}