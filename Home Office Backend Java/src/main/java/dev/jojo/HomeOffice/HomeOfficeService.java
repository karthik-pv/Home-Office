package dev.jojo.HomeOffice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class HomeOfficeService {
    public double calculateAbsoluteReturn(double marketValue , double totalCost){
        return ((marketValue-totalCost)/totalCost)*100;
    }

    public double calculateXirr(String jsonData){
        List<Transaction> transactions = parseJsonToTransactions(jsonData);
        return new Xirr(transactions).xirr();
    }

    private static List<Transaction> parseJsonToTransactions(String jsonInput) {
        List<Transaction> transactions = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonInput);
            rootNode.forEach(node -> {
                double amount = node.get("amount").asDouble();
                LocalDate date = LocalDate.parse(node.get("date").asText());
                transactions.add(new Transaction(amount, date));
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return transactions;
    }
}
