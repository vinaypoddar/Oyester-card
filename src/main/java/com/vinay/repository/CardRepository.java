package com.vinay.repository;

import com.vinay.exception.CardNotFoundException;
import com.vinay.model.Card;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CardRepository {
    private Map<Long, Card> cards = new HashMap<>();

    public Card getCard(Long cardNum) throws CardNotFoundException {
        Card card = cards.get(cardNum);
        if(card == null)
            throw new CardNotFoundException("Card with number {" + cardNum + "} does not exist.");
        return card;
    }

    public Card recordCard(Card card) {
    	cards.put(card.getNumber(), card);
        return card;
    }
}
