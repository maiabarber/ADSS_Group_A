package employees.domain;

public class BankAccount {
    private String bankNumber;
    private String branchNumber;
    private String accountNumber;

    public BankAccount() {
    }

    public BankAccount(String bankNumber, String branchNumber, String accountNumber) {
        this.bankNumber = bankNumber;
        this.branchNumber = branchNumber;
        this.accountNumber = accountNumber;
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
