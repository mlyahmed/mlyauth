package com.mlyauth.atests.steps;

import com.mlyauth.atests.world.CurrentPersonHolder;
import com.mlyauth.beans.PersonBean;
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

    @Autowired
    private CurrentPersonHolder currentPersonHolder;

    @Before
    public void markTheTransaction() {
        currentPersonHolder.setCurrentPerson(PersonBean.newInstance().setUsername("root").setPassword("root".toCharArray()));
        txStatus = txMgr.getTransaction(new DefaultTransactionDefinition());
    }

    @After
    public void rollbackTheTransaction() {
        txMgr.rollback(txStatus);
    }


}
