package employees.domain;

/**
 * BankAccount class represents an employee's bank account information in the system.
 * It contains the bank number, branch number, and account number.
 */
public class BankAccount {
    private String bankNumber;
    private String branchNumber;
    private String accountNumber;

    public BankAccount(String bankNumber, String branchNumber, String accountNumber) {
        validatePositiveNumber(bankNumber, "Bank number");
        validatePositiveNumber(branchNumber, "Branch number");
        validatePositiveNumber(accountNumber, "Account number");
        this.bankNumber = bankNumber;
        this.branchNumber = branchNumber;
        this.accountNumber = accountNumber;
    }

    private static void validatePositiveNumber(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        try {
            long number = Long.parseLong(value.trim());
            if (number <= 0) {
                throw new IllegalArgumentException(fieldName + " must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid positive number");
        }
    }

    public static void validateBankNumber(String bankNumber) {
        validatePositiveNumber(bankNumber, "Bank number");
    }

    public static void validateBranchNumber(String branchNumber) {
        validatePositiveNumber(branchNumber, "Branch number");
    }

    public static void validateAccountNumber(String accountNumber) {
        validatePositiveNumber(accountNumber, "Account number");
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public String getBranchNumber() {
        return branchNumber;
    }

    public void setBranchNumber(String branchNumber) {
        this.branchNumber = branchNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
            "bankNumber='" + bankNumber + '\'' +
            ", branchNumber='" + branchNumber + '\'' +
            ", accountNumber='" + accountNumber + '\'' +
            '}';
    }
}
