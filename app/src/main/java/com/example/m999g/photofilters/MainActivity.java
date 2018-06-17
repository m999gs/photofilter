package com.example.m999g.photofilters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.m999g.photofilters.FilterPack;
import com.example.m999g.photofilters.imageprocessors.Filter;
import com.example.m999g.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.example.m999g.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.example.m999g.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.example.m999g.photofilters.utils.ThumbnailCallback;
import com.example.m999g.photofilters.utils.ThumbnailItem;
import com.example.m999g.photofilters.utils.ThumbnailsManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ThumbnailCallback{

    private static int PICK_IMAGE = 1;
    private static final int READ_MEMORY_PERMISSIONS_REQUEST = 1;

    private ImageView placeHolderImageView;

    private RecyclerView thumbListView;
    private Bitmap thumbImage;
    int drawable;


    static {
        System.loadLibrary("NativeImageProcessor");
    }
    private Activity activity;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        drawable = R.drawable.cat;
        thumbImage=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), drawable), 640, 640, false);
        initUIWidgets();
        getPermissionToAccessStorage();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void initUIWidgets() {
        thumbListView = (RecyclerView) findViewById(R.id.thumbnails);
        placeHolderImageView = (ImageView) findViewById(R.id.place_holder_imageview);
        placeHolderImageView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), drawable), 640, 640, false));
        initHorizontalList();
    }

    private void initHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }

    private void bindDataToAdapter() {
        final Context context = this.getApplication();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                ThumbnailsManager.clearThumbs();
                List<Filter> filters = FilterPack.getFilterPack(getApplicationContext());

                for (Filter filter : filters) {
                    ThumbnailItem thumbnailItem = new ThumbnailItem();
                    thumbnailItem.filterName=filter.getName();
                    thumbnailItem.image = thumbImage;
                    thumbnailItem.filter = filter;
                    ThumbnailsManager.addThumb(thumbnailItem);
                }

                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) activity);
                thumbListView.setAdapter(adapter);

                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open:
                addImageMethod();
                return true;
            case R.id.action_save:
                takeAndSaveScreenShot(activity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addImageMethod() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageuri = data.getData();
            try {
                thumbImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageuri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String path=thumbImage.toString();
            drawable=Integer.parseInt(path);
            initUIWidgets();
        }
    }

    public void takeAndSaveScreenShot(Activity mActivity) {
        View MainView = mActivity.getWindow().getDecorView();
        MainView.setDrawingCacheEnabled(true);
        MainView.buildDrawingCache();
        Bitmap MainBitmap = MainView.getDrawingCache();
        Rect frame = new Rect();

        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        //to remove statusBar from the taken sc
        int statusBarHeight = frame.top;

        //Action Bar Height
        int actionBarHeight = 0;
        final TypedArray styledAttributes = mActivity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        //using screen size to create bitmap
        int width = placeHolderImageView.getWidth();
        int height = placeHolderImageView.getHeight();
        Bitmap OutBitmap = Bitmap.createBitmap(MainBitmap, 0, statusBarHeight + actionBarHeight, width, height);
        MainView.destroyDrawingCache();
        try {

            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            //you can also using current time to generate name
            String name = "Mohit";
            File file = new File(path, name + ".png");
            fOut = new FileOutputStream(file);

            OutBitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            Toast.makeText(getApplicationContext(), "image Saved", Toast.LENGTH_SHORT).show();
            fOut.flush();
            fOut.close();

            //this line will add the saved picture to gallery
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToAccessStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_MEMORY_PERMISSIONS_REQUEST);
        }
    }
    @Override
    public void onThumbnailClick(Filter filter) {
        placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), drawable), 640, 640, false)));
    }



}
