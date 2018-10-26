package com.vinay.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.vinay.exception.CardNotFoundException;
import com.vinay.model.Barrier;
import com.vinay.model.Barrier.Direction;
import com.vinay.model.Barrier.Type;
import com.vinay.model.Card;
import com.vinay.repository.JourneyRepository;
import com.vinay.service.CardService;
import com.vinay.utils.UtilConstants;

@RunWith(MockitoJUnitRunner.class)
public class BarrierServiceImplTests {

    private BarrierServiceImpl barrierService;

    @Mock
    private JourneyRepository journeyRepository;
    
    @Mock
    private CardService cardService;

    @Mock
    private Card card;

    @Before
    public void setup() throws CardNotFoundException {
        barrierService = new BarrierServiceImpl(journeyRepository, cardService);
        
        when(card.getNumber()).thenReturn(UtilConstants.TEST_CARD_NUMBER);
        when(cardService.getCard(UtilConstants.TEST_CARD_NUMBER)).thenReturn(card);
        when(card.getBalance()).thenReturn(UtilConstants.TEST_BALANCE);
    }

    @Test
    public void attemptToPassBarrier_shouldRemoveMaxFareWhenPassingInForTubeJourney() throws Exception {
        Barrier barrier = new Barrier("Holborn", generateZoneSetWithValues(1), Type.TUBE, Direction.IN);
        barrierService.passBarrier(barrier, card.getNumber());

        verify(cardService).deductBalance(card.getNumber(), UtilConstants.MAX_COST);
    }

    @Test
    public void attemptToPassBarrier_shouldRefundPreviousIntoTubeFareIfNewFareIsOutOfTube() throws Exception {
        Barrier previousBarrier = new Barrier("Holborn", generateZoneSetWithValues(1), Type.TUBE, Direction.IN);
        Barrier currentBarrier = new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.OUT);
        when(journeyRepository.getMostRecentTubeBarrierPassed(UtilConstants.TEST_CARD_NUMBER)).thenReturn(previousBarrier);
        barrierService.passBarrier(currentBarrier, card.getNumber());

        verify(cardService).addBalance(card.getNumber(), UtilConstants.MAX_COST);
    }

    @Test
    public void attemptToPassBarrier_shouldAddPassToJourneyRepository() throws Exception {
        Barrier currentBarrier = new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.IN);
        barrierService.passBarrier(currentBarrier, card.getNumber());

        verify(cardService, never()).addBalance(card.getNumber(), UtilConstants.MAX_COST);
    }

    @Test
    public void attemptToPassBarrier_shouldNotRefundFareIfNoPreviousJourney() throws Exception {
        Barrier currentBarrier = new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.IN);
        when(cardService.getCard(UtilConstants.TEST_CARD_NUMBER)).thenReturn(card);
        when(card.getBalance()).thenReturn(UtilConstants.TEST_BALANCE);
        barrierService.passBarrier(currentBarrier, card.getNumber());
        verify(cardService, never()).addBalance(card.getNumber(), UtilConstants.MAX_COST);
    }

    @Test
    public void attemptToPassBarrier_shouldNotRefundFareIfPreviousTubeBarrierWasExit() throws Exception {
        Barrier previousBarrier = new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.OUT);
        Barrier currentBarrier = new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.IN);
        when(journeyRepository.getMostRecentTubeBarrierPassed(UtilConstants.TEST_CARD_NUMBER)).thenReturn(previousBarrier);
        when(cardService.getCard(UtilConstants.TEST_CARD_NUMBER)).thenReturn(card);
        when(card.getBalance()).thenReturn(UtilConstants.TEST_BALANCE);
        barrierService.passBarrier(currentBarrier, card.getNumber());

        verify(cardService, never()).addBalance(card.getNumber(), UtilConstants.MAX_COST);
    }

    @Test
    public void attemptToPassBarrier_shouldOnlyCharge180ForBusJourney() throws Exception {
        Barrier busBarrier = new Barrier("Any", generateZoneSetWithValues(1), Type.BUS, Direction.IN);
        barrierService.passBarrier(busBarrier, card.getNumber());

//        verify(cardService, never()).addBalance(card.getNumber(), anyDouble());
        verify(cardService).deductBalance(card.getNumber(), UtilConstants.BUS_COST);
    }

    @Test
    public void minZonesCrossed_shouldReturnOneZoneIfOneZoneCrossed() {
        int expectedZonesCrossed = 1;
        int actualZonesCrossed =
                barrierService.getMinZonesCrossed(generateZoneSetWithValues(2), generateZoneSetWithValues(2));

        assertEquals(expectedZonesCrossed, actualZonesCrossed);
    }

    @Test
    public void minZonesCrossed_shouldReturnMinimumZonesCrossedIfAmbiguous1() {
        int expectedZonesCrossed = 1;
        int actualZonesCrossed =
                barrierService.getMinZonesCrossed(generateZoneSetWithValues(1), generateZoneSetWithValues(1, 2));

        assertEquals(expectedZonesCrossed, actualZonesCrossed);
    }

    @Test
    public void minZonesCrossed_shouldReturnMinimumZonesCrossedIfAmbiguous2() {
        int expectedZonesCrossed = 1;
        int actualZonesCrossed =
                barrierService.getMinZonesCrossed(generateZoneSetWithValues(1, 2), generateZoneSetWithValues(2, 3));

        assertEquals(expectedZonesCrossed, actualZonesCrossed);
    }

    @Test
    public void mustHaveCrossedZoneOne_shouldReturnTrueIfOnlyInZoneOne() {
        boolean expectedCrossed = true;
        boolean actualCrossed = barrierService
                .mustHaveCrossedZoneOne(generateZoneSetWithValues(1), generateZoneSetWithValues(1), 1);

        assertEquals(expectedCrossed, actualCrossed);
    }

    @Test
    public void mustHaveCrossedZoneOne_shouldReturnFalseIfAmbiguous1() {
        boolean expectedCrossed = false;
        boolean actualCrossed = barrierService
                .mustHaveCrossedZoneOne(generateZoneSetWithValues(1, 2), generateZoneSetWithValues(2, 3), 2);

        assertEquals(expectedCrossed, actualCrossed);
    }

    @Test
    public void mustHaveCrossedZoneOne_shouldReturnFalseIfAmbiguous2() {
        boolean expectedCrossed = false;
        boolean actualCrossed = barrierService
                .mustHaveCrossedZoneOne(generateZoneSetWithValues(1, 2), generateZoneSetWithValues(2, 3), 1);

        assertEquals(expectedCrossed, actualCrossed);
    }

    private Set<Integer> generateZoneSetWithValues(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
