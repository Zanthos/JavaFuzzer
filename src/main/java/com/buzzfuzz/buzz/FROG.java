package com.buzzfuzz.buzz;

import com.buzzfuzz.rog.ROG;
import com.buzzfuzz.rog.decisions.Config;

import org.reflections.Reflections;

public class FROG extends ROG {

    public FROG(Reflections reflections) {
        super(reflections);
    }

    @Override
    public void logCrash(Exception e, Config config) {
        Engine.log(e, config);
    }
}