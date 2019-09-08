package com.example.photogallery.Activity;

import androidx.fragment.app.Fragment;

import com.example.photogallery.Fragment.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
