package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Fragment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by StuCollyn on 20/05/2018.
 */

public class ParcelFragments implements Serializable {

        public ArrayList<Fragment> fragmentParcelable;

    public ParcelFragments(ArrayList<Fragment> fragments) {
        this.fragmentParcelable = fragments;
    }


    }


