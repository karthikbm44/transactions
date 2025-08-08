package com.onebank.transaction.Service;

import com.onebank.transaction.Dto.TransactionRequest;

public interface TransactionService {
    void saveTransaction(TransactionRequest request);
}
