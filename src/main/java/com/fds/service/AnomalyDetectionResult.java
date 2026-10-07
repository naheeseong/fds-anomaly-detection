package com.fds.service;

import java.util.List;

public class AnomalyDetectionResult {

    private final List<String> patternTypes;
    private final double confidenceScore;

    public AnomalyDetectionResult(List<String> patternTypes, double confidenceScore) {
        this.patternTypes = patternTypes;
        this.confidenceScore = confidenceScore;
    }

    public List<String> getPatternTypes() {
        return patternTypes;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public boolean isAbnormal() {
        return !patternTypes.isEmpty();
    }

    public String reasonSummary() {
        return String.join(", ", patternTypes);
    }
}
