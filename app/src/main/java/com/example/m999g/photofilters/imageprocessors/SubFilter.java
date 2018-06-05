package com.example.m999g.photofilters.imageprocessors;

import android.graphics.Bitmap;

public interface SubFilter {
    Bitmap process(Bitmap inputImage);

    Object getTag();

    void setTag(Object tag);
}
