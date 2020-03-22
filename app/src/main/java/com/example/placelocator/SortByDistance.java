package com.example.placelocator;

import java.util.Comparator;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class SortByDistance implements Comparator<PlacePojo> {

    private double curLatitude,curLongitude;

    @Override
    public int compare(PlacePojo obj1, PlacePojo obj2) {
        double distance1=obj1.getDistance();
        double distance2=obj2.getDistance();

        if(distance1-distance2>0)
            return 1;
        return -1;
    }

}
