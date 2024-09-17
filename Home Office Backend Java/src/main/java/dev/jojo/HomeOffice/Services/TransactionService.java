package dev.jojo.HomeOffice.Services;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.Repositories.TransactionRepository;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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

    public Iterable<Transaction> getFundHouseTransactions(String fundhouse){
        try{
            Iterable<Transaction> transactions = transactionRepository.findByFundHouse(fundhouse);
            List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false)
                    .toList());
            transactionList.sort(Comparator.comparing(Transaction::getTransactionDate));
            return transactionList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Iterable<Transaction> getFundHouseSchemeTransactions(String fundhouse , String scheme){
        try{
            Iterable<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
            List<Transaction> transactionList = new ArrayList<>(StreamSupport.stream(transactions.spliterator(), false)
                    .toList());
            transactionList.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));
            ;
            return transactionList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Iterable<Transaction> getBalanceUnitsTransactions(Iterable<Transaction> transactions){
        try{
            List<Transaction> transactionList = new ArrayList<>();
            transactions.forEach(transactionList::add);
            transactionList.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));
            Collections.reverse(transactionList);
            Double balanceUnits = transactionList.getFirst().getBalUnits();
            List<Transaction> returnable = new ArrayList<>();
            for(Transaction transaction : transactionList){
                System.out.println(balanceUnits);
                if(transaction.getNetTransactionAmt()>0){
                    continue;
                }
                if(balanceUnits<1){
                    break;
                }
                if (balanceUnits>transaction.getUnits()) {
                    balanceUnits=balanceUnits-transaction.getUnits();
                    returnable.add(transaction);
                }
                else{
                    transaction.setNetTransactionAmt(balanceUnits*transaction.getNav()*-1);
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

    public Iterable<Transaction> getCustomNumberUnitsTransaction(Iterable<Transaction> transactions, Double units) {
        try {
            List<Transaction> transactionList = new ArrayList<>();
            List<Transaction> postZeroTransactions = new ArrayList<>();
            List<Transaction> buyTransactions = new ArrayList<>();
            List<Transaction> returnable = new ArrayList<>();
            Double balanceUnits = units;
            Double soldUnits = 0.0;

            // Collect transactions into a list and sort by date
            transactions.forEach(transactionList::add);
            transactionList.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));

            Collections.reverse(transactionList);

            // Traverse in reverse and collect transactions until we find one with zero balance units
            for (Transaction transaction : transactionList) {
                if (transaction.getBalUnits() == 0.0) {
                    break;
                }
                postZeroTransactions.add(transaction);
            }
            Collections.reverse(postZeroTransactions);

            // Separate buy and sell transactions
            for (Transaction transaction : postZeroTransactions) {
                if (transaction.getNetTransactionAmt() > 0) {
                    soldUnits += transaction.getUnits();
                } else {
                    buyTransactions.add(transaction);
                }
            }

            // Handle sold units
            Iterator<Transaction> iterator = buyTransactions.iterator();
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                if (transaction.getUnits() < soldUnits) {
                    soldUnits -= transaction.getUnits();
                    iterator.remove(); // Remove the transaction from the list
                } else {
                    Double remainingUnits = transaction.getUnits() - soldUnits;
                    transaction.setUnits(remainingUnits);
                    transaction.setAmount(remainingUnits * transaction.getNav() * -1);
                    soldUnits = 0.0; // All sold units have been processed
                    break;
                }
            }

            // Add the remaining buy transactions to returnable
            for (Transaction buyTransaction : buyTransactions) {
                if (buyTransaction.getUnits() <= balanceUnits) {
                    balanceUnits -= buyTransaction.getUnits();
                    buyTransaction.setAmount(buyTransaction.getUnits() * buyTransaction.getNav() * -1);
                    returnable.add(buyTransaction);
                } else {
                    buyTransaction.setUnits(balanceUnits);
                    buyTransaction.setNetTransactionAmt(balanceUnits * buyTransaction.getNav() * -1);
                    returnable.add(buyTransaction);
                    break;
                }
            }

            // Print the result for debugging
            for (Transaction t : returnable) {
                System.out.println("Units: " + t.getUnits() + ", Amount: " + t.getAmount() + ", Date: " + t.getTransactionDate());
            }

            return returnable;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    public List<org.decampo.xirr.Transaction> mapToXirrTransactions(Iterable<Transaction> transactions , double Units , double nav) {
        List<org.decampo.xirr.Transaction> xirrTransactions = new ArrayList<>();
        double balUnits = 0.0;
        for (Transaction transaction : transactions) {
            double amount = transaction.getNetTransactionAmt();
            LocalDate date = transaction.getTransactionDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            System.out.println(amount);
            System.out.println(date);
            balUnits = transaction.getBalUnits();
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


    public double getTotalXirr(String fundhouse , Iterable<String> schemes , double nav){
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        Iterable<Transaction> temp;
        for(String scheme : schemes){
            temp = transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
            List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(temp, 0.00 ,nav);
            allRelevantTransactions.addAll(res);
        }
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getBalanceUnitsXirr(String fundhouse , Iterable<String> schemes,double nav){
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        Iterable<Transaction> temp;
        for(String scheme : schemes){
            temp = transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
            List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(getBalanceUnitsTransactions(temp), 0.00,nav);
            allRelevantTransactions.addAll(res);
        }
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getCustomUnitsXirr(String fundhouse , String scheme , Double units , Double nav){
        Iterable<Transaction> temp;
        temp = transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(getCustomNumberUnitsTransaction(temp, units),units , nav );
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>(res);
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }
}
