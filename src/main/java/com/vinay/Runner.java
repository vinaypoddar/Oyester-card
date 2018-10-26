package com.vinay;

import com.vinay.exception.CardNotFoundException;
import com.vinay.exception.IllegalParameterException;
import com.vinay.exception.InsufficientCardBalanceException;
import com.vinay.model.Barrier;
import com.vinay.model.Card;
import com.vinay.service.BarrierService;
import com.vinay.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import static com.vinay.model.Barrier.Direction;
import static com.vinay.model.Barrier.Type;
import java.util.*;

@Component
public class Runner implements CommandLineRunner {

	@Autowired
    private CardService cardService;
	@Autowired
    private BarrierService barrierService;
    private List<Barrier> mockBarriers = new ArrayList<>();

    public Runner() {
        mockBarriers.add(new Barrier("Holborn", generateZoneSetWithValues(1), Type.TUBE, Direction.IN));
        mockBarriers.add(new Barrier("Holborn", generateZoneSetWithValues(1), Type.TUBE, Direction.OUT));
        mockBarriers.add(new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.IN));
        mockBarriers.add(new Barrier("Earl's Court", generateZoneSetWithValues(1, 2), Type.TUBE, Direction.OUT));
        mockBarriers.add(new Barrier("Wimbledon", generateZoneSetWithValues(3), Type.TUBE, Direction.IN));
        mockBarriers.add(new Barrier("Wimbledon", generateZoneSetWithValues(3), Type.TUBE, Direction.OUT));
        mockBarriers.add(new Barrier("Hammersmith", generateZoneSetWithValues(2), Type.TUBE, Direction.IN));
        mockBarriers.add(new Barrier("Hammersmith", generateZoneSetWithValues(2), Type.TUBE, Direction.OUT));
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Card card = cardService.createCard();
        Double amount = -1D;
        
        System.out.println("Hi, We've made you an Oyster card, but there's no money on it.\nPlease enter the amount of money you'd like to add to the card then press <enter>:");
        while(amount < 0D) {
            try {
                amount = scanner.nextDouble();
                break;
            } catch (NumberFormatException e) {
                // Suppress...
            }

            System.out.println("Please enter the amount of money you'd like to add to the card then press <enter> (can't be negative or non-numeric):");
        }
        
        cardService.addBalance(card.getNumber(), amount);
        boolean userWantsToTravel = true;

        while(userWantsToTravel) {
            System.out.println("Your card balance is £" + card.getBalance() + ". Would you like to start/finish a " +
                    "(1) bus or (2) tube journey?");
            int journeyType = scanner.nextInt();
            
            if (journeyType == 1) {
                Barrier busBarrier = new Barrier("Generic Bus", generateZoneSetWithValues(1),Barrier.Type.BUS,Barrier.Direction.IN);
                if(attemptToPassBarrier(card, busBarrier))
                    System.out.println("Congrats, you made it. Your card balance is now £" + card.getBalance() + ". You can get off the bus whenever you want, and you don't need to swipe out.");
                else {
                    System.out.println("Whoops, you didn't have enough money. Goodbye.");
                    return;
                }
            } else {
                System.out.println("Select the barrier you'd like to try your card on:");
                for(int i = 0; i < mockBarriers.size(); i++)
                    System.out.println("(" + i + ") " + mockBarriers.get(i));
                int barrierNum = scanner.nextInt();
                try {
                    if(attemptToPassBarrier(card, mockBarriers.get(barrierNum)))
                        System.out.println("Congrats, you made it through the barrier. Your card balance is now £" + card.getBalance() + "");
                    else {
                        System.out.println("Whoops, you didn't have enough money. Goodbye.");
                        return;
                    }
                } catch (IllegalParameterException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
            }
            System.out.println("Would you like to pass another barrier? (1) Yes. (2) No.");
            if(scanner.nextInt() != 1)
                userWantsToTravel = false;
        }
    }

    private boolean attemptToPassBarrier(Card card, Barrier barrier) throws IllegalParameterException, CardNotFoundException {
        try {
            barrierService.passBarrier(barrier, card.getNumber());
        } catch (InsufficientCardBalanceException e) {
            return false;
        }
        return true;
    }

    private Set<Integer> generateZoneSetWithValues(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}