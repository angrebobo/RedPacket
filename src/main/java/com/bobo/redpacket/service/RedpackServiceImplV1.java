package com.bobo.redpacket.service;

import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.common.utils.IdUtils;
import com.bobo.redpacket.common.utils.UtilMix;
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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 20:02 2021/9/21
 */

@Slf4j
@Service("redpackServiceImplV1")
public class RedpackServiceImplV1 implements RedpackService {

    @Autowired
    private RedpackActivityDao activityDao;

    @Autowired
    private RedpackDao packdao;

    @Autowired
    private RedpackDetailDao detailDao;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public CreateActivityResultDTO create(CreateActivityDTO dto) {
        RedpackActivityPO redpackActivityPO = new RedpackActivityPO();
        redpackActivityPO.setVersion(0);
        redpackActivityPO.setTotalAmount(dto.getTotalAmount());
        redpackActivityPO.setSurplusAmount(dto.getTotalAmount());
        redpackActivityPO.setTotal(dto.getTotal());
        redpackActivityPO.setSurplusTotal(dto.getTotal());
        Long id = IdUtils.nextId();
        redpackActivityPO.setId(id);
        activityDao.insert(redpackActivityPO);
        return new CreateActivityResultDTO(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @Override
    public boolean fight(FightRedpackDTO dto) throws RedpackException {

        // ??????????????????
        String lockKey = "activityID".concat(dto.getActivityID().toString());
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            // ????????????
            boolean resultOfLock = rLock.tryLock(30, 10, TimeUnit.SECONDS);
            if(resultOfLock){
                RedpackActivityPO queryPO = new RedpackActivityPO();
                queryPO.setId(dto.getActivityID());
                RedpackActivityPO redpackActivityPO = activityDao.selectOne(queryPO);
                // ??????????????????id????????????
                if(redpackActivityPO == null){
                    throw new RedpackException(RspCode.REDPACK_NOT_EXIST);
                }
                // ???????????????????????????
                BigDecimal surplusAmount = redpackActivityPO.getSurplusAmount();
                Integer surplusTotal = redpackActivityPO.getSurplusTotal();

                // ??????????????????????????????
                if(BigDecimal.ZERO.compareTo(surplusAmount) > 0 || surplusTotal == 0){
                    return false;
                }

                // ??????????????????????????????????????????????????????????????????????????????
                BigDecimal amount;

                // ??????????????????????????????????????????????????????????????????
                if(surplusTotal == 1) {
                    amount = surplusAmount;
                }else {
                    // ?????????????????????0.01-surplusAmount??????????????????
                    amount = UtilMix.getRedpackAmount(surplusAmount);
                }

                // ????????????????????????????????????
                redpackActivityPO.setSurplusTotal(surplusTotal - 1);
                redpackActivityPO.setSurplusAmount(surplusAmount.subtract(amount));
                activityDao.updateById(redpackActivityPO);

                // ???????????????
                RedpackPo redpackPo = new RedpackPo();
                redpackPo.setId(IdUtils.nextId());
                redpackPo.setAmount(amount);
                redpackPo.setActivity_id(redpackActivityPO.getId());
                packdao.insert(redpackPo);

                // ???????????????????????????????????????????????????
                RedpackDetailPO redpackDetailPO = new RedpackDetailPO();
                redpackDetailPO.setAmount(amount);
                redpackDetailPO.setUserID(dto.getUserID());
                redpackDetailPO.setRepackID(redpackPo.getId());
                detailDao.insert(redpackDetailPO);
            }
        } catch (InterruptedException e) {
            log.error("" + e);
            throw new RedpackException(RspCode.ERROR);
        }
        finally{
            // ?????????
            rLock.unlock();
        }

        return true;
    }
}
