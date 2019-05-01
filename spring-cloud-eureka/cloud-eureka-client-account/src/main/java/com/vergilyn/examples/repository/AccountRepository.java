package com.vergilyn.examples.repository;

import com.vergilyn.examples.entity.Account;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Integer> {

    @Modifying
    @Query("update Account set amount = amount - ?2 where userId = ?1")
    int decreaseAccount(String userId, Double amount);
}
