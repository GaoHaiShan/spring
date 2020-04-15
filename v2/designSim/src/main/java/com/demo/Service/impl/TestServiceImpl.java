package com.demo.Service.impl;

import com.demo.Service.TestService;
import com.spring.annotation.MyService;

@MyService
public class TestServiceImpl implements TestService {
    @Override
    public String testOne(String name, int age) {
        return "TestServiceImpl test one  "+ name + " : "+ age ;
    }

    @Override
    public String toString() {
        return "testOne";
    }
}
