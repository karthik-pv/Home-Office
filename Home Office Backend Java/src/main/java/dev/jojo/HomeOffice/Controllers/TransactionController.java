package dev.jojo.HomeOffice.Controllers;

import dev.jojo.HomeOffice.Entities.FundHouseXirrRequest;
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

    @PostMapping("/getTotalFundHouseXirr")
    public ResponseEntity<Double> getTotalFundHouseXirr(@RequestBody FundHouseXirrRequest data) {
        String fundhouse = data.getFundhouse();
        Iterable<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getTotalXirr(fundhouse, schemes));
    }

    @PostMapping("/getBalanceUnitsXirr")
    public ResponseEntity<Double> getBalanceUnitsXirr(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        Iterable<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getBalanceUnitsXirr(fundhouse,schemes));
    }

    @PostMapping("/getCustomUnitsXirr")
    public ResponseEntity<Double> getCustomUnitsXirr(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        List<String> schemes = data.getSchemes();
        Double units = data.getUnits();
        return ResponseEntity.ok(transactionService.getCustomUnitsXirr(fundhouse, schemes.getFirst(),units));
    }

    @PostMapping("/getSoldUnitsXirr")
    public ResponseEntity<Double> getSoldUnits(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        List<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getSoldUnitsXirr(fundhouse,schemes));
    }

    @PostMapping("/getTotalAbsReturn")
    public ResponseEntity<Double> getTotalAbsReturn(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        List<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getTotalAbsoluteReturn(fundhouse, schemes));
    }

    @PostMapping("/getBalanceUnitsAbsReturn")
    public ResponseEntity<Double> getBalanceUnitsAbsReturn(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        Iterable<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getBalanceUnitsAbsReturn(fundhouse,schemes));
    }

    @PostMapping("/getCustomUnitsAbsReturn")
    public ResponseEntity<Double> getCustomUnitsAbsReturn(@RequestBody FundHouseXirrRequest data){
        String fundhouse = data.getFundhouse();
        List<String> schemes = data.getSchemes();
        Double units = data.getUnits();
        return ResponseEntity.ok(transactionService.getCustomUnitsAbsoluteReturn(fundhouse,schemes.getFirst(),units));
    }

    @PostMapping("/getSoldUnitsAbsReturn")
    public ResponseEntity<Double> getSoldUnitsAbsReturn(@RequestBody FundHouseXirrRequest data){
        String fundHouse = data.getFundhouse();
        List<String> schemes = data.getSchemes();
        return ResponseEntity.ok(transactionService.getSoldUnitsAbsoluteReturn(fundHouse,schemes));
    }

}