package com.onebank.transaction.Service.Impl;

import com.onebank.transaction.Dto.TransactionRequest;
import com.onebank.transaction.Entity.Transaction;
import com.onebank.transaction.Repository.TransactionRepository;
import com.onebank.transaction.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository repository;

    @Override
    public void saveTransaction(TransactionRequest request) {

        Transaction transaction = Transaction.builder()
                .accountNumber(request.getAccountNumber())
                .creditAmount(request.getCreditAmount())
                .debitAmount(request.getDebitAmount())
                .currentBalance(request.getCurrentBalance())
                .build();

        repository.save(transaction);
        System.out.println("transaction saved successfully!!!");

        

    }
}
