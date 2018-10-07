package com.xiaobai.distributed.affair.impl;

import com.xiaobai.distributed.affair.Affair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAffair implements Affair{

    private static final Logger log = LoggerFactory.getLogger(MainAffair.class);

    public void execute() {
        //主线程操作
        for(int i=0;i<10000;i++) {
            if(i == 0) {
                log.info(Thread.currentThread().getName() + ":Locked Affair Begin...");
            }else if(i == 9999) {
                log.info(Thread.currentThread().getName() + ":Locked Affair End!");
            }
        }
    }
}
