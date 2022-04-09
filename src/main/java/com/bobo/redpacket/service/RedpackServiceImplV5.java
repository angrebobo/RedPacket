package com.bobo.redpacket.service;

import com.bobo.redpacket.common.Const;
import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.common.utils.IdUtils;
import com.bobo.redpacket.dao.RedpackActivityDao;
import com.bobo.redpacket.dao.RedpackDao;
import com.bobo.redpacket.dao.RedpackDetailDao;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import com.bobo.redpacket.model.po.RedpackActivityPO;
import com.bobo.redpacket.model.po.RedpackDetailPO;
import com.bobo.redpacket.model.po.RedpackPo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author: HuangSiBo
 * @Description: 将预先分配好的红包存入redis中，抢红包时从redis取出一个分配好的红包并更新到数据库
 *
 * @Data: Created in 18:55 2021/9/21
 */
@Slf4j
@Service("redpackServiceImplV5")
public class RedpackServiceImplV5 implements RedpackService{

    @Autowired
    private RedpackActivityDao activityDao;
    @Autowired
    private RedpackDao redpackDao;
    @Autowired
    protected RedpackDetailDao detailDao;
    @Resource(name = "transactionManager")
    private DataSourceTransactionManager txManager;
    @Resource
    private RedisTemplate<String, RedpackPo> redisTemplate;




    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @Override
    public CreateActivityResultDTO create(CreateActivityDTO dto) {
        RedpackActivityPO redpackActivityPO = new RedpackActivityPO();
        redpackActivityPO.setVersion(0);
        redpackActivityPO.setSurplusAmount(dto.getTotalAmount());
        redpackActivityPO.setTotalAmount(dto.getTotalAmount());
        redpackActivityPO.setTotal(dto.getTotal());
        redpackActivityPO.setSurplusTotal(dto.getTotal());
        Long id = IdUtils.nextId();
        redpackActivityPO.setId(id);
        activityDao.insert(redpackActivityPO);

        // 平均分配
        BigDecimal decimal = dto.getTotalAmount().divide(new BigDecimal(dto.getTotal()), RoundingMode.HALF_DOWN)
                .setScale(2, RoundingMode.HALF_DOWN);

        List<RedpackPo> queue = Lists.newArrayList();
        for (Integer i = 0; i < dto.getTotal(); i++) {
            RedpackPo redpackPO = new RedpackPo();
            redpackPO.setId(IdUtils.nextId());
            redpackPO.setAmount(decimal);
            redpackPO.setStatus(RedpackPo.ENABLE);
            redpackPO.setActivity_id(redpackActivityPO.getId());
            redpackPO.setVersion(0);
            redpackDao.insert(redpackPO);
            queue.add(redpackPO);
        }

        // 定义查询的key
        String queueKey = Const.REDIS_QUEUE_NAME.concat(redpackActivityPO.getId().toString());
        // 将预先分配的红包全部存入redis中
        redisTemplate.opsForList().rightPushAll(queueKey, queue);

        return new CreateActivityResultDTO(id);
    }

    @Override
    public boolean fight(FightRedpackDTO dto) throws RedpackException {
        Long RedpackActivityID = dto.getActivityID();

        String queueKey = Const.REDIS_QUEUE_NAME.concat(RedpackActivityID.toString());

        RedpackPo po = redisTemplate.opsForList().leftPop(queueKey);
        if(po == null){
            return false;
        }



        // 开启事物
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus txStatus = txManager.getTransaction(def);
        try {
            po.setStatus(RedpackPo.DISABLE);
            redpackDao.updateById(po);
            RedpackDetailPO detailPO = new RedpackDetailPO();
            detailPO.setAmount(po.getAmount());
            detailPO.setRepackID(po.getActivity_id());
            detailPO.setUserID(dto.getUserID());
            detailDao.insert(detailPO);
            txManager.commit(txStatus);

            System.out.println(dto.getUserID() + "抢到红包，金额为" + po.getAmount() + "元");
        }catch (Throwable e){
            log.error("", e);
            txManager.rollback(txStatus);
            throw new RedpackException(RspCode.ERROR);
        }
        return true;
    }
}
