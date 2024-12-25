package ict.minesunshineone.uid.monitoring;

import java.util.ArrayList;
import java.util.List;

public class OperationStats {

    private final int maxSamples;
    private final List<Double> samples;
    private long count;
    private double sum;

    public OperationStats(int maxSamples) {
        this.maxSamples = maxSamples;
        this.samples = new ArrayList<>();
        this.count = 0;
        this.sum = 0.0;
    }

    public synchronized void addSample(double duration) {
        count++;
        sum += duration;

        if (samples.size() >= maxSamples) {
            sum -= samples.remove(0);
        }
        samples.add(duration);
    }

    public double getAverage() {
        return samples.isEmpty() ? 0.0 : sum / samples.size();
    }

    public long getCount() {
        return count;
    }
}
