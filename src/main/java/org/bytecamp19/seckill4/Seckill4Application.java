package org.bytecamp19.seckill4;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.bytecamp19.seckill4.mapper")
public class Seckill4Application {

    public static void main(String[] args) {
        SpringApplication.run(Seckill4Application.class, args);
    }

}
