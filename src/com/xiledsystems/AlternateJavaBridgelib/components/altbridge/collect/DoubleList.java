package com.xiledsystems.AlternateJavaBridgelib.components.altbridge.collect;

import java.util.ArrayList;

public class DoubleList {

	private ArrayList<Object> list1;
	private ArrayList<Object> list2;
	
	public DoubleList() {
		list1 = new ArrayList<Object>();
		list2 = new ArrayList<Object>();
	}
	
	public DoubleList(ArrayList<Object> list1, ArrayList<Object> list2) {
		this.list1 = list1;
		this.list2 = list2;
	}
	
	public void add(Object firstItem, Object secondItem) {
		list1.add(firstItem);
		list2.add(secondItem);
	}
	
	public void remove(int index) {
		list1.remove(index);
		list2.remove(index);		
	}
	
	public void replaceLists(ArrayList<Object> firstList, ArrayList<Object> secondList) {
		list1 = firstList;
		list2 = secondList;
	}
	
	public Object[] get(int index) {
		Object things[] = new Object[2];
		things[0] = list1.get(index);
		things[1] = list2.get(index);
		return things;
	}
			
	public ArrayList<Object> getList(int oneortwo) {
		if (oneortwo==1) {
			return list1;
		}
		if (oneortwo==2) {
			return list2;			
		}
		return null;
	}
	
	public int size() {
		int one = list1.size();
		int two = list2.size();
		if (one != two) {
			return -1;
		}
		return one;
	}
	
	public int index(Object object) {
		int idx = list1.indexOf(object);
		if (idx == -1) {
			idx = list2.indexOf(object); 
		} 
		return idx;		
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof DoubleList) {
			DoubleList listcheck = (DoubleList) object;
			if (listcheck.getList(1).equals(list1) && listcheck.getList(2).equals(list2)) {
				return true;
			}
		} 
		return false;		
	}
	
}
