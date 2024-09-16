package dev.jojo.HomeOffice.Controllers;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.Services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/getAllTransactions")
    public ResponseEntity<Iterable<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/getFundHouseTransactions")
    public ResponseEntity<Iterable<Transaction>> getFundHouseTransaction(@RequestParam String fundhouse) {
        return ResponseEntity.ok(transactionService.getFundHouseTransactions(fundhouse));
    }

    @GetMapping("/getFundHouseSchemeTransactions")
    public ResponseEntity<Iterable<Transaction>> getFundHouseTransaction(@RequestParam String fundhouse, @RequestParam String scheme) {
        return ResponseEntity.ok(transactionService.getFundHouseSchemeTransactions(fundhouse, scheme));
    }

    @PostMapping("/getXirr")
    public ResponseEntity<Double> getXirr(@RequestBody String fundhouse, @RequestBody String scheme, @RequestBody Double units) {
        return ResponseEntity.ok(transactionService.calculateXirr(fundhouse, scheme, units));
    }

}