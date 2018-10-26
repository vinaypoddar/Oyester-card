package com.vinay.repository;

import org.springframework.stereotype.Repository;

import com.vinay.model.Barrier;

import java.util.HashMap;
import java.util.Map;

@Repository
public class JourneyRepository {
    private Map<Long, Barrier> mostRecentTubeBarrierPassing = new HashMap<>();

    public void addJourney(Long cardNum, Barrier barrier) {
        mostRecentTubeBarrierPassing.put(cardNum, barrier);
    }

    public Barrier getMostRecentTubeBarrierPassed(Long cardNum) {
        return mostRecentTubeBarrierPassing.get(cardNum);
    }
}
