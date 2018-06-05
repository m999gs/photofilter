package com.example.m999g.photofilters;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.m999g.photofilters.imageprocessors.Filter;
import com.example.m999g.photofilters.utils.ThumbnailCallback;
import com.example.m999g.photofilters.utils.ThumbnailItem;
import com.example.m999g.photofilters.utils.ThumbnailsManager;

import java.util.List;

import butterknife.ButterKnife;

public class FilterFragment extends Fragment implements ThumbnailCallback {
    private FiltersListFragmentListener listener;

    private RecyclerView thumbListView;
    private ImageView placeHolderImageView;
    private Bitmap thumbImage;
    int drawable;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_list, container, false);
        thumbListView = (RecyclerView)view.findViewById(R.id.thumbnails);
        placeHolderImageView = (ImageView) view.findViewById(R.id.place_holder_imageview);
        drawable = R.drawable.dog;


        ButterKnife.bind(this, view);
        initUIWidgets();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initUIWidgets() {
        placeHolderImageView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getContext().getResources(), drawable), 640, 640, false));
        thumbImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getContext().getResources(), drawable), 640, 640, false);
        initHorizontalList();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bindDataToAdapter() {
        final Context context = getContext();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                ThumbnailsManager.clearThumbs();
                List<Filter> filters = FilterPack.getFilterPack(getContext());

                for (Filter filter : filters) {
                    ThumbnailItem thumbnailItem = new ThumbnailItem();
                    thumbnailItem.image = thumbImage;
                    thumbnailItem.filterName = filter.getName();
                    thumbnailItem.filter = filter;
                    ThumbnailsManager.addThumb(thumbnailItem);
                }

                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) getActivity());
                thumbListView.setAdapter(adapter);

                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }


    @Override
    public void onThumbnailClick(Filter filter) {
        placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getActivity().getResources(), drawable), 640, 640, false)));
    }

    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }

}
