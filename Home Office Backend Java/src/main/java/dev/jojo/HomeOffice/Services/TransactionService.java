package dev.jojo.HomeOffice.Services;

import dev.jojo.HomeOffice.Entities.Transaction;
import dev.jojo.HomeOffice.GrpcClient;
import dev.jojo.HomeOffice.Repositories.TransactionRepository;
import org.decampo.xirr.Xirr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
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
        for(String scheme : schemes){
            temp = (ArrayList<Transaction>) transactionRepository.findByFundHouseAndFundDesc(fundhouse,scheme);
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
        }
        return allTransactions;
    }

    public double getTotalXirr(String fundhouse , Iterable<String> schemes , double nav){
        ArrayList<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        Iterable<Transaction> temp;
        temp = getTotalTransactions(fundhouse,schemes);
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(temp, 0.00 ,nav);
        allRelevantTransactions.addAll(res);
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

    public double getTotalAbsoluteReturn(String fundHouse,Iterable<String> schemes , double nav){
        List<Transaction> temp;
        Double TotalBuy = 0.00;
        Double TotalSell = 0.00;
        Double BalanceUnits = 0.00;
        for(String scheme : schemes){
            temp = transactionRepository.findByFundHouseAndFundDesc(fundHouse,scheme);
            temp.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));

            for(Transaction individualTransaction : temp) {
                BalanceUnits = individualTransaction.getBalUnits();
                if(individualTransaction.getNetTransactionAmt()>0){
                    TotalSell+= individualTransaction.getNetTransactionAmt();
                }
                else{
                    TotalBuy+= individualTransaction.getNetTransactionAmt()*-1;
                }
            }
            TotalSell+=BalanceUnits*nav;
        }
        return ((TotalSell-TotalBuy)/TotalBuy);
    }

    public double getBalanceUnitsAbsReturnCore(String fundhouse, Iterable<String> schemes, double nav) {
        try {
            List<Transaction> allRelevantTransactions = new ArrayList<>();
            Double totalSell = 0.00;
            Double totalBuy = 0.00;

            // Iterate through all schemes to collect relevant transactions
            for (String scheme : schemes) {
                List<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundhouse, scheme);

                // Sort transactions by date and id, then reverse to process recent transactions first
                transactions.sort(Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(Transaction::getId));
                Collections.reverse(transactions);

                // Calculate the balance units and absolute return for each scheme
                if (!transactions.isEmpty()) {
                    Double balanceUnits = transactions.get(0).getBalUnits();
                    Double initialBalanceUnits = balanceUnits;

                    for (Transaction transaction : transactions) {
                        System.out.println("Remaining Balance Units: " + balanceUnits);

                        // Skip if it's a sell transaction (NetTransactionAmt > 0)
                        if (transaction.getNetTransactionAmt() > 0) {
                            continue;
                        }

                        // If balance units are less than or equal to 0, break the loop
                        if (balanceUnits < 1) {
                            break;
                        }

                        // If balance units exceed the transaction's units, deduct from balance units
                        if (balanceUnits > transaction.getUnits()) {
                            balanceUnits -= transaction.getUnits();
                            allRelevantTransactions.add(transaction);
                        } else {
                            // If remaining balance units are less, set the appropriate amount and break
                            transaction.setNetTransactionAmt(balanceUnits * transaction.getNav() * -1);
                            transaction.setUnits(balanceUnits);
                            allRelevantTransactions.add(transaction);
                            balanceUnits = 0.0;
                            break;
                        }
                    }

                    // If there are remaining balance units, add a new transaction for the final value at the given NAV
                    if (initialBalanceUnits > 0) {
                        Transaction newTransaction = new Transaction();
                        newTransaction.setNetTransactionAmt(initialBalanceUnits * nav);
                        newTransaction.setUnits(initialBalanceUnits);
                        newTransaction.setNav(nav);
                        newTransaction.setTransactionDesc("Remaining units at NAV");
                        allRelevantTransactions.add(newTransaction);
                    }
                }
            }

            // Calculate the total buy and sell amounts
            if (!allRelevantTransactions.isEmpty()) {
                for (Transaction individualTransaction : allRelevantTransactions) {
                    System.out.println("Net Transaction Amount: " + individualTransaction.getNetTransactionAmt());

                    if (individualTransaction.getNetTransactionAmt() > 0) {
                        totalSell += individualTransaction.getNetTransactionAmt();
                    } else {
                        totalBuy += individualTransaction.getNetTransactionAmt() * -1;
                    }
                }

                // Return the absolute return as ((TotalSell - TotalBuy) / TotalBuy)
                return ((totalSell - totalBuy) / totalBuy);
            } else {
                return 0.0; // No transactions found, return zero absolute return
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
