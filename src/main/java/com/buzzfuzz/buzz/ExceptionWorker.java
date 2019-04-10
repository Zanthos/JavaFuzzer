/**
 *
 */
package com.buzzfuzz.buzz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.buzzfuzz.rog.decisions.Choice;
import com.buzzfuzz.rog.decisions.Config;
import com.buzzfuzz.rog.decisions.ConfigTree;
import com.buzzfuzz.rog.decisions.Constraint;
import com.buzzfuzz.rog.decisions.Scope;
import com.buzzfuzz.rog.decisions.Target;
import com.buzzfuzz.rog.utility.ConfigUtil;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author Johnny Rockett
 *
 */
public class ExceptionWorker extends Thread {

    static final double nextGenRatio = 0.2;
    static final double validChoiceWeight = 10;

    private ExceptionManager manager;
    private int popSize;
    private int gens;

    private ExceptionWorkspace workspace;

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
            List<Double> fitnesses = new ArrayList<Double>();
            String winner = "";
            for (int g = 0; g < this.gens; g++) {
                Member[] population = cull();
                if (population.length != 0) {
                    fitnesses.add((double)Math.round(population[0].fitness));
                    winner = population[0].location.getAbsolutePath();
                } else {
                    fitnesses.add(0.0);
                }

                Config[] nextGen = breed(population);

                simulate(nextGen);
            }
            System.out.println(this.workspace.location.getParentFile().getName() + ": " + fitnesses.toString() + "\nfile:///" + winner + "/config.xml");
        }
    }

    private Member[] cull() {
        File[] members = this.workspace.location.listFiles();
        Member[] population = new Member[(members == null) ? 0 : members.length];
        // Iterate over all configs in the directory
        for (int i=0; i < members.length; i++) {
            population[i] = new Member(members[i]);
        }
        // Give each member of the population a fitness score
        Random random = new Random();
        for (Member member : population) {
            double fitness = 0;
            for (int i=0; i<this.popSize/5; i++) {
                int index = random.nextInt(population.length);
                fitness += evaluateFitness(member, population[index]);
            }
            fitness /= this.popSize/5;
            int constraintPenalty = 0;
            for (Scope scope : member.extractConfig().getTree()) {
                constraintPenalty += getBreadth(scope.getConstraint());
            }
            fitness -= constraintPenalty;
            member.setFitness(fitness);
        }

        // Sort them by their fitness in decending order
        Arrays.sort(population, new SortbyFitness());

        // Only take the first part of the population that had the highest fitness
        int keepSize = (population.length < popSize/5) ? population.length : (int)(population.length * nextGenRatio);
        keepSize = Math.min(popSize, keepSize);
        // Now that we have bred the new population, we can delete the old one.
        try {
            for (int i=keepSize; i<population.length; i++) {
                FileUtils.deleteDirectory(population[i].location);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Arrays.copyOfRange(population, 0, keepSize);
    }

    public int getBreadth(Constraint constraint) {
        if (constraint == null)
            return 0;
        int score = 0; // insentive to get rid of constraints that do nothing
        if (constraint.getNullProb() != null)
			score++;
		if (constraint.getProb() != null)
            score++;
        if (constraint.isNegative() != null && !constraint.isNegative())
            score++;
        if (constraint.getLowerBound() != null) // Eventually this should vary based on the size of the range
            score++;
        if (constraint.getUpperBound() != null)
            score++;
        return score;
    }

    // Returns fitness evaluation for the percentage of the time that choices fit the config's constraints
    private double evaluateFitness(Member subject, Member example) {
        // Iterate through scopes in choices
        // For each target in choices, find relavent constraint in constraints
        // Evaluate if the choices would have been possible given the constraints
        int enforce = 0;
        int deter = 0;
        Config subjectConfig = subject.extractConfig();
        for (Choice choice : example.extractConfig().getChoices()) {
            // System.out.println(choice.getTarget());
            Constraint response = subjectConfig.findConstraintFor(choice.getTarget());
            int validity = choice.rateValidity(response);
            // System.out.println(validity);
            enforce += validity;
        }
        //     Constraint constraint = subject.extractConfig().findConstraintFor(scope.getTarget());
        //     if (constraint == null) {
        //         continue;
        //     }
        // }
        // System.out.println();
        return (validChoiceWeight * enforce) - deter;
    }

    private Config[] breed(Member[] population) {
        // Break members into random pairs and combine into two opposite offspring
        // Random change of another pair of kids based on their fitness function

        Config[] nextGeneration = new Config[popSize];

        for (int childIndex = 0; childIndex < nextGeneration.length; childIndex++) {
            Member p1 = population[(int)(Math.random() * population.length-1)];
            Member p2 = population[(int)(Math.random() * population.length-1)];
            nextGeneration[childIndex] = createChild(p1.extractConfig(), p2.extractConfig());
        }

        return nextGeneration;
    }

    private void simulate(Config[] nextPop) {
        // For each config, run a test using it
        for (Config config : nextPop) {
            Runner runner = new Runner(this.workspace.method, 1, config);
            runner.execute();
        }
    }

    private static Config createChild(Config t1, Config t2) {
        ConfigTree tree = new ConfigTree(mergeDNA(t1.getTree().getRoot(), t2.getTree().getRoot()));
        // int size = tree.getRoot().getChildren().size();
        if (should(0))
            ConfigUtil.minimize(tree);
        // if (size != tree.getRoot().getChildren().size()) {
        //     String path = "/Users/Rockett/Projects/Fuzzing/JavaFuzzing/ExampleMavenProject/demo/target/";
        //     ConfigUtil.log(path, new Config(tree));
        //     System.out.println(path);
        // }
        return new Config(tree);
	}

	// Merges two trees together, overriding the first tree with the second where applicable
	private static Scope mergeDNA(Scope s1, Scope s2) {

        Scope node = new Scope();
        node.setTarget(should(0.5) ? s1.getTarget() : s2.getTarget());
        node.setConstraint(should(0.5) ? s1.getConstraint() : s2.getConstraint());

        if (should(0.05)) { // mutate target
            mutate(node.getTarget());
            // if (node.getTarget().isEmpty())
            //     return null;
        } if (should(0.05)) { // mutate constraint
            mutate(node.getConstraint());
        }

        // Maybe can replace with a while loop that has a catch for when one runs out.

        int i=0;
        while (true) { // While true is a bit weird but actually slightly efficient because of two conditionals
            if (i >= s1.getChildren().size()) {
                if (should(0.3)) {
                    for (Scope child : s2.getChildren()) {
                        node.addChild(child);
                    }
                }
                break;
            } else if (i >= s2.getChildren().size()) {
                if (should(0.3)) {
                    for (Scope child : s2.getChildren()) {
                        node.addChild(child);
                    }
                }
                break;
            }

            if (should(0.05)) { // Chance to delete node but keep grandkids
                Scope dominant = (should(0.5)) ? s1 : s2;
                node.getChildren().addAll(dominant.getChildren().get(i).getChildren());
            } else node.addChild(mergeDNA(s1.getChildren().get(i), s2.getChildren().get(i)));
            i++;
        }

        return node;
    }

    private static boolean should(double prob) {
        return Math.random() < prob;
    }

    private static Target mutate(Target target) {
        if (target == null)
            return null;
        if (target.getInstancePath() != null && should(0.5)) { // instance path
            if (should(0.5)) { // remove from left
                int index = target.getInstancePath().indexOf('.');
                if (index != -1) {
                    target.setInstancePath(target.getInstancePath().substring(index));
                } else target.setInstancePath(null);
            } else { // remove from right
                int index = target.getInstancePath().lastIndexOf('.');
                if (index != -1) {
                    target.setInstancePath(target.getInstancePath().substring(0, index));
                } else target.setInstancePath(null);
            }
        } if (target.getTypeName() != null && should(0.5)) {
            target.setTypeName(null);
        }
        return target;
    }

    private static Constraint mutate(Constraint constraint) {
        if (constraint == null || should(0.1))
            return null;
        if (should(0.2) && constraint.isNegative() != null)
            constraint.setNegative(null);
        if (should(0.2) && constraint.getLowerBound() != null)
            constraint.setLowerBound(null);
        if (should(0.2) && constraint.getUpperBound() != null)
            constraint.setUpperBound(null);
        if (should(0.2) && constraint.getNullProb() != null)
            constraint.setNullProb(null);
        if (should(0.2) && constraint.getProb() != null)
            constraint.setProb(null);
        return constraint;
    }


    private class Member {
        File location;
        double fitness;

        Config config;

        public Member(File location) {
            this.location = location;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public Config extractConfig() {
            if (this.config == null) {
                String configFilePath = Paths.get(this.location.getPath(), "config.xml").toString();
                this.config = ConfigUtil.createConfigFromFile(configFilePath);
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
            return Double.compare (b.fitness, a.fitness);
        }
    }
}
