package com.vinay.service;

import com.vinay.exception.CardNotFoundException;
import com.vinay.exception.IllegalParameterException;
import com.vinay.model.Card;

public interface CardService {
    Card getCard(Long cardNum) throws CardNotFoundException;
    Card createCard();
    double addBalance(Long cardNum, Double balance) throws IllegalParameterException, CardNotFoundException;
    double deductBalance(Long cardNum, Double balance) throws IllegalParameterException, CardNotFoundException;
}
