package com.weljak.currencyexchange.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class BankAccountRepositoryTest {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @BeforeEach
    void setup() {
        bankAccountRepository.deleteAll();
    }

    @Test
    void repoShouldSaveNewBankAccount() {
        //given
        String testUUID = UUID.randomUUID().toString();
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        BigDecimal initialBalanceUSD = BigDecimal.ZERO;
        BankAccount bankAccount = new BankAccount(testUUID, testName, testSurname, initialBalancePLN, initialBalanceUSD);

        //when
        bankAccountRepository.save(bankAccount);

        //then
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(testUUID);
        assertTrue(bankAccountOptional.isPresent());
        BankAccount entity = bankAccountOptional.get();
        assertEquals(testUUID, entity.getId());
        assertEquals(testName, entity.getName());
        assertEquals(testSurname, entity.getSurname());
        assertEquals(initialBalancePLN, entity.getBalancePLN());
        assertEquals(initialBalanceUSD, entity.getBalanceUSD());
    }

    @Test
    void repoShouldUpdateExistingData() {
        //given
        String testUUID = UUID.randomUUID().toString();
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        BigDecimal initialBalanceUSD = BigDecimal.ZERO;
        BankAccount bankAccount = new BankAccount(testUUID, testName, testSurname, initialBalancePLN, initialBalanceUSD);
        bankAccountRepository.save(bankAccount);

        //when
        BigDecimal updatedPLNBalance = BigDecimal.valueOf(123.3).setScale(2, RoundingMode.CEILING);
        BigDecimal updatedUSDBalance = BigDecimal.valueOf(1133.3).setScale(2, RoundingMode.CEILING);
        BankAccount updated = new BankAccount(testUUID, testName, testSurname, updatedPLNBalance, updatedUSDBalance);
        bankAccountRepository.save(updated);

        //then
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(testUUID);
        assertTrue(bankAccountOptional.isPresent());
        BankAccount entity = bankAccountOptional.get();
        assertEquals(testUUID, entity.getId());
        assertEquals(testName, entity.getName());
        assertEquals(testSurname, entity.getSurname());
        assertEquals(updatedPLNBalance, entity.getBalancePLN());
        assertEquals(updatedUSDBalance, entity.getBalanceUSD());
    }

    @Test
    void repoShouldDeleteData() {
        //given
        String testUUID = UUID.randomUUID().toString();
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        BigDecimal initialBalanceUSD = BigDecimal.ZERO;
        BankAccount bankAccount = new BankAccount(testUUID, testName, testSurname, initialBalancePLN, initialBalanceUSD);
        bankAccountRepository.save(bankAccount);

        //when
        bankAccountRepository.deleteById(testUUID);

        //then
        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(testUUID);
        assertTrue(bankAccountOptional.isEmpty());
    }
}
