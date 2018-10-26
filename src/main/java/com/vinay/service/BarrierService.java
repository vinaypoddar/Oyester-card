package com.vinay.service;

import com.vinay.exception.CardNotFoundException;
import com.vinay.exception.IllegalParameterException;
import com.vinay.exception.InsufficientCardBalanceException;
import com.vinay.model.Barrier;

import java.util.Set;

public interface BarrierService {
    void passBarrier(Barrier barrier, Long cardNum)
            throws InsufficientCardBalanceException, IllegalParameterException, CardNotFoundException;
    int getMinZonesCrossed(Set<Integer> from, Set<Integer> to);
    boolean mustHaveCrossedZoneOne(Set<Integer> from, Set<Integer> to, int minZonesCrossed);
}
