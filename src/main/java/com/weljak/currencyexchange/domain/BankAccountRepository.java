package com.weljak.currencyexchange.domain;

import com.weljak.currencyexchange.domain.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    Optional<BankAccount> findByUuid(String uuid);
}
