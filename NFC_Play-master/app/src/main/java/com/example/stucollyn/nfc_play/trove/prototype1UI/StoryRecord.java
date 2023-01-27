package com.example.stucollyn.nfc_play.trove.prototype1UI;

import java.io.Serializable;

/**
 * Created by StuCollyn on 24/07/2018.
 */

public class StoryRecord implements Serializable {

    String StoryID;
    String StoryName;
    String StoryDate;
    String StoryRef;
    String StoryType;
    String linkedText;


    public StoryRecord(String StoryID, String StoryName, String StoryDate, String StoryRef, String StoryType) {

        this.StoryID = StoryID;
        this.StoryName = StoryName;
        this.StoryDate = StoryDate;
        this.StoryRef = StoryRef;
        this.StoryType = StoryType;

    }

    public String getStoryID() {

        return  StoryID;
    }

    public String getStoryName() {

        return  StoryName;
    }

    public String getStoryDate() {

        return  StoryDate;
    }

    public String getStoryRef() {

        return  StoryRef;
    }

    public String getStoryType() {

        return  StoryType;
    }
}
