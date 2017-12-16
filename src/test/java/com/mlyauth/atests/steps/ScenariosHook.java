package com.mlyauth.atests.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ScenariosHook {

    private TransactionStatus txStatus;

    @Autowired
    private PlatformTransactionManager txMgr;

    @Before
    public void markTheTransaction() {
        txStatus = txMgr.getTransaction(new DefaultTransactionDefinition());
    }

    @After
    public void rollbackTheTransaction() {
        txMgr.rollback(txStatus);
    }


}
