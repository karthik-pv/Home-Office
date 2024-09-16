package dev.jojo.HomeOffice.Repositories;

import dev.jojo.HomeOffice.Entities.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

    List<Transaction> findByFundHouseAndFundDesc(String fundHouse, String fundDesc);

    // Method to fetch transactions by fundHouse only
    List<Transaction> findByFundHouse(String fundHouse);

}
