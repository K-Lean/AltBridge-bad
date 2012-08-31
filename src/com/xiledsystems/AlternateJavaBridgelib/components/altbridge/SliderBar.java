package com.xiledsystems.AlternateJavaBridgelib.components.altbridge;

import com.xiledsystems.AlternateJavaBridgelib.components.events.EventDispatcher;

import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SliderBar extends AndroidViewComponent implements OnSeekBarChangeListener {

	private final SeekBar view;
		
	
	protected SliderBar(ComponentContainer container) {
		super(container);
		view = new SeekBar(container.$context());
		view.setOnSeekBarChangeListener(this);
		
		container.$add(this);
		
	}
	
	protected SliderBar(ComponentContainer container, int resourceId) {
		super(container, resourceId);
		view = null;
		
		SeekBar bar = (SeekBar) container.$form().findViewById(resourceId);
		bar.setOnSeekBarChangeListener(this);
		
	}
	
	public int PositionMax() {
		if (resourceId != -1) {
			return ((SeekBar) container.$form().findViewById(resourceId)).getMax() + 1;
		} else {
			return view.getMax() + 1;
		}
	}
	
	public void PositionMax(int max) {
		if (resourceId != -1) {
			((SeekBar) container.$form().findViewById(resourceId)).setMax(max+1);
		} else {
			view.setMax(max + 1);
		}
	}
	
	public int Position() {
		if (resourceId != -1) {
			return ((SeekBar) container.$form().findViewById(resourceId)).getProgress() + 1;
		} else {
			return view.getProgress() + 1;
		}
	}
	
	public void Position(int position) {
		if (position<1) {
			position = 1;
		}
		if (resourceId!= -1) {
			((SeekBar) container.$form().findViewById(resourceId)).setProgress(position-1);
		} else {
			view.setProgress(position-1);
		}
	}	
	
	@Override
	public View getView() {		
		if (resourceId != -1) {
			return (SeekBar) container.$form().findViewById(resourceId);
		} else {
			return view;
		}
	}

	@Override
	public void postAnimEvent() {
		
		EventDispatcher.dispatchEvent(this, "AnimationMiddle");

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		EventDispatcher.dispatchEvent(this, "PositionChanged", fromUser);
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}
