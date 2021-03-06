package org.bytecamp19.seckill4.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.bytecamp19.seckill4.cache.InventoryManager;
import org.bytecamp19.seckill4.cache.LayeringCache;
import org.bytecamp19.seckill4.cache.LayeringCacheManager;
import org.bytecamp19.seckill4.entity.Product;
import org.bytecamp19.seckill4.error.ForbiddenException;
import org.bytecamp19.seckill4.interceptor.costlogger.CostLogger;
import org.bytecamp19.seckill4.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by LLAP on 2019/8/4.
 * Copyright (c) 2019 L. Xiao, F. Baoren, L. Yangzhou. All rights reserved.
 */
@Service
public class ProductService {
    private Logger logger = LoggerFactory.getLogger(ProductService.class);
    private ProductMapper productMapper;
    private LayeringCacheManager cacheManager;
    private InventoryManager inventoryManager;

    public ProductService(ProductMapper productMapper, LayeringCacheManager cacheManager,
                          InventoryManager inventoryManager) {
        this.productMapper = productMapper;
        this.cacheManager = cacheManager;
        this.inventoryManager = inventoryManager;
    }

    //    @Cacheable(
//            key = "'product:' + #pid",
//            value = "productCache",
//            cacheManager = "cacheManager"
//    )
    @CostLogger(LEVEL = CostLogger.Level.WARN)
    @DS("slave")
    public Product getProduct(long pid) throws ForbiddenException {
        LayeringCache cache = (LayeringCache)cacheManager.getCache("productCache");
        Product ret = null;
        if (cache != null) {
            Cache.ValueWrapper val = cache.get("product:" + pid);
            if (val != null) ret = (Product)val.get();
            // if cache misses
            if (ret == null) {
                long start = System.currentTimeMillis();
                ret = productMapper.selectById(pid);
                logger.warn("SQL query {} ms", (System.currentTimeMillis() - start));
                if (ret != null) {
                    cache.put("product:" + pid, ret);
                }
            }
        }
        // Get inventory
        if (ret != null) {
            // 简化init过程（如果无则新建，如果有则不动，避免两次查询）
            Long inv = inventoryManager.getInventory(pid);
//            if (inv < 0) inventoryManager.initInventory(pid, ret.getCount());
            if (inv == null) throw new ForbiddenException("Cannot initialize inventory");
            else ret.setCount(inv.intValue());
        }
        return ret;
    }
}
