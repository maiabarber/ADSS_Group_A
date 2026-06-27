package dataaccess.mapper;

import java.sql.ResultSet;

import dataaccess.dto.BankAccountDto;
import employee.domain.BankAccount;

public class BankAccountMapper {

    public static BankAccount toDomain(BankAccountDto bankAccount) {
        return new BankAccount(
            bankAccount.getBankNumber(),
            bankAccount.getBranchNumber(),
            bankAccount.getAccountNumber()
        );
    }

    public static BankAccountDto toDto(BankAccount bankAccount) {
        return new BankAccountDto(
            bankAccount.getBankNumber(),
            bankAccount.getBranchNumber(),
            bankAccount.getAccountNumber()
        );
    }

    public static BankAccount mapResultSetToBankAccount(ResultSet rs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapResultSetToBankAccount'");
    }

}
