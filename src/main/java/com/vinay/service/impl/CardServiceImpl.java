package com.vinay.service.impl;

import com.vinay.exception.CardNotFoundException;
import com.vinay.exception.IllegalParameterException;
import com.vinay.model.Card;
import com.vinay.repository.CardRepository;
import com.vinay.service.CardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CardServiceImpl implements CardService {
    
	private final Random random = new Random(System.currentTimeMillis());
    private CardRepository cardRepository;
    
    @Autowired
    public CardServiceImpl(CardRepository cardRepository) {
		this.cardRepository = cardRepository;
	}
    
    @Override
    public Card createCard() {
        return cardRepository.recordCard(new Card(Math.abs(random.nextLong())));
    }

    @Override
    public Card getCard(Long cardNum) throws CardNotFoundException {
        return cardRepository.getCard(cardNum);
    }

	@Override
	public double addBalance(Long cardNum, Double amount) throws IllegalParameterException, CardNotFoundException {
		if(amount < 0.0D)
            throw new IllegalParameterException("Cannot add a negative amount of money to a card");
		Card card = cardRepository.getCard(cardNum);
        card.setBalance(amount + card.getBalance());
        return card.getBalance();
	}

	@Override
	public double deductBalance(Long cardNum, Double amount) throws IllegalParameterException, CardNotFoundException {
		if(amount < 0.0D) 
            throw new IllegalParameterException("Cannot remove a negative amount of money from a card");
		Card card = cardRepository.getCard(cardNum);
        card.setBalance(card.getBalance() - amount);
        return card.getBalance();
	}    
    
}
