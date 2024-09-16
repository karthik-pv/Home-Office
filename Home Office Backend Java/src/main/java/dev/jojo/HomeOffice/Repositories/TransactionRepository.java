package dev.jojo.HomeOffice.Repositories;

import dev.jojo.HomeOffice.Entities.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction,Integer> {
}
