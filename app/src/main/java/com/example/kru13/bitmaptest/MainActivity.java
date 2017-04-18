package com.example.kru13.bitmaptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

	// Used to load the 'native-lib' library on application startup.
	static {
		System.loadLibrary("native-lib");
	}

	private TextView labelSeekBarIm;
	private TextView labelSeekBarRe;

	private double re = -2.0;
	private double im = -1.2;
	private double imgViewWidth, imgViewHeight;


	/**
	 * A native method that is implemented by the 'native-lib' native library,
	 * which is packaged with this application.
	 */
	public native String stringFromJNI();

	private native void bitmapChange(Bitmap img, double re, double im);

	ImageView imgview;

	ScaleGestureDetector SGD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgview = (ImageView) findViewById(R.id.imageView1);

		ViewTreeObserver vto = imgview.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				imgview.getViewTreeObserver().removeOnPreDrawListener(this);
				imgViewHeight = imgview.getMeasuredHeight();
				imgViewWidth = imgview.getMeasuredWidth();
				return true;
			}
		});

		SGD = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
			@Override
			public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
				double scale = scaleGestureDetector.getScaleFactor();
				Log.d("LibBitmap", "pinch zoom " + scale);

				return true;
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
				double scale = scaleGestureDetector.getScaleFactor();
				Log.d("LibBitmap", "pinch zoom " + scale);

				return true;
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

			}
		});

		imgview.setFocusable(true);
		imgview.setClickable(true);
		imgViewHeight = imgview.getMeasuredHeight();
		imgViewWidth = imgview.getMeasuredWidth();


		imgview.setOnTouchListener(this);

		SeekBar seekBarRe = (SeekBar) findViewById(R.id.seekBarRe);
		SeekBar seekBarIm = (SeekBar) findViewById(R.id.seekBarIm);
		seekBarIm.setOnSeekBarChangeListener(this);
		seekBarRe.setOnSeekBarChangeListener(this);

		this.labelSeekBarRe = (TextView) findViewById(R.id.textViewRe);
		this.labelSeekBarIm = (TextView) findViewById(R.id.textViewIm);

		refreshFractal();
	}

	private void refreshFractal() {
		Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.vsb);
		Log.d("NDK bitmapChanged", "Starting ...");

		// Example of a call to a native method
		bitmapChange(img, re, im);

		Log.d("NDK bitmapChanged", "Finished");
		imgview.setImageBitmap(img);
	}

	public void buttonClick(View v) {
		refreshFractal();
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		SGD.onTouchEvent(motionEvent);

		if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			double x = motionEvent.getX();
			double y = motionEvent.getY();

			if (x < (imgViewWidth / 2) && y < (imgViewHeight / 2)) {
				Log.d("LibBitmap", "left up");
			} else if (x >= (imgViewWidth / 2) && y < (imgViewHeight / 2)) {
				Log.d("LibBitmap", "right up");
			} else if (x < (imgViewWidth / 2) && y >= (imgViewHeight / 2)) {
				Log.d("LibBitmap", "left down");
			} else if (x >= (imgViewWidth / 2) && y >= (imgViewHeight / 2)) {
				Log.d("LibBitmap", "right down");
			}
		}
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

		if (seekBar.getId() == R.id.seekBarIm) {
			im = ((double) i - 50) / 10;
			labelSeekBarIm.setText("Im: " + Double.toString(im));
		} else if (seekBar.getId() == R.id.seekBarRe) {
			re = ((double) i - 20) / 10;
			labelSeekBarRe.setText("Re: " + Double.toString(re));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		refreshFractal();
	}
}
