package com.example.kru13.bitmaptest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener, GestureDetector.OnGestureListener {

	// Used to load the 'native-lib' library on application startup.
	static {
		System.loadLibrary("native-lib");
	}

	private double re = -2.0;
	private double im = -1.2;
	private double xPos = -2.0;
	private double yPos = -1.0;
	private double zoom = 3.0;
	private final double xStep = 0.2;
	private final double yStep = 0.2;
	private final double zoomStep = 0.1;
	private final double zoomMax = 4;
	private double imgViewWidth, imgViewHeight;
	private TextView labelZoom;

	private double pinchValue = 0;
	private int pinchCounter = 0;


	/**
	 * A native method that is implemented by the 'native-lib' native library,
	 * which is packaged with this application.
	 */
	public native String stringFromJNI();

	private native void bitmapChange(Bitmap img, double xPos, double yPos, double zoom);

	ImageView imgview;

	ScaleGestureDetector SGD;
	private GestureDetectorCompat flinkDetector;

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
				pinchValue += scaleGestureDetector.getScaleFactor();
				pinchCounter++;
//				Log.d("LibBitmap", "pinch zoom " + scale);
				return true;
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
//				double scale = scaleGestureDetector.getScaleFactor();
//				Log.d("LibBitmap", "pinch zoom " + scale);
				return true;
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
				if ((pinchValue / pinchCounter) > 0) {
					Log.d("LibBitmap", "pinch zoom in");
					zoom -= (zoomStep * pinchCounter) / 5;
				} else if ((pinchValue / pinchCounter) < 0) {
					Log.d("LibBitmap", "pinch zoom out");
					zoom += (zoomStep * pinchCounter) / 5;
				}
				refreshFractal();
				pinchValue = 0;
				pinchCounter = 0;
			}
		});

		imgview.setFocusable(true);
		imgview.setClickable(true);
		imgViewHeight = imgview.getMeasuredHeight();
		imgViewWidth = imgview.getMeasuredWidth();


		imgview.setOnTouchListener(this);

		SeekBar seekBarZoom = (SeekBar) findViewById(R.id.seekBarZoom);
		seekBarZoom.setOnSeekBarChangeListener(this);

		this.labelZoom = (TextView) findViewById(R.id.textViewZoom);

		flinkDetector = new GestureDetectorCompat(this, this);

		refreshFractal();
	}

	private void refreshFractal() {
		labelZoom.setText("Zoom " + Double.toString(4 - zoom));


		Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.vsb);
		Log.d("LibBitmap", "refreshing fractal ...");

		// Example of a call to a native method
		bitmapChange(img, xPos, yPos, zoom);

		Log.d("NDK bitmapChanged", "Finished");
		imgview.setImageBitmap(img);
	}

	public void buttonClick(View v) {
		if (v.getId() == R.id.btnDown) {
			yPos -= yStep;
		} else if (v.getId() == R.id.btnUp) {
			yPos += yStep;
		} else if (v.getId() == R.id.btnLeft) {
			xPos -= xStep;
		} else if (v.getId() == R.id.btnRight) {
			xPos += xStep;
		}
		refreshFractal();
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		SGD.onTouchEvent(motionEvent);
		flinkDetector.onTouchEvent(motionEvent);

//		if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//			double x = motionEvent.getX();
//			double y = motionEvent.getY();
//
//			if (x < (imgViewWidth / 2) && y < (imgViewHeight / 2)) {
//				Log.d("LibBitmap", "left up");
//			} else if (x >= (imgViewWidth / 2) && y < (imgViewHeight / 2)) {
//				Log.d("LibBitmap", "right up");
//			} else if (x < (imgViewWidth / 2) && y >= (imgViewHeight / 2)) {
//				Log.d("LibBitmap", "left down");
//			} else if (x >= (imgViewWidth / 2) && y >= (imgViewHeight / 2)) {
//				Log.d("LibBitmap", "right down");
//			}
//		}
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

		if (seekBar.getId() == R.id.seekBarZoom) {
			zoom = ((zoomMax * 10) - (double) i) / 10;

		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		refreshFractal();
	}

	@Override
	public boolean onDown(MotionEvent motionEvent) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent motionEvent) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent motionEvent) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent motionEvent) {

	}

	@Override
	public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
		Log.d("LibBitmap", String.format("Flink %f, %f, %f, %f", motionEvent.getX(), motionEvent.getY(), motionEvent1.getX(), motionEvent1.getY()));

		if (motionEvent.getX() < motionEvent1.getX()) {
			xPos -= xStep;
		} else if (motionEvent.getX() > motionEvent1.getX()) {
			xPos += xStep;
		}

		if (motionEvent.getY() < motionEvent1.getY()) {
			yPos += yStep;
		} else if (motionEvent.getY() > motionEvent1.getY()) {
			yPos -= yStep;
		}

		refreshFractal();
		return true;
	}
}
