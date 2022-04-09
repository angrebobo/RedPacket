package com.bobo.redpacket;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;


public class AllSpringbeanTest extends BaseTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void test(){
        for (String definitionName : context.getBeanDefinitionNames()) {

            System.out.println(definitionName);
        }
    }
}
