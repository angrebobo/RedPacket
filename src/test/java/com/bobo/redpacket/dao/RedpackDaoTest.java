package com.bobo.redpacket.dao;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.Condition;
import com.bobo.redpacket.BaseTest;
import com.bobo.redpacket.model.po.RedpackPo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;


public class RedpackDaoTest extends BaseTest {

    @Autowired
    private RedpackDao dao;

    @Test
    public void test() {

        RedpackPo redpackPO = new RedpackPo();
        redpackPO.setStatus(1);
        redpackPO.setActivity_id(1L);
        BigDecimal money = new BigDecimal(12.9);
        redpackPO.setAmount(money);
        dao.insert(redpackPO);

        List<RedpackPo> redpackPOS = dao.selectList(Condition.empty());
        System.out.println(JSON.toJSONString(redpackPOS, true));
    }
}
