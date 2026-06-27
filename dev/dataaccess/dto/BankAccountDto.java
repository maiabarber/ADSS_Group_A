package dataaccess.dto;


public final class BankAccountDto {
    private final String bankNumber;
    private final String branchNumber;
    private final String accountNumber;

    public BankAccountDto(String bankNumber, String branchNumber, String accountNumber) {
        this.bankNumber = bankNumber;
        this.branchNumber = branchNumber;
        this.accountNumber = accountNumber;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getBranchNumber() {
        return branchNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}