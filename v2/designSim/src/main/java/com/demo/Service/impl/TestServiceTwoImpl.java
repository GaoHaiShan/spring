package com.demo.Service.impl;


import com.demo.Service.TestServiceTwo;
import com.spring.annotation.MyService;

@MyService
public class TestServiceTwoImpl implements TestServiceTwo {

    @Override
    public String testTwo(String name, int age) {
        return "TestServiceTwoImpl Test Two " + name +" : "+ age;
    }

    @Override
    public String toString() {
        return "testTwo";
    }
}
