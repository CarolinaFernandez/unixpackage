package com.github.unixpackage.utils;

import java.lang.reflect.InvocationTargetException;

public class UnixThread extends Thread {
	private String runClassName;
	private String runMethodName;
	private Object[] runArguments;

	public UnixThread(String runClassName, String runMethodName) {
		this.runClassName = runClassName;
		this.runMethodName = runMethodName;
	}

	public UnixThread(String runClassName, String runMethodName,
			String[] runArguments) {
		this(runClassName, runMethodName);
		this.runArguments = runArguments;
	}

	@Override
	public void run() {
		java.lang.reflect.Method method;
		Class<?> classObject;
		Object classInstance;
		try {
			classObject = Class.forName(this.runClassName);
			classInstance = (Object) classObject.newInstance();
			method = classInstance.getClass().getMethod(this.runMethodName);
			method.invoke(classInstance, this.runArguments);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
