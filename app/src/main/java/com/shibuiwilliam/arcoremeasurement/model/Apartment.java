
package com.shibuiwilliam.arcoremeasurement.model;

import androidx.annotation.NonNull;

import com.mecofarid.dynamicspinner.annotation.SubCategory;
import com.mecofarid.dynamicspinner.model.ItemSpinner;

import java.util.List;

public class Apartment extends ItemSpinner {

    public String name;
    public Integer code;
    @SubCategory
    public List<Room> roomList = null;

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
