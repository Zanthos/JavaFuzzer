/**
 *
 */
package com.buzzfuzz.buzz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import com.buzzfuzz.rog.decisions.Config;
import com.buzzfuzz.rog.utility.ConfigUtil;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author Johnny Rockett
 *
 */
public class ExceptionWorker extends Thread {

    static final double nextGenRatio = 0.5;

    ExceptionManager manager;
    int popSize;
    int gens;

    ExceptionWorkspace workspace;

    public boolean shouldStop = false;

    public ExceptionWorker(ExceptionManager manager, int popSize, int gens) {
        super();
        this.manager = manager;
        this.popSize = popSize;
        this.gens = gens;

    }

    public void run() {
        // Check to make sure we were given a valid directory

        while (!shouldStop) {

            // Get a new exception
            this.workspace = manager.popException();
            if (workspace == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            }

            // Evaluate exception
            for (int g = 0; g < this.gens; g++) {
                Member[] population = cull();

                Config[] nextGen = breed(population);

                // Now that we have bred the new population, we can delete the old one.
                try {
                    FileUtils.cleanDirectory(workspace.location);
                    System.out.println("Deleted past generation");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                simulate(nextGen);
            }
        }
    }

    private Member[] cull() {
        File[] members = this.workspace.location.listFiles();
        Member[] population = new Member[members.length];
        // Iterate over all configs in the directory
        for (int i=0; i < members.length; i++) {

            File log = Paths.get(members[i].getPath(), "log.txt").toFile();
            long fitness = log.length();

            population[i] = new Member(members[i], fitness);
        }

        // Sort them by their fitness in decending order
        Arrays.sort(population, new SortbyFitness());

        // Only take the first part of the population that had the highest fitness
        return Arrays.copyOfRange(population, 0, Math.min(popSize, (int)(population.length * nextGenRatio)));
    }

    private Config[] breed(Member[] population) {
        // Break members into random pairs and combine into two opposite offspring
        // Random change of another pair of kids based on their fitness function

        Config[] nextGeneration = new Config[population.length*3];

        for (int childIndex = 0; childIndex < nextGeneration.length; childIndex++) {
            Member p1 = population[(int)(Math.random() * population.length-1)];
            Member p2 = population[(int)(Math.random() * population.length-1)];
            nextGeneration[childIndex] = createChild(p1.extractConfig(), p2.extractConfig());
        }

        // TODO: Randomly mutate offspring

        return nextGeneration;
    }

    private void simulate(Config[] nextPop) {
        // For each config, run a test using it
        for (Config config : nextPop) {
            Runner runner = new Runner(this.workspace.method, 1, config);
            runner.execute();
        }
    }

    private Config createChild(Config a, Config b) {
        // TODO: randomly choose nodes from parents
        return a;
    }

    private class Member {
        File location;
        long fitness;

        Config config;

        public Member(File location, long fitness) {
            this.location = location;
            this.fitness = fitness;
        }

        public Config extractConfig() {
            if (this.config == null) {
                String configFilePath = Paths.get(this.location.getPath(), "config.xml").toString();
                this.config = new Config(ConfigUtil.createConfigFromFile(configFilePath));
            }
            return this.config;
        }
    }

    class SortbyFitness implements Comparator<Member>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Member a, Member b)
        {
            return (int)(b.fitness - a.fitness);
        }
    }
}
