package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.Transaction;
import java.io.*;
import java.util.*;

public class AccountRepository {
    private static final String FILE_PATH = "bank-data.dat";

    public void saveAll(Map<UUID, Account> accounts, Map<UUID, List<Transaction>> transactions)
            throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(new HashMap<>(accounts));
            oos.writeObject(new HashMap<>(transactions));
        }
    }

    public BankData loadAll() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            @SuppressWarnings("unchecked")
            Map<UUID, Account> accounts = (Map<UUID, Account>) ois.readObject();
            @SuppressWarnings("unchecked")
            Map<UUID, List<Transaction>> transactions =
                (Map<UUID, List<Transaction>>) ois.readObject();
            return new BankData(accounts, transactions);
        } catch (FileNotFoundException e) {
            return new BankData(new HashMap<>(), new HashMap<>());
        }
    }

    public record BankData(Map<UUID, Account> accounts, Map<UUID, List<Transaction>> transactions) {}
}
