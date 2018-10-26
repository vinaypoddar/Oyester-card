package com.vinay.service.impl;

import com.vinay.exception.CardNotFoundException;
import com.vinay.exception.IllegalParameterException;
import com.vinay.exception.InsufficientCardBalanceException;
import com.vinay.model.Barrier;
import com.vinay.repository.JourneyRepository;
import com.vinay.service.BarrierService;
import com.vinay.service.CardService;
import com.vinay.utils.UtilContants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.vinay.model.Barrier.Direction;
import static com.vinay.model.Barrier.Type;

import java.util.Set;

@Service
public class BarrierServiceImpl implements BarrierService {

    private JourneyRepository journeyRepository;

    private CardService cardService;
    
    @Autowired
    public BarrierServiceImpl(JourneyRepository journeyRepository, CardService cardService) {
		this.journeyRepository = journeyRepository;
		this.cardService = cardService;
	}

	@Override
    public void passBarrier(Barrier barrier, Long cardNum)
            throws InsufficientCardBalanceException, IllegalParameterException, CardNotFoundException {
        Barrier mostRecentTubeBarrierPassed = journeyRepository.getMostRecentTubeBarrierPassed(cardNum);
        verifyTripIsValid(barrier, mostRecentTubeBarrierPassed);

        if(isUserDueRefund(mostRecentTubeBarrierPassed)) {
        	cardService.addBalance(cardNum ,UtilContants.MAX_COST);
        }

        Double cost = getCharge(barrier, cardNum);

        if(cardService.getCard(cardNum).getBalance() < cost) {
            throw new InsufficientCardBalanceException("Not enough money on card to pay fare");
        }

        cardService.deductBalance(cardNum, cost);

        if(barrier.getType() == Type.TUBE) {
            journeyRepository.addJourney(cardNum, barrier);
        }
    }

    @Override
    public boolean mustHaveCrossedZoneOne(Set<Integer> from, Set<Integer> to, int minZonesCrossed) {
        return (from.size() == 1 && from.contains(1)) || (to.size() == 1 && to.contains(1));

    }

    @Override
    public int getMinZonesCrossed(Set<Integer> from, Set<Integer> to) {
        int minZonesVisited = Integer.MAX_VALUE;

        outerloop:
        for(int fromZone : from) {
            for(int toZone: to) {
                int zonesVisited = Math.abs(fromZone - toZone) + 1;
                if(zonesVisited < minZonesVisited)
                    minZonesVisited = zonesVisited;

                if(minZonesVisited == 1)
                    break outerloop;
            }
        }
        return minZonesVisited;
    }

    private boolean isUserDueRefund(Barrier mostRecentTubeBarrierPassed) {
        return mostRecentTubeBarrierPassed != null && mostRecentTubeBarrierPassed.getDirection() == Direction.IN;
    }

    private Double getCharge(Barrier barrier, Long cardNum) {
        if(barrier.getType() == Type.BUS)
            return UtilContants.BUS_COST;

        Barrier tubeBarrierComingFrom = journeyRepository.getMostRecentTubeBarrierPassed(cardNum);
        if(isStartingTubeJourney(tubeBarrierComingFrom))
            return UtilContants.MAX_COST;

        int minZonesCrossed = getMinZonesCrossed(tubeBarrierComingFrom.getZones(), barrier.getZones());
        boolean zoneOneCrossed = mustHaveCrossedZoneOne(tubeBarrierComingFrom.getZones(), barrier.getZones(), minZonesCrossed);

        return getFare(minZonesCrossed, zoneOneCrossed);
    }

    private Double getFare(int minZonesCrossed, boolean zoneOneCrossed) {
        if(minZonesCrossed == 1 && zoneOneCrossed)
            return UtilContants.COST_ONLY_ZONE_ONE;
        if(minZonesCrossed == 1 && !zoneOneCrossed)
            return UtilContants.COST_ONE_ZONE_NOT_INCLUDING_ZONE_ONE;
        if(minZonesCrossed == 2 && zoneOneCrossed)
            return UtilContants.COST_TWO_ZONES_INCLUDING_ZONE_ONE;
        if(minZonesCrossed == 2 && !zoneOneCrossed)
            return UtilContants.COST_TWO_ZONES_EXCLUDING_ZONE_ONE;
        if(minZonesCrossed == 3)
            return UtilContants.MAX_COST;
        return UtilContants.MAX_COST;
    }

    private boolean isStartingTubeJourney(Barrier comingFromTubeBarrier) {
        return comingFromTubeBarrier == null || comingFromTubeBarrier.getDirection() == Direction.OUT;
    }

    private void verifyTripIsValid(Barrier barrier, Barrier mostRecentTubeBarrierPassed) throws IllegalParameterException {
        if(barrier.getDirection() == Direction.OUT && (isStartingTubeJourney(mostRecentTubeBarrierPassed)))
            throw new IllegalParameterException("You can't tap out when you never tapped in.");
    }
}