package com.example.testerapplication;


public class NativeInterface {
	private int fooVal;
	public NativeInterface() {
		// TODO Auto-generated constructor stub
		fooVal = 0;
	}
	
	public void doFoo(){
		fooVal = foo();
	}
	
	public int getFoo() {
		return fooVal;
	}
	
	private static native int foo();
}
