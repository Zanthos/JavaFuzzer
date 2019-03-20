package com.buzzfuzz.buzz;

import java.io.File;
import java.lang.reflect.Method;

public class ExceptionWorkspace {

    public File location;
    public Method method;

    public ExceptionWorkspace(File location, Method method) {
        this.location = location;
        this.method = method;
    }
}