package dataaccess.dto;

public final class BankAccountDto {
    private final String bankCode;
    private final String branchCode;
    private final String accountNumber;

    public BankAccountDto(String bankCode, String branchCode, String accountNumber) {
        this.bankCode = bankCode;
        this.branchCode = branchCode;
        this.accountNumber = accountNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}