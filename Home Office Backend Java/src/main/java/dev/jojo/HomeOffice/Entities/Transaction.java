package dev.jojo.HomeOffice.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"MasterTable\"") // Use double quotes for case-sensitive table names
public class Transaction {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "\"FundHouse\"")  // Case-sensitive column name
    private String fundHouse;

    @Column(name = "\"InvestorName\"")
    private String investorName;

    @Column(name = "\"TransactionDate\"")
    private Date transactionDate;

    @Column(name = "\"TransactionDesc\"")
    private String transactionDesc;

    @Column(name = "\"Amount\"")
    private Double amount;

    @Column(name = "\"NAV\"")
    private Double nav;

    @Column(name = "\"Load\"")
    private Double load;

    @Column(name = "\"Units\"")
    private Double units;

    @Column(name = "\"BalUnits\"")
    private Double balUnits;

    @Column(name = "\"FundDesc\"")
    private String fundDesc;

    @Column(name = "\"NetTransactionAmt\"")
    private Double netTransactionAmt;

    @Column(name = "\"Status\"")
    private Boolean status;
}
