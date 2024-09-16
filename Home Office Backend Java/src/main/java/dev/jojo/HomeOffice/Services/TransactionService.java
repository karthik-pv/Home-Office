package dev.jojo.HomeOffice.Services;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.Repositories.TransactionRepository;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Fetch all transactions
    public Iterable<Transaction> getAllTransactions() {
        Iterable<Transaction> transactions = transactionRepository.findAll();
        List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false).toList());
        transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
        return transactionList;
    }

    // Fetch transactions by fund house
    public Iterable<Transaction> getFundHouseTransactions(String fundHouse) {
        Iterable<Transaction> transactions = transactionRepository.findByFundHouse(fundHouse);
        List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false).toList());
        transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
        return transactionList;
    }

    // Fetch transactions by fund house and scheme
    public Iterable<Transaction> getFundHouseSchemeTransactions(String fundHouse, String scheme) {
        Iterable<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundHouse, scheme);
        List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false).toList());
        transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
        return transactionList;
    }

    // Calculate XIRR based on criteria
    public double calculateXirr(List<Transaction> transactions, double units) {
        List<org.decampo.xirr.Transaction> xirrTransactions = mapToXirrTransactions(transactions, units);
        return new Xirr(xirrTransactions).xirr();
    }

    // Map transactions to XIRR transactions
    private List<org.decampo.xirr.Transaction> mapToXirrTransactions(Iterable<Transaction> transactions, double units) {
        List<org.decampo.xirr.Transaction> xirrTransactions = new ArrayList<>();
        double balUnits = 0.0;
        double nav = 0.0;
        for (Transaction transaction : transactions) {
            double amount = transaction.getNetTransactionAmt();
            LocalDate date = transaction.getTransactionDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            balUnits = transaction.getBalUnits();
            nav = transaction.getNav();
            xirrTransactions.add(new org.decampo.xirr.Transaction(amount, date));
        }
        if (units == 0.00) {
            xirrTransactions.add(new org.decampo.xirr.Transaction(balUnits * nav, LocalDate.now()));
        } else {
            xirrTransactions.add(new org.decampo.xirr.Transaction(units * nav, LocalDate.now()));
        }
        return xirrTransactions;
    }

    public double calculateXirr(String fundHouse, String fundDesc, Double units) {
        Iterable<Transaction> transactions;

        if (fundHouse != null && fundDesc != null) {
            transactions = getFundHouseSchemeTransactions(fundHouse, fundDesc);
        } else if (fundHouse != null) {
            transactions = getFundHouseTransactions(fundHouse);
        } else {
            transactions = getAllTransactions();
        }

        List<Transaction> transactionList = new ArrayList<>();
        transactions.forEach(transactionList::add);

        if (units != null) {
            return calculateXirr(transactionList, units);
        } else {
            return calculateXirr(transactionList, 0.00);
        }
    }

    public Iterable<Transaction> getBalanceUnitsTransactions(String fundhouse , String scheme){
        Iterable<Transaction> transactions;
        if(fundhouse!=null && scheme!=null){
            transactions==
        }
        List<Transaction> transactionList = new ArrayList<>();
        transactions.forEach(transactionList::add);
        transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
        Collections.reverse(transactionList);
        Double balanceUnits = transactionList.getFirst().getBalUnits();
        List<Transaction> returnable = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.getNetTransactionAmt() > 0) {
                continue;
            }
            if (balanceUnits < 1) {
                break;
            }
            if (balanceUnits > transaction.getUnits()) {
                balanceUnits = balanceUnits - transaction.getUnits() + 0.001;
                returnable.add(transaction);
            } else {
                transaction.setAmount(balanceUnits * transaction.getNav());
                returnable.add(transaction);
                break;
            }
        }
        Collections.reverse(returnable);
        return returnable;
    }
}
