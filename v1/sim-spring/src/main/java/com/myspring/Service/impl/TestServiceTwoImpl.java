package com.myspring.Service.impl;

import com.myspring.Service.TestServiceTwo;
import servlet.MyService;

@MyService
public class TestServiceTwoImpl implements TestServiceTwo {
    @Override
    public String testTwo(String name, int age) {
        return "TestServiceTwoImpl Test Two " + name +" : "+ age;
    }
}
