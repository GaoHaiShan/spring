package com.myspring.controlller;

import com.myspring.Service.TestService;
import com.myspring.Service.TestServiceTwo;
import servlet.Autowired;
import servlet.MyController;
import servlet.RequestMapping;
import servlet.RequestParam;

@MyController
@RequestMapping("/demo")
public class TestDemo {
    @Autowired
    private TestService service;

    @Autowired
    private TestServiceTwo testServiceTwo;

    @RequestMapping("/testOne")
    public String testOne(@RequestParam("name") String name,@RequestParam("age") int age){
        return service.testOne(name,age);
    }
    @RequestMapping("/testTwo")
    public String testTwo(@RequestParam("name") String name,@RequestParam("age") int age){
        return testServiceTwo.testTwo(name,age);
    }
}
