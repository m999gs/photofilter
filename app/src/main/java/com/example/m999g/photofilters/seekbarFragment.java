package com.example.m999g.photofilters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import butterknife.ButterKnife;

public class seekbarFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{
    private SeekBar brightness;
    private SeekBar contrast;
    private SeekBar saturation;
    private EditImageFragmentListener listener;

    public seekbarFragment(){
        // this is a constructor
    }

    public void setListener(EditImageFragmentListener listener) {
        this.listener = listener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seekbar, container, false);

        ButterKnife.bind(this, view);
        brightness=view.findViewById(R.id.seekbar_brightness);
        contrast=view.findViewById(R.id.seekbar_contrast);
        saturation=view.findViewById(R.id.seekbar_saturation);

        // keeping brightness value b/w -100 / +100
        brightness.setMax(200);
        brightness.setProgress(100);

        // keeping contrast value b/w 1.0 - 3.0
        contrast.setMax(20);
        contrast.setProgress(0);

        // keeping saturation value b/w 0.0 - 3.0
        saturation.setMax(30);
        saturation.setProgress(10);

        brightness.setOnSeekBarChangeListener(this);
        contrast.setOnSeekBarChangeListener(this);
        saturation.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (listener != null) {

            if (seekBar.getId() == R.id.seekbar_brightness) {
                // brightness values are b/w -100 to +100
                listener.onBrightnessChanged(progress - 100);
            }

            if (seekBar.getId() == R.id.seekbar_contrast) {
                // converting int value to float
                // contrast values are b/w 1.0f - 3.0f
                // progress = progress > 10 ? progress : 10;
                progress += 10;
                float floatVal = .10f * progress;
                listener.onContrastChanged(floatVal);
            }

            if (seekBar.getId() == R.id.seekbar_saturation) {
                // converting int value to float
                // saturation values are b/w 0.0f - 3.0f
                float floatVal = .10f * progress;
                listener.onSaturationChanged(floatVal);
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onEditCompleted();
    }

    public interface EditImageFragmentListener {
        void onBrightnessChanged(int brightness);

        void onSaturationChanged(float saturation);

        void onContrastChanged(float contrast);

        void onEditStarted();

        void onEditCompleted();
    }
}
