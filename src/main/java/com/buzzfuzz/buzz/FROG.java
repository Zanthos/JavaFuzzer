package com.buzzfuzz.buzz;

import java.lang.reflect.Method;

import com.buzzfuzz.rog.ROG;
import com.buzzfuzz.rog.decisions.Config;

import org.reflections.Reflections;

public class FROG extends ROG {

    public FROG(Reflections reflections) {
        super(reflections);
    }

    @Override
    public void logCrash(Exception e, Config config) {
        // Should ask Engine to do it so that file writing is thread safe
        Engine.log(e, config);
    }
}