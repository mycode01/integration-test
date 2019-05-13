package com.example.demo.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HelloControllerTests {

    private MockMvc mockMvc;
    @MockBean
    HelloController helloController;

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(helloController).build();
    }

    @Test
    public void getHelloTest() throws Exception{
        String name = "world!";
        given(this.helloController.hello(name)).willReturn("hello! "+name);

        mockMvc.perform(get("/hello/{name}", name))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
