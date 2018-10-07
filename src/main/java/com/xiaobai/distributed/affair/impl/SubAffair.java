package com.xiaobai.distributed.affair.impl;

import com.xiaobai.distributed.affair.Affair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubAffair implements Affair{

    private static final Logger log = LoggerFactory.getLogger(SubAffair.class);

    public void execute() {
        //子线程操作
        for(int i=0;i<1000;i++) {
            if(i == 0) {
                log.info(Thread.currentThread().getName() + ":Locked Affair Begin...");
            }else if(i == 999) {
                log.info(Thread.currentThread().getName() + ":Locked Affair End!");
            }
        }
    }
}
