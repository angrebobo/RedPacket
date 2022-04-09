package com.bobo.redpacket.service;

import com.bobo.redpacket.BaseTest;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;

public class RedpackServiceTest extends BaseTest {

    @Qualifier("redpackServiceImplV5")
    @Autowired
    private RedpackService redpackService;


    @Test
    public void testCreate() throws RedpackException {

        int count = 5;
        CreateActivityDTO activityDTO = new CreateActivityDTO();
        activityDTO.setTotal(count);
        activityDTO.setTotalAmount(BigDecimal.TEN);
        activityDTO.setUserID(1L);

        CreateActivityResultDTO createActivityResultDTO = redpackService.create(activityDTO);

        for (int i = 0; i < count; i++) {
            FightRedpackDTO fightRedpackDTO = new FightRedpackDTO();
            fightRedpackDTO.setUserID(System.currentTimeMillis());
            fightRedpackDTO.setActivityID(createActivityResultDTO.getActivityID());
            redpackService.fight(fightRedpackDTO);
        }

    }

//    @Test
//    public void testFight() throws RedpackException, InterruptedException {
//        for (int i = 0; i < 5; i++) {
//            FightRedpackDTO fightRedpackDTO = new FightRedpackDTO();
//            fightRedpackDTO.setUserId(System.currentTimeMillis());
//            fightRedpackDTO.setRedpackId(1202201329779363842L);
//            redpackService.fight(fightRedpackDTO);
//            TimeUnit.SECONDS.sleep(1);
//        }
//    }
}
