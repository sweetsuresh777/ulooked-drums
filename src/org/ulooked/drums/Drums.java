package org.ulooked.drums;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Drums extends Activity {
	public static Drums self = null;

	static final String TAG = "Drums";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);		
        setContentView(R.layout.main);
        self = this;
        ((DrumsView)findViewById(R.id.imageView)).init();
        
    }
     
   	public static class DrumsView extends View {
    	static final String TAG = "DrumsView";
    	
    	enum DrumButton {
    		Unknown,
    		TomHigh,
    		TomMiddle,
    		TomLow,
    		Rim,
    		Snare,
    		HiHat,
    		Ride,
    		Splash,
    		Kick,
    		Snare2,
    		HiHat2,
    		Open
    	}
    	
    	private Map<DrumButton,RectF> _buttonRegions;
    	private Map<DrumButton, Integer> _buttonSounds;
    	private SoundPool _sndPool;
    	private Map<Integer,Point> _pressedPointers;
    	private Paint _paint;
    	Point _lastEventPoint;
    	    	
    	public DrumsView(Context ctx) {
    		super(ctx);    		
    	}
    	
    	public DrumsView(Context context, AttributeSet attrs) {
    		super(context,attrs);
    		_paint = new Paint(); 
    		_paint.setStyle(Style.FILL); 
    		_paint.setShadowLayer(10, 5, 5, 0xff00ff);
    		_paint.setARGB(255, 80, 150, 30);
            _pressedPointers = new HashMap<Integer,Point>();
    	}
    	
    	public DrumsView(Context context, AttributeSet attrs, int defStyle) {
    		super(context,attrs,defStyle);
    	}
    	
//        @Override
//        protected void onFinishInflate() {
//        	super.onFinishInflate();
//        	((Activity)getContext()).getLayoutInflater().inflate(R.layout.main, this);
//        }
    	
    	public void init() {
            fillButtonRegions();
            loadSamples();
    	}
    	    
        protected void fillButtonRegions() {
        	_buttonRegions = new HashMap<DrumButton,RectF>();
        	_buttonRegions.put(DrumButton.TomHigh, new RectF(7,70,120,150));
        	_buttonRegions.put(DrumButton.TomMiddle, new RectF(123,70,236,150));
        	_buttonRegions.put(DrumButton.TomLow, new RectF(239,70,355,150));
        	_buttonRegions.put(DrumButton.Rim, new RectF(358,70,472,150));
           	_buttonRegions.put(DrumButton.Snare, new RectF(7,155,120,235));
           	_buttonRegions.put(DrumButton.HiHat, new RectF(123,155,236,235));
           	_buttonRegions.put(DrumButton.Ride, new RectF(239,155,355,235));
           	_buttonRegions.put(DrumButton.Splash, new RectF(358,155,472,235));
           	_buttonRegions.put(DrumButton.Kick, new RectF(7,238,120,316));
           	_buttonRegions.put(DrumButton.Snare2, new RectF(123,238,236,316));
           	_buttonRegions.put(DrumButton.HiHat2, new RectF(239,238,355,316));
           	_buttonRegions.put(DrumButton.Open, new RectF(358,238,472,316));
       }
        
        protected void loadSamples() {
        	_buttonSounds = new HashMap<DrumButton, Integer>();
        	_sndPool = new SoundPool(13, AudioManager.STREAM_MUSIC, 0);

        	try {
        		for (DrumButton butt : DrumButton.values()) {
        			if (butt == DrumButton.Unknown)
        				continue; //do not load unknown sound :)
     				_buttonSounds.put(butt, _sndPool.load(Drums.self.getAssets().openFd(soundFileNameForButton(butt)),1));
    			}
    		} catch (IOException e) {
    			Log.d(TAG, "Failed to load samples :(");
    			e.printStackTrace();
    		}
        }
        
        protected String currentSoundPack() {
        	return "defaultPack";
        }
        
        protected String soundFileNameForButton(DrumButton butt) {
        	StringBuilder sb = new StringBuilder();
        	sb.append(currentSoundPack()).append("/").append(butt.name()).append(".wav");
        	return sb.toString();
        }
        
        protected DrumButton buttonAt(Point pt) {
        	return buttonAt(pt.x,pt.y);
        }
        
        protected DrumButton buttonAt(float x, float y) {
        	for (Entry<DrumButton, RectF> entry : _buttonRegions.entrySet()) {
    			if (entry.getValue().contains(x,y)) {				
    				return entry.getKey();
    			}
    		}
        	return DrumButton.Unknown;
        }
        
        public void playSound(DrumButton butt) {
    		if (butt == DrumButton.Unknown)
    			return;
        	_sndPool.play(_buttonSounds.get(butt).intValue(),1,1,0,0,1);
        	Log.d(TAG,"Playing sound for button: "+butt.name());
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
        	super.onDraw(canvas);
        	int radius = 16;
        	
        	if (_lastEventPoint != null) {
        		canvas.drawCircle(_lastEventPoint.x, _lastEventPoint.y, 10, _paint);
        	}
        	
        	for (Entry<Integer, Point> entry : _pressedPointers.entrySet()) {
        		Point pt = entry.getValue();
        		canvas.drawCircle(pt.x, pt.y, radius, _paint);
    		}
        }
        
        @Override
    	public boolean onTouchEvent(MotionEvent event) {
    		//dumpEvent(event);

    		int action = event.getAction();
    		int pointerId = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
    		
        	switch (action & MotionEvent.ACTION_MASK) {
        	case MotionEvent.ACTION_POINTER_DOWN:
    		{
    			Point pressedPoint = new Point((int)event.getX(pointerId),(int)event.getY(pointerId));
    			playSound(buttonAt(pressedPoint));
    			_pressedPointers.put(new Integer(pointerId),pressedPoint);    	
    			invalidate();
    		}
    		break;
        	case MotionEvent.ACTION_DOWN:
    		{
    			Point pressedPoint = new Point((int)event.getX(pointerId),(int)event.getY(pointerId));
    			playSound(buttonAt(pressedPoint));
    			_pressedPointers.put(new Integer(pointerId),pressedPoint);
        		//Log.d(TAG, "Button "+pressedButton.name()+" pressed!");
    			invalidate();
    		}
    		break;
        	case MotionEvent.ACTION_MOVE:        		
        		break;
        	case MotionEvent.ACTION_UP:
    			_pressedPointers.clear();
    			invalidate();
        		break;
        	case MotionEvent.ACTION_POINTER_UP:
    			_pressedPointers.remove(new Integer(pointerId));
    			invalidate();
        		break;
        	}    	
    		return true;
    	}       
    	
    	/** Show an event in the LogCat view, for debugging */
    	private void dumpEvent(MotionEvent event) {
    	   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
    	      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
    	   StringBuilder sb = new StringBuilder();
    	   int action = event.getAction();
    	   int actionCode = action & MotionEvent.ACTION_MASK;
    	   sb.append("event ACTION_" ).append(names[actionCode]);
    	   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
    	         || actionCode == MotionEvent.ACTION_POINTER_UP) {
    	      sb.append("(pid " ).append(
    	      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
    	      sb.append(")" );
    	   }
    	   sb.append("[" );
    	   for (int i = 0; i < event.getPointerCount(); i++) {
    	      sb.append("#" ).append(i);
    	      sb.append("(pid " ).append(event.getPointerId(i));
    	      sb.append(")=" ).append((int) event.getX(i));
    	      sb.append("," ).append((int) event.getY(i));
    	      if (i + 1 < event.getPointerCount())
    	         sb.append(";" );
    	   }
    	   sb.append("]" );
    	   Log.d(TAG, sb.toString());
    	}
    }
}