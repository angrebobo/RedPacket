package com.bobo.redpacket.version;

import com.bobo.redpacket.BaseTest;
import com.bobo.redpacket.Verson.VersionFactory;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import com.bobo.redpacket.service.RedpackService;
import com.google.common.base.Stopwatch;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class BaseVersionTest extends BaseTest {

    @Autowired
    private VersionFactory versionFactory;

    protected List<Long> userList = new ArrayList<>();

    Integer[] threadCountGroup = new Integer[]{5, 10, 30, 100,1000}; //并发数

    @Before
    public void prepareData() {
        //生成用户
        long l = ThreadLocalRandom.current().nextLong(1000);
        for (int i = 0; i < 1000; i++) {
            userList.add(l);
        }
    }

    @Test
    public void draw() throws InterruptedException {

        RedpackService redpackService = versionFactory.route("v1");
        for (Integer tc : threadCountGroup) {

            // 发放一个红包
            CreateActivityDTO activityDTO = new CreateActivityDTO();
            activityDTO.setTotal(5);
            activityDTO.setTotalAmount(BigDecimal.TEN);
            activityDTO.setUserID(1L);
            CreateActivityResultDTO createActivityResultDTO = redpackService.create(activityDTO);
            Long activityId = createActivityResultDTO.getActivityID();

            Stopwatch stopwatch = Stopwatch.createStarted();

            AtomicInteger success = new AtomicInteger(0); // 代表成功数，如果一直抢成功数=红包总数
            AtomicInteger exception = new AtomicInteger(0); // 并发异常
            ExecutorService executorService = Executors.newFixedThreadPool(tc);

            // 模拟多个随机用户一起抢红包
            for (Integer i = 0; i < tc; i++) {
                Long userId = this.userList.get(Math.abs(new Random().nextInt() % this.userList.size()));
                executorService.submit(() -> {
                    FightRedpackDTO fightRedpackDTO = new FightRedpackDTO();
                    fightRedpackDTO.setUserID(userId);
                    fightRedpackDTO.setActivityID(activityId);
                    try {
                        boolean flag = redpackService.fight(fightRedpackDTO);
                        if (flag) {
                            success.getAndIncrement();
                        } else {

                        }
                    } catch (RedpackException e) {
                        // 并发异常
                        exception.getAndIncrement();
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.MINUTES);
            stopwatch.stop();
            // 打印测试结果
            System.out.println(String.format("concurrent %s  cost %s s, success %s exception %s TPS is %s/s", tc,
                    stopwatch.elapsed(TimeUnit.SECONDS), success.get(), exception.get(),
                    success.get() * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS) * 1000));
        }
    }


}
