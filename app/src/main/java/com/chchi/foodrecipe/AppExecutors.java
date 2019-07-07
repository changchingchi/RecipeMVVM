package com.chchi.foodrecipe;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AppExecutors {

    private static AppExecutors instance;
    private final ScheduledExecutorService networkIO = Executors.newScheduledThreadPool(3);

    private AppExecutors() {
    }

    public static final AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public ScheduledExecutorService networkIO() {
        return networkIO;
    }
}
