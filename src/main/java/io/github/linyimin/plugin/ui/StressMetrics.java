package io.github.linyimin.plugin.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author banzhe
 * @date 2022/12/27 18:08
 **/
public class StressMetrics {


    private final Map<Long, List<Long>> successMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> failedMap = new ConcurrentHashMap<>();

    public void addSuccess(long cost) {

        long timestamp = getTimeSeconds();

        synchronized (successMap) {
            List<Long> costs = successMap.getOrDefault(timestamp, new ArrayList<>());
            costs.add(cost);

            successMap.put(timestamp, costs);
        }
    }

    public void addFailed() {
        long timestamp = getTimeSeconds();

        synchronized (failedMap) {
            long count = failedMap.getOrDefault(timestamp, 0L);
            count++;

            failedMap.put(timestamp, count);
        }
    }

    public String successRate() {

        long success = 0;

        List<List<Long>> values = copySuccessMapValues();
        for (List<Long> costs : values) {
            success += costs.size();
        }

        long failed = 0;

        List<Long> failedValues;
        synchronized (failedMap) {
            failedValues = new ArrayList<>(failedMap.values());
        }

        for (long count : failedValues) {
            failed += count;
        }

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
        long success = 0;
        long cost = 0;

        List<List<Long>> values = copySuccessMapValues();

        for (List<Long> costs : values) {
            success += costs.size();
            for (long requestCost : costs) {
                cost += requestCost;
            }
        }

        if (success == 0) {
            return "0.00";
        }

        return String.format("%.2f", 1.0 * cost / success);
    }

    public String tps() {

        long success = 0;

        List<List<Long>> values = copySuccessMapValues();

        for (List<Long> costs : values) {
            success += costs.size();
        }

        if (successMap.size() == 0) {
            return "0";
        }

        return String.valueOf(success / successMap.size());
    }

    public String failedCount() {
        long failed = 0;

        List<Long> failedValues;
        synchronized (failedMap) {
            failedValues = new ArrayList<>(failedMap.values());
        }

        for (long count : failedValues) {
            failed += count;
        }


        return String.valueOf(failed);
    }

    public String total() {

        List<List<Long>> values = copySuccessMapValues();

        long success = 0;
        for (List<Long> costs : values) {
            success += costs.size();
        }


        List<Long> failedValues;
        synchronized (failedMap) {
            failedValues = new ArrayList<>(failedMap.values());
        }

        long failed = 0;
        for (long count : failedValues) {
            failed += count;
        }

        return String.valueOf(success + failed);
    }

    private long getTimeSeconds() {
        long timestamp = System.currentTimeMillis();

        return timestamp - (timestamp % 1000);
    }

    private List<List<Long>> copySuccessMapValues() {

        List<List<Long>> values = new ArrayList<>();
        synchronized (successMap) {
            for (List<Long> value : successMap.values()) {
                List<Long> temp = new ArrayList<>(value);
                values.add(temp);
            }
        }

        return values;
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

}
