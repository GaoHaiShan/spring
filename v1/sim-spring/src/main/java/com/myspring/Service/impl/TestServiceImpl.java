package com.myspring.Service.impl;

import com.myspring.Service.TestService;
import servlet.MyService;

@MyService
public class TestServiceImpl implements TestService {
    @Override
    public String testOne(String name, int age) {
        return "TestServiceImpl test one  "+ name + " : "+ age ;
    }
}
