package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody TransactionDto obj) {
        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("Invalid ID"));
        if(obj.getAmount() <= 0){
            return ResponseEntity.badRequest()
                    .body("Amount must be greater than zero");
        }

        double newBalance = user.getBalance() + obj.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);
        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs" + obj.getAmount() + "Deposit Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return ResponseEntity.ok("Deposit Successful!");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody TransactionDto obj) {
        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("Invalid ID"));
        double newBalance = user.getBalance() - obj.getAmount();
        if (newBalance < 0) {
            return ResponseEntity.ok("Insufficient Balance");
        }
        if(obj.getAmount() <= 0){
            return ResponseEntity.badRequest()
                    .body("Amount must be greater than zero");
        }

        user.setBalance(newBalance);
        userRepo.save(user);
        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs" + obj.getAmount() + "Withdrawal Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return ResponseEntity.ok("Withdrawal Successful!");
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj) {
        User sender = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("Not found"));
        User rec = userRepo.findByUsername(obj.getUsername());
        if (rec == null) {
            return "Receiver not found";
        }
        if (sender.getId() == rec.getId()) {
            return "Self Transaction not allowed";
        }
        if (obj.getAmount() < 1) {
            return "Invalid amount";
        }

        double sbalance = sender.getBalance() - obj.getAmount();
        if (sbalance < 0) {
            return "Unsufficient balance";
        }
        double rbalance = rec.getBalance() + obj.getAmount();

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);
        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs " + obj.getAmount() + "Send to user " + obj.getUsername());
        t1.setUserId(sender.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs " + obj.getAmount() + "received from " + sender.getUsername());
        t2.setUserId(rec.getId());

        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer done successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id){
        return transactionRepo.findAllByUserId(id);
    }
}