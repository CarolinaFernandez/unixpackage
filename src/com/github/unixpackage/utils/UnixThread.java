package com.github.unixpackage.utils;

import java.lang.reflect.InvocationTargetException;

//public class UnixThread implements Runnable {
public class UnixThread extends Thread {
    private String runClassName;
    private String runMethodName;
    private Object[] runArguments;

    public UnixThread(String runClassName, String runMethodName) {
    	this.runClassName = runClassName;
    	this.runMethodName = runMethodName;
    }
    
    public UnixThread(String runClassName, String runMethodName, String[] runArguments) {
    	this(runClassName, runMethodName);
    	this.runArguments = runArguments;
    }

    @Override
    public void run() {
    	java.lang.reflect.Method method;
    	Class<?> classObject;
    	Object classInstance;
		try {
			System.out.println("runClassName: " + this.runClassName);
			classObject = Class.forName(this.runClassName);
			// XXX added...
			System.out.println("classObject: " + classObject.toString());
			classInstance = (Object) classObject.newInstance();
			System.out.println("classInstance: " + classInstance.toString());
			// TODO Pass arguments
		  method = classInstance.getClass().getMethod(this.runMethodName);
		  System.out.println("method: " + method.toString());
		  method.invoke(classInstance, this.runArguments);
		} catch (SecurityException e) {
		  // ...
		} catch (NoSuchMethodException e) {
		  // ...
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
