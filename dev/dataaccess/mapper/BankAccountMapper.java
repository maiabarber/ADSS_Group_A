package dataaccess.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static BankAccountDto mapResultSetToBankAccount(ResultSet rs)
            throws SQLException {

        return new BankAccountDto(
                rs.getString("bank_number"),
                rs.getString("bank_branch_number"),
                rs.getString("bank_account_number")
        );
    }

}
