package dev.jojo.HomeOffice.Services;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.Repositories.TransactionRepository;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Iterable<Transaction> getAllTransactions(){
        try {
            Iterable<Transaction> transactions = transactionRepository.findAll();
            List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false)
                    .toList());
            transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
            return transactionList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Iterable<Transaction> getBalanceUnitsTransactions(){
        try{
            Iterable<Transaction> transactions = transactionRepository.findAll();
            List<Transaction> transactionList = new ArrayList<>();
            transactions.forEach(transactionList::add);
            transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
            Collections.reverse(transactionList);
            Double balanceUnits = transactionList.getFirst().getBalUnits();
            System.out.println(balanceUnits + "jere");
            List<Transaction> returnable = new ArrayList<>();
            for(Transaction transaction : transactionList) {
                System.out.println(balanceUnits + "jere");
                if(transaction.getNetTransactionAmt()>0){
                    continue;
                }
                if(balanceUnits<1){
                    break;
                }
                if (balanceUnits>transaction.getUnits()) {
                    balanceUnits=balanceUnits-transaction.getUnits()+0.001;
                    returnable.add(transaction);
                }
                else{
                    transaction.setAmount(balanceUnits*transaction.getNav());
                    returnable.add(transaction);
                    break;
                }
            }
            Collections.reverse(returnable);
            return returnable;
        } catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public Iterable<Transaction> getCustomNumberUnitsTransaction(Double units){
        try{
            Iterable<Transaction> transactions = transactionRepository.findAll();
            List<Transaction> transactionList = new ArrayList<>();
            transactions.forEach(transactionList::add);
            transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
            Collections.reverse(transactionList);
            Double balanceUnits = units*transactionList.getFirst().getNav();
            System.out.println(balanceUnits + "jere");
            List<Transaction> returnable = new ArrayList<>();
            for(Transaction transaction : transactionList) {
                System.out.println(balanceUnits + "jere");
                if(transaction.getNetTransactionAmt()>0){
                    continue;
                }
                if(balanceUnits<1){
                    break;
                }
                if (balanceUnits>transaction.getUnits()) {
                    balanceUnits=balanceUnits-transaction.getUnits()+0.001;
                    returnable.add(transaction);
                }
                else{
                    transaction.setAmount(balanceUnits*transaction.getNav());
                    returnable.add(transaction);
                    break;
                }
            }
            Collections.reverse(returnable);
            return returnable;
        } catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<org.decampo.xirr.Transaction> mapToXirrTransactions(Iterable<Transaction> transactions , double Units) {
        List<org.decampo.xirr.Transaction> xirrTransactions = new ArrayList<>();
        double balUnits = 0.0;
        double nav = 0.0;
        for (Transaction transaction : transactions) {
            double amount = transaction.getNetTransactionAmt();
            LocalDate date = transaction.getTransactionDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            System.out.println(amount);
            System.out.println(date);
            balUnits = transaction.getBalUnits();
            nav = transaction.getNav();
            xirrTransactions.add(new org.decampo.xirr.Transaction(amount, date));
        }
        if(Units==0.00) {
            System.out.println(balUnits*nav);
            xirrTransactions.add(new org.decampo.xirr.Transaction(balUnits * nav, LocalDate.now()));
        }
        else{
            xirrTransactions.add(new org.decampo.xirr.Transaction(Units * nav, LocalDate.now()));
        }
        return xirrTransactions;
    }

    public double calculateCompleteXirr() {
        Iterable<Transaction> transactions = getAllTransactions();
        List<org.decampo.xirr.Transaction> xirrTransactions = mapToXirrTransactions(transactions,0.00);
        return new Xirr(xirrTransactions).xirr();
    }

    public double calculateBalanceUnitsXirr(){
        Iterable<Transaction> transactions = getBalanceUnitsTransactions();
        List<org.decampo.xirr.Transaction> xirrTransactions = mapToXirrTransactions(transactions,0.00);
        return new Xirr(xirrTransactions).xirr();
    }

    public double calculateCustomUnitsXirr(double units){
        Iterable<Transaction> transactions = getCustomNumberUnitsTransaction(units);
        List<org.decampo.xirr.Transaction> xirrTransactions = mapToXirrTransactions(transactions,units);
        return new Xirr(xirrTransactions).xirr();
    }
}
