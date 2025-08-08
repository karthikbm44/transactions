package com.onebank.transaction.Controller;

import com.onebank.transaction.Dto.TransactionRequest;
import com.onebank.transaction.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @PostMapping("/saveTransaction")
    public void saveTransaction(@RequestBody TransactionRequest request){
        service.saveTransaction(request);
    }
}
