# Lesson 58: Encapsulation (Getters & Setters)

## Key Concepts
- **Encapsulation** bundles data and methods that operate on that data, hiding internal state
- Mark fields as `private` to prevent direct access from outside the class
- Provide `public` **getter** methods to read field values
- Provide `public` **setter** methods to modify field values (with validation if needed)
- Getters follow the naming pattern `getFieldName()`; setters follow `setFieldName()`
- Encapsulation allows you to add logic (validation, logging, computed values) when fields are accessed

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("123456789", "Alice", 500.0);

        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Account Holder: " + getAccountHolderName(account));
        System.out.println("Balance: $" + account.getBalance());

        account.deposit(250.0);
        System.out.println("After deposit: $" + account.getBalance());

        account.withdraw(100.0);
        System.out.println("After withdrawal: $" + account.getBalance());

        account.withdraw(800.0);
        System.out.println("After failed withdrawal: $" + account.getBalance());

        account.setAccountHolderName("Alice Smith");
        System.out.println("Updated name: " + getAccountHolderName(account));
    }

    static String getAccountHolderName(BankAccount acc) {
        return acc.getAccountHolderName();
    }
}

class BankAccount {
    private String accountNumber;
    private String accountHolderName;
    private double balance;

    BankAccount(String accountNumber, String accountHolderName, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        System.out.println("Insufficient funds!");
        return false;
    }
}
```

### Explanation
All fields in `BankAccount` are `private` — they cannot be accessed directly from `Main`. The class provides getters for reading values and methods like `deposit()` and `withdraw()` that include validation logic (no negative deposits, insufficient funds check). `setAccountHolderName()` allows updating the holder's name.

### Expected Output
```
Account Number: 123456789
Account Holder: Alice
Balance: $500.0
After deposit: $750.0
After withdrawal: $650.0
Insufficient funds!
After failed withdrawal: $650.0
Updated name: Alice Smith
```
