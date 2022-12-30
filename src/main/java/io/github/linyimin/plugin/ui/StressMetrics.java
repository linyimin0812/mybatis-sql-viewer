package io.github.linyimin.plugin.ui;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author banzhe
 * @date 2022/12/27 18:08
 **/
public class StressMetrics {


    private final Map<Long, List<Long>> successMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> failedMap = new ConcurrentHashMap<>();

    private final Map<Long, Long> concurrentNumMap = new ConcurrentHashMap<>();

    private final AtomicLong success = new AtomicLong(0);
    private final AtomicLong failed = new AtomicLong(0);

    private final AtomicLong maxRt = new AtomicLong(0);
    private final AtomicLong totalRt = new AtomicLong(0);

    private final AtomicLong concurrentNum = new AtomicLong(0);

    public void addSuccess(long cost) {

        long timestamp = getTimeSeconds();

        synchronized (successMap) {
            List<Long> costs = successMap.getOrDefault(timestamp, new ArrayList<>());
            costs.add(cost);
            successMap.put(timestamp, costs);

            if (cost > maxRt.get()) {
                maxRt.set(cost);
            }
        }

        concurrentNumMap.put(timestamp, this.concurrentNum.get());
        success.incrementAndGet();
        totalRt.addAndGet(cost);
    }

    public void addFailed() {
        long timestamp = getTimeSeconds();

        synchronized (failedMap) {
            long count = failedMap.getOrDefault(timestamp, 0L);
            count++;

            failedMap.put(timestamp, count);
        }
        concurrentNumMap.put(timestamp, this.concurrentNum.get());
        failed.incrementAndGet();
    }

    public synchronized void setConcurrentNum(int count) {
        this.concurrentNum.set(count);
    }

    public String successRate() {

        long success = this.success.get();
        long failed = this.failed.get();

        if (success + failed == 0) {
            return "0.00";
        }

        return String.format("%.2f", 100.0 * success / (success + failed));
    }

    public Map<Long, Double> successRateMap() {

        Map<Long, List<Long>> successMapCopy = copySuccessMap();
        Map<Long, Long> failedMapCopy;

        synchronized (failedMap) {
            failedMapCopy = new HashMap<>(failedMap);
        }

        Map<Long, Double> result = new HashMap<>();

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {
            double rate = 100.0 * entry.getValue().size() / (entry.getValue().size() + failedMapCopy.getOrDefault(entry.getKey(), 0L));
            result.put(entry.getKey(), rate);
        }

        return result;
    }

    public String averageRt() {
        long success = this.success.get();
        long cost = this.totalRt.get();

        if (success == 0) {
            return "0.00";
        }

        return String.format("%.2f", 1.0 * cost / success);
    }

    public String tps() {

        long success = this.success.get();
        int size = successMap.size();

        if (size == 0) {
            return "0";
        }

        return String.valueOf(success / size);
    }

    public String failedCount() {
        return String.valueOf(failed.get());
    }

    public String total() {
        return String.valueOf(success.get() + failed.get());
    }

    private long getTimeSeconds() {
        long timestamp = System.currentTimeMillis();

        return timestamp - (timestamp % 1000);
    }

    public Map<Long, List<Long>> copySuccessMap() {
        Map<Long, List<Long>> successMapCopy = new HashMap<>();

        synchronized (successMap) {

            for (Map.Entry<Long, List<Long>> entry : successMap.entrySet()) {
                List<Long> value = new ArrayList<>(entry.getValue());
                successMapCopy.put(entry.getKey(), value);
            }
        }

        return successMapCopy;
    }

    public Map<Long, Double> averageRtMap() {

        Map<Long, Double> result = new HashMap<>();

        Map<Long, List<Long>> successMapCopy = copySuccessMap();

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {
            double average = entry.getValue().stream().mapToInt(Math::toIntExact).summaryStatistics().getAverage();

            result.put(entry.getKey(), average);
        }

        return result;
    }

    public Map<Long, Double> tpsMap() {
        Map<Long, Double> result = new HashMap<>();

        Map<Long, List<Long>> successMapCopy = copySuccessMap();

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {

            result.put(entry.getKey(), (double) entry.getValue().size());
        }

        return result;

    }

    public Map<Long, Long> concurrentNumMap() {

        Map<Long, Long> copyMap;

        synchronized (concurrentNumMap) {
            copyMap = new HashMap<>(concurrentNumMap);
        }

        return copyMap;
    }

    public long getConcurrentNum() {
        return this.concurrentNum.get();
    }

    public long maxRt() {
        return this.maxRt.get();
    }

    public long maxTps() {
        Map<Long, List<Long>> copy;

        synchronized (successMap) {
            copy = copySuccessMap();
        }

        return copy.values().stream().mapToLong(Collection::size).max().orElse(0);
    }

    public Pair<Long, Long> tp99And90() {

        Map<Long, List<Long>> successMapCopy = copySuccessMap();

        List<Long> costs = successMapCopy.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        int max = costs.stream().mapToInt(Math::toIntExact).max().orElse(0);
        int[] countContainer = new int[max + 1];

        for (int i = 0; i <= max; i++) {
            countContainer[i] = 0;
        }

        for (long cost : costs) {
            countContainer[(int) cost] = countContainer[(int) cost] + 1;
        }

        int tp99 = (int) (0.99 * costs.size());
        int tp90 = (int) (0.90 * costs.size());

        int total = 0;
        long left = -1;
        long right = -1;

        for (int i = 0; i < countContainer.length; i++) {
            total += countContainer[i];
            if (total >= tp90 && left == -1) {
                left = i;
            }

            if (total >= tp99 && right == -1) {
                right = i;
            }

            if (left != -1 && right != -1) {
                break;
            }
        }

        return Pair.of(left, right);

    }

}
