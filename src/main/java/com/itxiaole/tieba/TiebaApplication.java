package com.itxiaole.tieba;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itxiaole.tieba.mapper")
public class TiebaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiebaApplication.class, args);
    }

}
