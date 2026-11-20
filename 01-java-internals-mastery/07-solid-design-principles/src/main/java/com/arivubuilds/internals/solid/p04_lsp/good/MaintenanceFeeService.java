package com.arivubuilds.internals.solid.p04_lsp.good;

import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;

@Service
public class MaintenanceFeeService {
    public void deductMaintenanceFee(List<WithdrawableAccount> accounts, BigDecimal fee) {
        for (WithdrawableAccount account : accounts) account.withdraw(fee);
    }
}
