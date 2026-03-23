package ru.university.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadedCalc {

    private static final int NUM_THREADS = 5;
    private static final int CALC_LENGTH = 20;
    private static final int BAR_WIDTH = 30;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Запуск многопоточного расчета...");

        long[] results = new long[NUM_THREADS];
        long startTimeGlobal = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            System.out.println();
        }

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            int threadIndex = i;
            executor.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                long threadId = Thread.currentThread().getId();

                for (int step = 0; step <= CALC_LENGTH; step++) {
                    simulateCalculation();

                    double progress = (double) step / CALC_LENGTH;
                    long currentTime = System.currentTimeMillis() - threadStartTime;

                    if (step == CALC_LENGTH) {
                        results[threadIndex] = currentTime;
                    }

                    String status = (step == CALC_LENGTH)
                            ? String.format("Завершено за %d мс", currentTime)
                            : "Расчет...";

                    updateConsole(threadIndex, threadId, progress, status);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.print(String.format("\033[%dB", NUM_THREADS));

        System.out.println("\n\nИТОГОВЫЙ ОТЧЕТ:");
        for (int i = 0; i < NUM_THREADS; i++) {
            System.out.printf("Поток #%d завершил работу за %d мс%n", (i + 1), results[i]);
        }
        System.out.println("Общее время работы программы: " + (System.currentTimeMillis() - startTimeGlobal) + " мс");
    }

    private static synchronized void updateConsole(int index, long id, double progress, String status) {
        int moveUp = NUM_THREADS - index;

        StringBuilder bar = new StringBuilder("[");
        int filledTotal = (int) (progress * BAR_WIDTH);
        for (int i = 0; i < BAR_WIDTH; i++) {
            if (i < filledTotal) bar.append("#");
            else bar.append("-");
        }
        bar.append("]");

        int percent = (int) (progress * 100);

        System.out.print(String.format("\033[s\033[%dA\033[2K\rПоток #%d | ID: %-3d | %s %3d%% | %s\033[u",
                moveUp, index + 1, id, bar.toString(), percent, status));
        System.out.flush();
    }

    private static void simulateCalculation() {
        try {
            Thread.sleep(100 + (int) (Math.random() * 400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
