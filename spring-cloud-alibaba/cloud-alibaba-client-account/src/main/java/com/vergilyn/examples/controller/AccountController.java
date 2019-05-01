package com.vergilyn.examples.controller;

import com.vergilyn.examples.dto.AccountDTO;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.AccountService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/decrease")
    ObjectResponse<Void> decrease(@RequestBody AccountDTO accountDTO){
        log.info("请求账户微服务：{}", accountDTO.toString());

        return accountService.decrease(accountDTO);
    }

}
