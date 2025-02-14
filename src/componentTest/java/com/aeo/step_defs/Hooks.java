package com.aeo.step_defs;

import io.cucumber.java.After;

public class Hooks {

    @After
    public void timeoutAfterTest() throws InterruptedException {
        Thread.sleep(5000);
    }

}
