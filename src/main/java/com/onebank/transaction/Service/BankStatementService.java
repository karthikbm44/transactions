package com.onebank.transaction.Service;

import com.onebank.transaction.Entity.Transaction;

import java.util.List;

public interface BankStatementService {
    List<Transaction> generateBankStatement(String accountNumber,String fromDate,String toDate);
}
