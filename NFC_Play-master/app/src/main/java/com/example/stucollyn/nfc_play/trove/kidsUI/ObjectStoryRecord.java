package com.example.stucollyn.nfc_play.trove.kidsUI;

import java.io.Serializable;

/*
ObjectStoryRecords are objects made up for individual story files - every image or audio file has an ObjectStoryRecord created for it when it is displayed in the Archive
and ExploreArchiveItem activities.
 */
public class ObjectStoryRecord implements Serializable {

    String objectName;  //The unique identifier of the object story folder the current story file belongs to
    String storyName;   //The unique identifier of the story file
    String storyDate;   //The data the story file was created
    String storyRef;    //The storage reference - either local or cloud for the story file
    String storyType;   //The data type of the story file
    String coverImage;  //Is the file a cover image? "yes" or "no"
    String linkedText;  //Any associated text about a story file
    String objectContext;   //Is story local or cloud


    //ObjectStoryRecord constructor
    public ObjectStoryRecord(String ObjectName, String StoryName, String StoryDate, String StoryRef, String StoryType, String CoverImage, String ObjectContext) {

        this.objectName = ObjectName;
        this.storyName = StoryName;
        this.storyDate = StoryDate;
        this.storyRef = StoryRef;
        this.storyType = StoryType;
        this.coverImage = CoverImage;
        this.objectContext = ObjectContext;
    }

    //Return coverImage
    public String isCoverImage() {

        return coverImage;
    }

    //Return objectName
    public String getObjectName() {

        return objectName;
    }

    //Return storyName
    public String getStoryName() {

        return storyName;
    }

    //Return storyDate
    public String getStoryDate() {

        return storyDate;
    }

    //Return storyRef
    public String getStoryRef() {

        return storyRef;
    }

    //Return storyTypes
    public String getStoryType() {

        return storyType;
    }

    //Return objectContext
    public String getObjectContext() {

        return objectContext;
    }
}
