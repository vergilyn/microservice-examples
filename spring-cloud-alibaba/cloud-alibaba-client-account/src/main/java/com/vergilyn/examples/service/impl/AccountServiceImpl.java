package com.vergilyn.examples.service.impl;

import javax.transaction.Transactional;

import com.vergilyn.examples.dto.AccountDTO;
import com.vergilyn.examples.repository.AccountRepository;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.AccountService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public ObjectResponse<Void> decrease(AccountDTO accountDTO) {

        int account = accountRepository.decreaseAccount(accountDTO.getUserId(), accountDTO.getAmount().doubleValue());

        return account > 0 ? ObjectResponse.success() : ObjectResponse.failure();
    }
}
