package app;

import abstractClass.AbstractAlgorithm;
import algorithms.Bruteforce;
import algorithms.Greedy;
import algorithms.RandomSolution;
import knapSack.Instance;
import knapSack.Item;
import knapSack.Solution;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class App implements AppMBean {

    private static int memorySize = 500;
    private static Integer counterThread = new Integer(0); // rzeczywista ilosc watkow
    private Integer numberOfThread = new Integer(0); // liczba watkow do ktorej dazymy
    private static Integer threadsToKill = new Integer(0);// ile watkow ma sie zabic

    private static DecimalFormat df = new DecimalFormat("0.00");

    private static boolean algorithmsLoaded = false;
    private static final Random generator = new Random();

    private static List<Long> seeds = new ArrayList<>();
    private static List<Thread> customersThread = new ArrayList<>();
    private static List<AbstractAlgorithm> algorithms = new ArrayList<>(Arrays.asList(new Bruteforce(), new RandomSolution()));
    // private static WeakHashMap<Long, Solution> map = new WeakHashMap<>();
    private static HashMap<Long, Solution> map = new HashMap<>();
    private static Integer counterRef = new Integer(0);
    private static Integer counterFallRef = new Integer(0);
    private static double statRate = 0;




    public static void generateSeeds(int n) {
        for (int i = 0; i < n; i++) {
            seeds.add((long) new Random().nextInt(50000) + 2000);
        }
    }

    public static Instance generateInstance(long seedLocal) {

        int capacity = (int) (seedLocal % 20 + 3);
        List<Item> listItems = new ArrayList<>();

        for (int i = 10; i < 120; i += 10) {
            //float profit, int weight
            Item item = new Item((float) (seedLocal % i + 2), (int) seedLocal % i);
            listItems.add(item);
        }
        Instance instance = new Instance(listItems, capacity);
        return instance;
    }
    
    public static void createCostumerThread() {

        Thread th = new Thread(() -> {
            ///int idxThread = _idxThread;
            // zwikszenie liczby watkow
            synchronized (counterThread){
                counterThread++;
            }
            while (true) {

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                String msg = "";
                int idx = new Random().nextInt(algorithms.size());
                long seedLocal = seeds.get(new Random().nextInt(seeds.size() - 1));
                synchronized (counterRef) {
                    counterRef++;
                }
                if (!map.containsKey(seedLocal)) {
                    synchronized (counterFallRef) {
                        counterFallRef++;
                    }
                    Instance instance = generateInstance(seedLocal);
                    Solution solution = algorithms.get(idx).algorithm(instance);

                    // map.put(seedLocal,solution);
                    // nowa funkcja ktora zadba o nie przekraczanie pamieci
                    putForMap(seedLocal, solution);
                    msg ="Watek: " + Thread.currentThread().getId() + " Seed: " + seedLocal + " Algorytm: " + algorithms.get(idx).getClass().toString().substring(17) + " Pojemnosc: " + instance.getCapacity() + " Caly profit: " + solution.getAllProfit();
                }


                synchronized (System.out) {
                  System.out.println(msg);

                }

                synchronized (threadsToKill){
                    if(threadsToKill > 0){
                        synchronized (counterThread){
                            counterThread--;
                        }
                        threadsToKill--;
                        break; // wychodze z petli while ikoncze watek
                    }
                }
            }
        });

        // uruchomienie
        th.start();
        // customersThread.add(th);
    }

    private static void putForMap(Long seedLocal, Solution solution) {
        // jak juz osiagniemy maks to musi usunac randomowy
        synchronized (map) {
            while (memorySize <= map.size()) {
                // dla to array podaje sie tablicedo ktorej zwracam
                Long[] keySeed = map.keySet().toArray(new Long[0]);
                Long key = keySeed[ThreadLocalRandom.current().nextInt(0, keySeed.length)];
                map.remove(key);
            }

            map.put(seedLocal, solution);

        }
    }

    @Override
    public void setThread(int idx) {
        int d; // roznica

        // najpierw korzystamy do ktorej chcamy dazyc
        synchronized (numberOfThread) {
            numberOfThread = idx;
            // synchronizacja do zabicia
            synchronized (threadsToKill) {
                // synchronizacja po rzeczywistej
                synchronized (counterThread) {
                    d = counterThread - numberOfThread;
                    if (d > 0)
                        threadsToKill = d; // kazdy watek odpytuje ta wartosc i jak jest wieksza od zera to sie zbaije
                }

            }
            // jezlei brakuje watkow trzeba utworzyc nowe
            if (d < 0) {
                for (int i = 0; i > d; i--) {
                    createCostumerThread();
                }
            }
        }
    }

    @Override
    public int getThread() {
        synchronized (numberOfThread) {
            return numberOfThread;
        }
    }

    @Override
    public void setMemorySize(int i) {

        if (i > 0) {
            synchronized (map) {
                memorySize = i;
                while (memorySize < map.size()) {
                    // dla to array podaje sie tablicedo ktorej zwracam
                    // memorySize ejst mniejszy wiec musimy usunac losowe rozwiazanie
                    Long[] keySeed = map.keySet().toArray(new Long[0]);
                    Long key = keySeed[ThreadLocalRandom.current().nextInt(0, keySeed.length)];
                    map.remove(key);
                }
            }
        }

    }

    @Override
    public int getMemorySize() {
        return memorySize;
    }

    @Override
    public String getDescription() {
        int freeSpace; // wolne wpisy
        int busySpace; // zajete wpisy
        int cThread; //liczba watkow
        double missRatio = 0 ;

        synchronized (map){
            busySpace = map.size();
            freeSpace = memorySize - busySpace;

            synchronized (counterFallRef){
                synchronized (counterFallRef){
                    if(counterRef > 0) missRatio = (counterFallRef / (double) counterRef) * 100;
                }
            }
        }

        synchronized (counterThread) {
            cThread = counterThread;
        }


        return "Liczba wolnych wpisow: " + freeSpace +
                " liczba zajetych wpisow: " + busySpace +
                " liczba watkow: " + cThread +
                " procent chybien: " + String.format("%.2f", missRatio);
    }
}
