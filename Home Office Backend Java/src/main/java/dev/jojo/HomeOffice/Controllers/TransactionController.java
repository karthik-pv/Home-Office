package dev.jojo.HomeOffice.Controllers;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.Services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/getAllTransactions")
    public ResponseEntity<Iterable<Transaction>> getAllTransactions(){
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/getTotalXirr")
    public ResponseEntity<Double> getTotalXirr(){
        return ResponseEntity.ok(transactionService.calculateCompleteXirr());
    }

    @GetMapping("/getBalanceUnitsXirr")
    public ResponseEntity<Double> getBalanceUnitsXirr(){
        return ResponseEntity.ok(transactionService.calculateBalanceUnitsXirr());
    }

    @GetMapping("/getCustomUnitsXirr")
    public ResponseEntity<Double> getCustomUnitsXirr(@RequestParam double units) {
        return ResponseEntity.ok(transactionService.calculateCustomUnitsXirr(units));
    }

}
