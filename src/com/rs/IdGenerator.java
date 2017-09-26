package com.rs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class IdGenerator {

    private static final Logger LOG = Logger.getLogger(IdGenerator.class.getName());

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();


    private static final List<String> ID_CHARACTERS = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "y", "x", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

    private static final int ID_CHARACTERS_LENGTH = ID_CHARACTERS.size();

    private static final int MAX_ID_LENGTH = 4;

    private final File stationsDir;

    private volatile boolean usedIdsLoaded;
    private final Set<String> usedIds = Collections.synchronizedSet(new HashSet<>());

    //private static final IdGenerator INSTANCE = new IdGenerator();

    private final BlockingQueue<String> blockingQueue;

     IdGenerator(File resourceDirectory) {
        blockingQueue = new ArrayBlockingQueue<>(1000);
        ScheduledExecutorService ID_GENERATOR_POOL = Executors.newScheduledThreadPool(1);
        ID_GENERATOR_POOL.scheduleAtFixedRate(new IdGeneratorTask(), 0, 1, TimeUnit.SECONDS);
        this.stationsDir = resourceDirectory;
    }

    //public static IdGenerator getInstance() {
      //  return INSTANCE;
    //}

    public String getId() {
        try {
            return blockingQueue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("");
        }
    }

    private List<String> getIdFromDirectories() {

        List<String> ids = new ArrayList<>();
        File[] files = stationsDir.listFiles((dir, name) -> dir.isDirectory()
                && name != null && name.length() == MAX_ID_LENGTH);

        for (File file : files) {
            ids.add(file.getName());
        }

        return ids;
    }

    private class IdGeneratorTask implements Runnable {
        static final int GENERATE_ID_BATCH_SIZE = 100;

        @Override
        public void run() {

            if (!usedIdsLoaded) { //FIXME
                List<String> idFromDirectories = getIdFromDirectories();
                LOG.fine("idGenerator loading idFromDirectories=" + idFromDirectories.size());
                usedIdsLoaded = true;
            }

            Set<String> result = new HashSet<>();

            while (!Thread.interrupted()) {
                int index1 = ThreadLocalRandom.current().nextInt(0, ID_CHARACTERS_LENGTH);
                int index2 = ThreadLocalRandom.current().nextInt(0, ID_CHARACTERS_LENGTH);
                int index3 = ThreadLocalRandom.current().nextInt(0, ID_CHARACTERS_LENGTH);
                int index4 = ThreadLocalRandom.current().nextInt(0, ID_CHARACTERS_LENGTH);

                String s = ID_CHARACTERS.get(index1);
                String s1 = ID_CHARACTERS.get(index2);
                String s2 = ID_CHARACTERS.get(index3);
                String s3 = ID_CHARACTERS.get(index4);
                String test = s + s1 +
                        s2 + s3;

                if (usedIds.add(test)) {//Return true  if this set did not already contain the specified element
                    result.add(test);
                }

                if (result.size() == GENERATE_ID_BATCH_SIZE) {
                    break;
                }
            }

            blockingQueue.addAll(result);
        }
    }
}
