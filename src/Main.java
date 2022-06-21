import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    static File file = new File("input_example.txt");
    static Scanner scanner;

    static {
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    static int testCases;
    static int[] weight;
    static int[] benefit;
    static int length; //number of items
    static int capacity; //size of the knapsack

    static int pop_size;
    static int numOfGenerations;
    static int numOfParents; //in the mating pool

    static int nxt = 0;
    static double pm;
    static double pc;


    static int[][] parents;
    static int[][] offSprings;
    static double[] fitness;
    static int[] mating_pool;


    public static void main(String[] args) throws IOException {

        testCases = scanner.nextInt();
        for (int t = 0; t < testCases; t++) {
            readFromFile();
            initVariables();
            initPop();
            for (int i = 0; i < numOfGenerations; i++) {
                fitness_all();
//            display_maximum();
                select_parents(numOfParents);
                cross_over_all(numOfParents);
                mutation_all();
                Replacement();
            }

            System.out.println("Case " + (t + 1) + ": ");
            fitness_all(); //the last generation
            display_maximum();
            System.out.println("--------");
        }
    }

    static void readFromFile() throws IOException {
        length = scanner.nextInt();
        capacity = scanner.nextInt();

        weight = new int[length];
        benefit = new int[length];

        for (int i = 0; i < length; i++) {
            weight[i] = scanner.nextInt();
            benefit[i] = scanner.nextInt();
        }
    }

    static void writeToFile() {
    }

    static void initPop() {
        Random rand = new Random();
        double randVal;
        //for each individual
        for (int i = 0; i < pop_size; i++) {
            //for every gene in every individual
            for (int j = 0; j < length; j++) {
                randVal = rand.nextDouble();
                if (randVal >= 0.5) {
                    parents[i][j] = 1;
                } else
                    parents[i][j] = 0;
            }
        }
    }

    static void initVariables() {
        pop_size = length * 2;
        numOfParents = pop_size/2;
        numOfGenerations = length * 2;
        pc = 0.7;
        pm = 0.1;

        parents = new int[pop_size][length];
        fitness = new double[pop_size];
        mating_pool = new int[numOfParents];
        offSprings = new int[numOfParents+1][length];
    }

    static void fitness_all() {

        for (int i = 0; i < pop_size; i++) {

            fitness[i] = ndv_fitness(parents[i]);
            if (!feasible(parents[i])) {
                fitness[i] = 1 / fitness[i];
            }

        }
    }

    static double ndv_fitness(int[] individual) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += individual[i] * benefit[i];
        }
        return sum;
    }

    static boolean feasible(int[] individual) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += individual[i] * weight[i];
        }
        return sum <= capacity;
    }

    static void select_parents(int numOfParents) {

        boolean[] flags = new boolean[pop_size];
        for (int i = 0; i < numOfParents; i++) {

            double mx = -1;
            int mx_ndx = 0;

            for (int j = 0; j < pop_size; j++) {
                if (flags[j]) continue;
                if (fitness[j] > mx) {
                    mx = fitness[j];
                    mx_ndx = j;
                }
            }

            mating_pool[i] = mx_ndx;
            flags[mx_ndx] = true;
        }
    }

    static void cross_over_all(int numOfParents) {
        nxt = 0;
        int i;
        for (i = 0; i < numOfParents - 1; i++) {
            cross_over(mating_pool[i], mating_pool[i + 1]);
            i++;
        }
        if(i<numOfParents)
            cross_over(mating_pool[i], mating_pool[0]);

    }

    static void cross_over(int ndx1, int ndx2) {

        int f = nxt++;
        int s = nxt++;
        offSprings[f] = parents[ndx1].clone();
        offSprings[s] = parents[ndx2].clone();

        Random rand = new Random();

        double randVal1;
        int randVal2; //the single point

        randVal1 = rand.nextDouble();
        randVal2 = rand.nextInt(length);

        if (randVal1 <= pc) {
            for (int i = randVal2; i < length; i++) {
                int tmp = offSprings[f][i];
                offSprings[f][i] = offSprings[s][i];
                offSprings[s][i] = tmp;
            }

//            parents[ndx1]=offSprings[f];
//            parents[ndx2]=offSprings[s];

        }
    }

    static void mutation_all() {
        for (int i = 0; i < nxt; i++) {
            mutation(i);
        }
    }

    static void mutation(int ndx) {
        Random random = new Random();
        double r;
        for (int i = 0; i < length; i++) {
            r = random.nextDouble();
            if (r <= pm)
                offSprings[ndx][i] = (offSprings[ndx][i] * -1) + 1;
        }
    }

    static void Replacement() {

        Random random = new Random();
        int r;

        boolean[] flags = new boolean[pop_size];
        //save the best one // elitism
        flags[mating_pool[0]] = true;

        for (int i = 0; i < nxt; i++) {

            r = random.nextInt(pop_size);
            if (flags[r]) continue;
            if (!feasible(offSprings[i])) continue;
            if (ndv_fitness(parents[r]) >= ndv_fitness(offSprings[i])) continue;

            parents[r] = offSprings[i].clone();
            flags[r] = true;

        }

    }

    //Displays
    static void dispaly_parents() {
        System.out.println("Parents: ");
        for (int i = 0; i < pop_size; i++) {
            System.out.print("[");
            for (int j = 0; j < length; j++) {
                System.out.print(parents[i][j] + ", ");
            }
            System.out.println("]");
        }

    }

    static void display_fitness() {
        System.out.println("fitness: ");
        for (int i = 0; i < pop_size; i++) {
            System.out.println(fitness[i]);
        }
    }

    static void display_selected(int numOfParents) {
        System.out.println("selected: ");
        for (int i = 0; i < numOfParents; i++)
            System.out.println(mating_pool[i]);
    }

    static void display_offSprings() {

        for (int i = 0; i < nxt; i++) {
            System.out.print("[");
            for (int j = 0; j < length; j++) {

                System.out.print(offSprings[i][j] + ", ");
            }
            System.out.println("]");
        }
    }

    static void display_maximum() {

        double mx = -1;
        int mx_ndx = 0;

        for (int j = 0; j < pop_size; j++) {
            if (fitness[j] > mx) {
                mx = fitness[j];
                mx_ndx = j;
            }
        }
        System.out.println("max: " + fitness[mx_ndx]);

        //display the decoded solution
//        for (int i = 0; i < length ; i++) {
//            if(parents[mx_ndx][i]==1){
//                System.out.println(weight[i]+", "+benefit[i]);
//            }
//        }
    }


}
