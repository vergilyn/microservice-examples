package com.vergilyn.examples.service;

import com.vergilyn.examples.dto.AccountDTO;
import com.vergilyn.examples.response.ObjectResponse;

public interface AccountService {
    /** 扣用户钱 */
    ObjectResponse<Void> decrease(AccountDTO accountDTO);
}
