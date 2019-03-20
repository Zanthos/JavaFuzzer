package com.buzzfuzz.buzz;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class ExceptionManager {

    Set<File> foundExceptions;
    Stack<ExceptionWorkspace> exceptionInstances;

    ExceptionWorker[] workers;

    public ExceptionManager(int popSize, int numGens) {
        this.foundExceptions = new TreeSet<File>();
        this.exceptionInstances = new Stack<ExceptionWorkspace>();

        int cores = Runtime.getRuntime().availableProcessors();
        this.workers = new ExceptionWorker[cores - 1]; // cores - 1 because this thread will continue to run
        for (int i = 0; i < workers.length; i++) {
            this.workers[i] = new ExceptionWorker(this, popSize, numGens);
        }
    }

    public synchronized ExceptionWorkspace popException() {
        if (exceptionInstances.isEmpty())
            return null;
        return exceptionInstances.pop();
    }

    public void manage(File exceptionsDir) {

        // Start workers
        for (int i = 0; i < this.workers.length; i++) {
            this.workers[i].start();
        }

        // Give the workers work
        do {
            catalogExceptions(exceptionsDir);
        } while (!exceptionInstances.isEmpty());

        // Stop workers
        for (int i = 0; i < this.workers.length; i++) {
            this.workers[i].shouldStop = true;
            try {
                this.workers[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // TODO: I shouldn't stop checking for exceptions until all of the workers are idle
        // So, this should only disable and join when worker idle flag
    }

    private void catalogExceptions(File exceptionsDir) {
        // Now our initial set of random configs and exceptions exists
        // We should deligate exception workers to evolve those random configs

        for (Method method : Engine.methodTargets) {
            File methodDir = Paths.get(exceptionsDir.getPath(), Engine.getMethodName(method)).toFile();
            // For every type of exception
            for (File exception : methodDir.listFiles()) {
                // For every bug of exception
                for (File bug : exception.listFiles()) {
                    if (!foundExceptions.contains(bug)) {
                        foundExceptions.add(bug);
                        System.out.println("Pushing new bug: " + bug.getName());
                        File corpusPath = Paths.get(bug.getPath(), "corpus").toFile();
                        this.exceptionInstances.push(new ExceptionWorkspace(corpusPath, method));
                    }
                }
            }
        }
    }
}