package com.distributionlock.distributionlock.conntroller;

import com.distributionlock.distributionlock.entity.Goods;
import com.distributionlock.distributionlock.entity.Order;
import com.distributionlock.distributionlock.mapper.GoodsMapper;
import com.distributionlock.distributionlock.mapper.OrderMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    OrderMapper orderMapper;

    @PostMapping("/createOrder}")
    public String  createOrder(@RequestBody Order order){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        Long goodsId = order.getGoodsId();
        //
        String goodsStockkey = "shop:goods:stock:"+ goodsId;
        String redisLock = "distribution:lock:"+goodsId;
        //查库存
        String stock = operations.get(goodsStockkey);
        Map<String,String> hashMap = new HashMap<>();
        if (StringUtils.isEmpty(stock)){
            //重建K
            Goods goodsById = goodsMapper.getGoodsById(goodsId);
            stock = goodsById.getStock().toString();
            operations.set(goodsStockkey,goodsById.getStock().toString());
        }


        int stockInt = Integer.parseInt(stock);

        //在分布式环境下存在线程安全问题
        Boolean lock = operations.setIfAbsent(redisLock, "lock",300 , TimeUnit.MILLISECONDS);

        try {
            if (lock){
                if (stockInt > 0){
                    //减库存
                    stockInt = stockInt - 1;
                    //更新数据库
                    goodsMapper.updateStockById(goodsId,stockInt);
                    //生成订单
                    String uuid = UUID.randomUUID().toString().replace("-","");
                    order.setOrderId(uuid);
                    orderMapper.createOrder(order);

                    //更新redis
                    operations.set(goodsStockkey,String.valueOf(stockInt));
                    return uuid;
                }else{
                    return "商品已经被抢购完了！";
                }
            }
        }finally {
            stringRedisTemplate.delete(redisLock);
        }
        return "拿不到锁!";

    }

}
