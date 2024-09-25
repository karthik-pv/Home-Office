package dev.jojo.HomeOffice.Services;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.GrpcClient;
import dev.jojo.HomeOffice.Repositories.TransactionRepository;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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


    public Iterable<Transaction> getCustomNumberUnitsTransaction(Iterable<Transaction> transactions, Double units) {
        try {
            List<Transaction> transactionList = new ArrayList<>();
            List<Transaction> postZeroTransactions = new ArrayList<>();
            List<Transaction> buyTransactions = new ArrayList<>();
            List<Transaction> returnable = new ArrayList<>();
            Double balanceUnits = units;
            Double soldUnits = 0.0;

            transactions.forEach(transactionList::add);
            transactionList.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));

            Collections.reverse(transactionList);

            for (Transaction transaction : transactionList) {
                if (transaction.getBalUnits() == 0.0) {
                    break;
                }
                postZeroTransactions.add(transaction);
            }
            Collections.reverse(postZeroTransactions);

            for (Transaction transaction : postZeroTransactions) {
                if (transaction.getNetTransactionAmt() > 0) {
                    soldUnits += transaction.getUnits();
                } else {
                    buyTransactions.add(transaction);
                }
            }

            Iterator<Transaction> iterator = buyTransactions.iterator();
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                if (transaction.getUnits() < soldUnits) {
                    soldUnits -= transaction.getUnits();
                    iterator.remove();
                } else {
                    Double remainingUnits = transaction.getUnits() - soldUnits;
                    transaction.setUnits(remainingUnits);
                    transaction.setAmount(remainingUnits * transaction.getNav() * -1);
                    soldUnits = 0.0;
                    break;
                }
            }

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

            for (Transaction t : returnable) {
                System.out.println("Units: " + t.getUnits() + ", Amount: " + t.getAmount() + ", Date: " + t.getTransactionDate());
            }

            return returnable;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }



    public List<org.decampo.xirr.Transaction> mapToXirrTransactions(Iterable<Transaction> transactions ) {
        List<org.decampo.xirr.Transaction> xirrTransactions = new ArrayList<>();
        LocalDate date;
        Double amount;
        for (Transaction transaction : transactions) {
            amount = transaction.getNetTransactionAmt();
            date = transaction.getTransactionDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            xirrTransactions.add(new org.decampo.xirr.Transaction(amount, date));
        }
        return xirrTransactions;
    }

    public Iterable<Transaction> getTotalTransactions(String fundhouse , Iterable<String> schemes){
        ArrayList<Transaction> allTransactions = new ArrayList<>();
        ArrayList<Transaction> temp = new ArrayList<>();
        Double netTransactionAmt=0.0;
        Double nav = 0.0;
        for(String scheme : schemes) {
            temp = (ArrayList<Transaction>) transactionRepository.findByFundHouseAndFundDesc(fundhouse, scheme);
            temp.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));
            nav = GrpcClient.makeRpcCall(scheme);
            System.out.println(nav);
            netTransactionAmt = temp.getLast().getBalUnits() * nav;
            Transaction toInsert = new Transaction();
            toInsert.setNetTransactionAmt(netTransactionAmt);
            toInsert.setNav(nav);
            LocalDateTime now = LocalDateTime.now();
            toInsert.setTransactionDate(Timestamp.valueOf(now));
            temp.addLast(toInsert);
            allTransactions.addAll(temp);
        }
        return allTransactions;
    }

    public Iterable<Transaction> getBalanceUnitsTransactions(String fundHouse , Iterable<String> schemes){
        try{
            List<Transaction> returnable = new ArrayList<>();
            Double nav=0.0;
            for(String scheme : schemes) {
                List<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundHouse,scheme);
                transactions.sort(Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(Transaction::getId));
                Collections.reverse(transactions);
                Double balanceUnits = transactions.getFirst().getBalUnits();
                nav = GrpcClient.makeRpcCall(scheme);
                List<Transaction> temp = new ArrayList<>();
                Transaction toInsert = new Transaction();
                LocalDateTime now = LocalDateTime.now();
                toInsert.setTransactionDate(Timestamp.valueOf(now));
                toInsert.setUnits(balanceUnits);
                toInsert.setNav(nav);
                toInsert.setNetTransactionAmt(balanceUnits*nav);
                temp.add(toInsert);
                for (Transaction transaction : transactions) {
                    System.out.println(balanceUnits);
                    if (transaction.getNetTransactionAmt() > 0) {
                        continue;
                    }
                    if (balanceUnits < 1) {
                        break;
                    }
                    if (balanceUnits > transaction.getUnits()) {
                        balanceUnits = balanceUnits - transaction.getUnits() - 1;
                        temp.add(transaction);
                    } else {
                        transaction.setNetTransactionAmt(balanceUnits * transaction.getNav() * -1);
                        temp.add(transaction);
                        break;
                    }
                }
                returnable.addAll(temp);
            }
            return returnable;
        } catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getTotalXirr(String fundhouse , Iterable<String> schemes){
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        Iterable<Transaction> temp;
        temp = getTotalTransactions(fundhouse,schemes);
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(temp);
        allRelevantTransactions.addAll(res);
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getBalanceUnitsXirr(String fundhouse , Iterable<String> schemes,double nav){
        List<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        allRelevantTransactions = mapToXirrTransactions(getBalanceUnitsTransactions(fundhouse,schemes));
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getCustomUnitsXirr(String fundhouse , String scheme , Double units , Double nav){
        Iterable<Transaction> temp;
        temp = transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(getCustomNumberUnitsTransaction(temp, units));
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>(res);
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getTotalAbsoluteReturn(String fundHouse,Iterable<String> schemes , double nav){
        List<Transaction> allTransactions;
        Double TotalBuy = 0.00;
        Double TotalSell = 0.00;

        allTransactions = (List<Transaction>) getTotalTransactions(fundHouse , schemes);

        for(Transaction individualTransaction : allTransactions) {
            if(individualTransaction.getNetTransactionAmt()>0){
                TotalSell+= individualTransaction.getNetTransactionAmt();
            }
            else{
                TotalBuy+= individualTransaction.getNetTransactionAmt()*-1;
            }
        }
        return ((TotalSell-TotalBuy)/TotalBuy);
    }

    public double getBalanceUnitsAbsReturnCore(String fundhouse, Iterable<String> schemes, double nav) {
        try {
            List<Transaction> allRelevantTransactions = (List<Transaction>) getBalanceUnitsTransactions(fundhouse,schemes);
            Double totalSell = 0.00;
            Double totalBuy = 0.00;
            if (!allRelevantTransactions.isEmpty()) {
                for (Transaction individualTransaction : allRelevantTransactions) {
                    System.out.println("Net Transaction Amount: " + individualTransaction.getNetTransactionAmt());

                    if (individualTransaction.getNetTransactionAmt() > 0) {
                        totalSell += individualTransaction.getNetTransactionAmt();
                    } else {
                        totalBuy += individualTransaction.getNetTransactionAmt() * -1;
                    }
                }
                return ((totalSell - totalBuy) / totalBuy);
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getCustomUnitsAbsoluteReturn(String fundhouse, String scheme, Double units, Double nav) {
        try {
            // Fetch the relevant transactions based on the fundhouse and scheme
            Iterable<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundhouse, scheme);

            // Get the processed transactions for the custom number of units
            Iterable<Transaction> customUnitsTransactions = getCustomNumberUnitsTransaction(transactions, units);

            // Initialize total sell and total buy amounts
            Double totalSell = 0.0;
            Double totalBuy = 0.0;

            // Iterate over the transactions and calculate total buy and sell amounts
            for (Transaction transaction : customUnitsTransactions) {
                if (transaction.getNetTransactionAmt() > 0) {
                    // Add to totalSell for positive (sell) transactions
                    totalSell += transaction.getNetTransactionAmt();
                } else {
                    // Add to totalBuy for negative (buy) transactions
                    totalBuy += transaction.getNetTransactionAmt() * -1;
                }
            }

            // Add a final transaction representing the remaining units at the given NAV
            double remainingUnitsValue = units * nav;
            totalSell += remainingUnitsValue;

            // Calculate and return the absolute return as ((TotalSell - TotalBuy) / TotalBuy)
            if (totalBuy > 0) {
                return ((totalSell - totalBuy) / totalBuy);
            } else {
                // In case there's no buy transaction, return 0 to avoid division by zero
                return 0.0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
