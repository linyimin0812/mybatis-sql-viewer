package io.github.linyimin.plugin.ui;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author banzhe
 * @date 2022/12/27 18:08
 **/
public class StressMetrics {

    private final Map<Long, List<Long>> successMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> failedMap = new ConcurrentHashMap<>();

    private final Map<Long, Double> successRateMap = new ConcurrentHashMap<>();
    private final Map<Long, Double> averageRtMap = new ConcurrentHashMap<>();
    private final Map<Long, Double> tpsMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> concurrentNumMap = new ConcurrentHashMap<>();

    private long successMapProcessTimeSecond = 0;
    private long averageRtMapProcessTimeSecond = 0;
    private long tpsMapProcessTime = 0;

    private final AtomicLong success = new AtomicLong(0);
    private final AtomicLong failed = new AtomicLong(0);

    private final AtomicLong maxRt = new AtomicLong(0);
    private final AtomicLong totalRt = new AtomicLong(0);

    private final AtomicLong concurrentNum = new AtomicLong(0);

    private final int[] SCALE = new int[2400];
    private final int[] COUNT_CONTAINER = new int[2400];

    public StressMetrics() {
        initScaleAndCountContainer();
    }

    public void addConcurrentNum() {
        long timestamp = getTimeSeconds();
        this.concurrentNumMap.put(timestamp, this.concurrentNum.get());
    }

    public void addSuccess(long cost) {

        long timestamp = getTimeSeconds();

        synchronized (successMap) {
            List<Long> costs = successMap.getOrDefault(timestamp, new ArrayList<>());
            costs.add(cost);
            successMap.put(timestamp, costs);

            if (cost > maxRt.get()) {
                maxRt.set(cost);
            }

            addCostCount((int) cost);
        }

        success.incrementAndGet();
        totalRt.addAndGet(cost);

        concurrentNumMap.put(timestamp, this.concurrentNum.get());
    }

    public void addFailed() {
        long timestamp = getTimeSeconds();

        synchronized (failedMap) {
            long count = failedMap.getOrDefault(timestamp, 0L);
            count++;

            failedMap.put(timestamp, count);
        }

        failed.incrementAndGet();

        concurrentNumMap.put(timestamp, this.concurrentNum.get());
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

        long currentTimeSecond = getTimeSeconds();

        Map<Long, List<Long>> successMapCopy = copySuccessMap(this.successMapProcessTimeSecond, currentTimeSecond);
        Map<Long, Long> failedMapCopy = copyFailedMap(this.successMapProcessTimeSecond, currentTimeSecond);

        this.successMapProcessTimeSecond = currentTimeSecond;

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {
            double rate = 100.0 * entry.getValue().size() / (entry.getValue().size() + failedMapCopy.getOrDefault(entry.getKey(), 0L));
            this.successRateMap.put(entry.getKey(), rate);
        }

        return this.successRateMap;
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

    private Map<Long, List<Long>> copySuccessMap(long processedTimeSecond, long currentTimeSecond) {

        Map<Long, List<Long>> successMapCopy = new HashMap<>();

        if (processedTimeSecond == 0) {
            for (Map.Entry<Long, List<Long>> entry : successMap.entrySet()) {
                List<Long> value = new ArrayList<>(entry.getValue());
                successMapCopy.put(entry.getKey(), value);
            }
        } else {
            synchronized (this.successMap) {
                for (long seconds = processedTimeSecond; seconds <= currentTimeSecond; seconds += 1000) {
                    if (this.successMap.containsKey(seconds)) {
                        List<Long> value = new ArrayList<>(this.successMap.get(seconds));
                        successMapCopy.put(seconds, value);
                    }
                }
            }
        }

        return successMapCopy;
    }

    private Map<Long, Long> copyFailedMap(long processedTimeSecond, long currentTimeSecond) {

        Map<Long, Long> failedMapCopy = new HashMap<>();

        if (processedTimeSecond == 0) {
            synchronized (failedMap) {
                failedMapCopy = new HashMap<>(failedMap);
            }
        } else {

            synchronized (this.failedMap) {
                for (long seconds = processedTimeSecond; seconds <= currentTimeSecond; seconds += 1000) {
                    if (this.failedMap.containsKey(seconds)) {
                        failedMapCopy.put(seconds, this.failedMap.get(seconds));
                    }
                }
            }
        }

        return failedMapCopy;
    }

    public Map<Long, Double> averageRtMap() {

        long currentSeconds = getTimeSeconds();

        Map<Long, List<Long>> successMapCopy = copySuccessMap(this.averageRtMapProcessTimeSecond, currentSeconds);

        this.averageRtMapProcessTimeSecond = currentSeconds;

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {
            double average = entry.getValue().stream().mapToInt(Math::toIntExact).summaryStatistics().getAverage();

            this.averageRtMap.put(entry.getKey(), average);
        }

        return averageRtMap;
    }

    public Map<Long, Double> tpsMap() {

        long currentTimeSecond = getTimeSeconds();

        Map<Long, List<Long>> successMapCopy = copySuccessMap(this.tpsMapProcessTime, currentTimeSecond);

        this.tpsMapProcessTime = currentTimeSecond;

        for (Map.Entry<Long, List<Long>> entry : successMapCopy.entrySet()) {

            this.tpsMap.put(entry.getKey(), (double) entry.getValue().size());
        }

        return this.tpsMap;

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
        return (long) this.tpsMap().values().stream().mapToDouble(x -> x).max().orElse(0);
    }

    public Pair<Long, Long> tp99And90() {

        long tp99 = (long) (this.success.get() * 0.99);
        long tp90 = (long) (this.success.get() * 0.90);

        long left = -1;
        long right = -1;

        long total = 0;
        for (int i = 0; i < 2400; i++) {
            total += COUNT_CONTAINER[i];
            if (total >=tp90 && left == -1) {
                left = SCALE[i];
            }

            if (total >= tp99 && right == -1) {
                right = SCALE[i];
            }

            if (left != -1 && right != -1) {
                break;
            }
        }

        return Pair.of(left, right);

    }

    private void initScaleAndCountContainer() {
        for (int i = 0; i < 1000; i++) {
            SCALE[i] = i;
            COUNT_CONTAINER[i] = 0;
        }
        for (int i = 1000; i < 1900; i++) {
            SCALE[i] = (i - 1000 + 1) * 10 + 1000;
            COUNT_CONTAINER[i] = 0;
        }

        for (int i = 1900; i < 2400; i++) {
            SCALE[i] = (i - 1900 + 1) * 100 + 10000;
            COUNT_CONTAINER[i] = 0;
        }
    }

    private void addCostCount(int cost) {
        int index = 0;
        if (cost < 1000) {
            index = cost;
        } else if (cost < 10000) {
            index = (cost - 1000) / 10 + 1000;
        } else {
            index = (cost - 10000) / 100 + 1900;
        }

        if (index >= 2400) {
            index = 2399;
        }

        COUNT_CONTAINER[index] = COUNT_CONTAINER[index] + 1;

    }

}
