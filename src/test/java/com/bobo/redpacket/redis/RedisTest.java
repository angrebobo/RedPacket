package com.bobo.redpacket.redis;

import com.alibaba.fastjson.JSON;
import com.bobo.redpacket.BaseTest;
import com.bobo.redpacket.common.Const;
import com.bobo.redpacket.model.po.RedpackPo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisTest extends BaseTest {

    @Resource
    private RedisTemplate<String, RedpackPo> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test(){
        List<RedpackPo> queue = Lists.newArrayList();
        for (Integer i = 0; i < 5; i++) {
            RedpackPo redpackPO = new RedpackPo();
            redpackPO.setId(System.currentTimeMillis());
            redpackPO.setAmount(BigDecimal.ONE);
            redpackPO.setStatus(RedpackPo.ENABLE);
            redpackPO.setActivity_id(1L);
            queue.add(redpackPO);
        }

        //加入redis
        String queueKey = Const.REDIS_QUEUE_NAME.concat("1");

        System.out.println(JSON.toJSONString(queue));
        redisTemplate.opsForList().rightPushAll(queueKey, queue);
        RedpackPo redpackPO = (RedpackPo) redisTemplate.opsForList().leftPop(queueKey);
        System.out.println(redpackPO);

    }


    @Test
    public void lock() throws InterruptedException {
        RLock lock = redissonClient.getLock("lock");

        boolean b1 = lock.tryLock(3, TimeUnit.SECONDS);
        log.info("b1 is : {}", b1);
        boolean b2 = lock.tryLock(3, TimeUnit.SECONDS);
        log.info("b2 is : {}", b2);
        TimeUnit.SECONDS.sleep(5);

//        boolean b2 = lock.tryLock(3, TimeUnit.SECONDS);
//        log.info("b2 is : {}", b2);
//
        lock.unlock();
        lock.unlock();
    }
}
