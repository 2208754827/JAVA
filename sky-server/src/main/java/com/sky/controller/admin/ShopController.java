package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;


    @PutMapping("/{status}")
    public Result setShop(@PathVariable  Integer status){
        log.info("获取到的设置值是{}",status==1 ? "营业中":"打样中");

        redisTemplate.opsForValue().set("SHOP_STATUS",status);
        return Result.success();


    }



    @GetMapping("/status")
    public Result <Integer> getShop(){


       Integer status= (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        return Result.success(status);


    }
}
