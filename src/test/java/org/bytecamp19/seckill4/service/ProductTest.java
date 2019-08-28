package org.bytecamp19.seckill4.service;

import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductTest {
    @Autowired
    private ProductService productService;

    private final long pid = 177620431;
    private final long pid_1 = 1111111;

    @Test
    public void getProductTest() throws ForbiddenException {
        // 验证正确pid
        Product product = productService.getProduct(pid);
        assertNotNull(product);
        System.out.println(product);
        // 验证错误pid_1
        Product product1 = productService.getProduct(pid_1);
        assertNull(product1);
        System.out.println(product1);
    }
}
