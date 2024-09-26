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

    public Iterable<Transaction> getCustomNumberUnitsTransaction(String fundHouse , String scheme , Double units) {
        try {
            List<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundHouse,scheme);
            List<Transaction> postZeroTransactions = new ArrayList<>();
            List<Transaction> buyTransactions = new ArrayList<>();
            List<Transaction> returnable = new ArrayList<>();
            Double balanceUnits = units;
            Double soldUnits = 0.0;
            Double nav = 0.0;

            transactions.sort(Comparator
                    .comparing(Transaction::getTransactionDate)
                    .thenComparing(Transaction::getId));

            Collections.reverse(transactions);

            for (Transaction transaction : transactions) {
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

            for(Transaction transaction : buyTransactions) {
                if(transaction.getUnits()<=soldUnits){
                    soldUnits-=transaction.getUnits();
                } else {
                    Double remainingUnits = transaction.getUnits() - soldUnits;
                    transaction.setUnits(remainingUnits);
                    transaction.setAmount(remainingUnits * transaction.getNav() * -1);
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

            nav = GrpcClient.makeRpcCall(scheme);
            Transaction toInsert = new Transaction();
            toInsert.setNav(nav);
            LocalDateTime now = LocalDateTime.now();
            toInsert.setTransactionDate(Timestamp.valueOf(now));
            toInsert.setNetTransactionAmt(nav*units);
            returnable.add(toInsert);
            return returnable;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Iterable<Transaction> getAllSoldTransactions(String fundHouse, Iterable<String> schemes) {
        try {
            ArrayList<Transaction> allRelevantTransactions = new ArrayList<>();
            double soldUnits = 0.0;

            for (String scheme : schemes) {
                List<Transaction> transactions = transactionRepository.findByFundHouseAndFundDesc(fundHouse, scheme);
                if (transactions == null) {
                    continue;
                }

                transactions.sort(Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(Transaction::getId));

                for (Transaction transaction : transactions) {
                    if (transaction.getNetTransactionAmt() > 0) {
                        allRelevantTransactions.add(transaction);
                        soldUnits += transaction.getUnits();
                    }
                }

                for (Transaction transaction : transactions) {
                    if (transaction.getNetTransactionAmt() < 0) {
                        if (transaction.getUnits() <= soldUnits) {
                            allRelevantTransactions.add(transaction);
                            soldUnits -= transaction.getUnits();
                        } else {
                            Transaction partialTransaction = new Transaction();
                            partialTransaction.setTransactionDate(transaction.getTransactionDate());
                            partialTransaction.setUnits(soldUnits);
                            partialTransaction.setNetTransactionAmt(soldUnits * transaction.getNav()* -1);
                            allRelevantTransactions.add(partialTransaction);
                            soldUnits = 0;
                            break;
                        }
                    }
                }
            }

            return allRelevantTransactions.isEmpty() ? Collections.emptyList() : allRelevantTransactions;
        } catch (Exception e) {
            throw new RuntimeException("Error processing transactions: " + e.getMessage(), e);
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

    public double getBalanceUnitsXirr(String fundhouse , Iterable<String> schemes){
        List<org.decampo.xirr.Transaction> allRelevantTransactions = new ArrayList<>();
        allRelevantTransactions = mapToXirrTransactions(getBalanceUnitsTransactions(fundhouse,schemes));
        if (!allRelevantTransactions.isEmpty()) {
            return new Xirr(allRelevantTransactions).xirr();
        } else {
            return 0.0;
        }
    }

    public double getCustomUnitsXirr(String fundhouse , String scheme , Double units){
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(getCustomNumberUnitsTransaction(fundhouse,scheme,units));
        if (!res.isEmpty()) {
            return new Xirr(res).xirr();
        } else {
            return 0.0;
        }
    }

    public Double getSoldUnitsXirr(String fundhouse , Iterable<String> schemes){
        List<org.decampo.xirr.Transaction> res = mapToXirrTransactions(getAllSoldTransactions(fundhouse,schemes));
        if(!res.isEmpty()){
            return new Xirr(res).xirr();
        }
        else{
            return 0.0;
        }
    }

    public Double getAbsoluteReturn(Iterable<Transaction> allTransactions){
        try {
            Double TotalBuy = 0.00;
            Double TotalSell = 0.00;
            for (Transaction individualTransaction : allTransactions) {
                if (individualTransaction.getNetTransactionAmt() > 0) {
                    TotalSell += individualTransaction.getNetTransactionAmt();
                } else {
                    TotalBuy += individualTransaction.getNetTransactionAmt() * -1;
                }
            }
            return ((TotalSell - TotalBuy) / TotalBuy);
        }
        catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getTotalAbsoluteReturn(String fundHouse,Iterable<String> schemes ){
        List<Transaction> allTransactions;

        allTransactions = (List<Transaction>) getTotalTransactions(fundHouse , schemes);

        return getAbsoluteReturn(allTransactions);
    }

    public double getBalanceUnitsAbsReturn(String fundhouse, Iterable<String> schemes) {
        try {
            List<Transaction> allRelevantTransactions = (List<Transaction>) getBalanceUnitsTransactions(fundhouse,schemes);
            return getAbsoluteReturn(allRelevantTransactions);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getCustomUnitsAbsoluteReturn(String fundhouse, String scheme, Double units) {
        try {
            Iterable<Transaction> customUnitsTransactions = getCustomNumberUnitsTransaction(fundhouse,scheme,units);
            return getAbsoluteReturn(customUnitsTransactions);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Double getSoldUnitsAbsoluteReturn(String fundhouse , Iterable<String> schemes){
        try{
            ArrayList<Transaction> allRelevantTransactions = (ArrayList<Transaction>) getAllSoldTransactions(fundhouse,schemes);
            return getAbsoluteReturn(allRelevantTransactions);
        }
        catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
