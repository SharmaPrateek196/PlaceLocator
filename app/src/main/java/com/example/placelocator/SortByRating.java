package com.example.placelocator;

import android.util.Log;

import java.util.Comparator;

public class SortByRating implements Comparator<PlacePojo> {
    @Override
    public int compare(PlacePojo p1, PlacePojo p2) {
        Log.d("diffe==",""+(p1.getRating()-p2.getRating()));
        if(p1.getRating()-p2.getRating()<0.0)
            return 1;
        else
            return -1;
    }
}
