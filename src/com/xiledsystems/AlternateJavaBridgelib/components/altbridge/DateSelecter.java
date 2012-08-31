package com.xiledsystems.AlternateJavaBridgelib.components.altbridge;

import java.util.Calendar;

import com.xiledsystems.AlternateJavaBridgelib.components.events.EventDispatcher;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class DateSelecter extends ButtonBase {

	DatePickerDialog dialog;
	private int mYear;
	private int mMonth;
	private int mDay;
	
	private DatePickerDialog.OnDateSetListener selectListener = new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			dateSet(year, monthOfYear, dayOfMonth);			
		}
	};
	
	public DateSelecter(ComponentContainer container) {
		super(container);				
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
	}
	
	public DateSelecter(ComponentContainer container, int resId) {
		super(container, resId);				
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
	}
	
	public void DefaultDate(int year, int month, int day) {
		mYear = year;
		mMonth = month;
		mDay = day;		
	}
	
	public int[] DefaultDate() {
		return new int[] { mYear, mMonth, mDay };
	}
	
	private void dateSet(int year, int monthOfYear, int dayOfMonth) {
		EventDispatcher.dispatchEvent(this, "DateSet", year, monthOfYear, dayOfMonth);
	}
	
	@Override
	public void click() {
		//view.init(mYear, mMonth, mDay, selectListener);
		dialog = new DatePickerDialog(container.$context(), selectListener, mYear, mMonth, mDay);
		dialog.show();		
	}

}
