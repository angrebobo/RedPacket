package com.bobo.redpacket.service;

import com.bobo.redpacket.common.Const;
import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.common.utils.IdUtils;
import com.bobo.redpacket.common.utils.UtilMix;
import com.bobo.redpacket.dao.RedpackActivityDao;
import com.bobo.redpacket.dao.RedpackDao;
import com.bobo.redpacket.dao.RedpackDetailDao;
import com.bobo.redpacket.exception.OptimisticLockException;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import com.bobo.redpacket.model.po.RedpackActivityPO;
import com.bobo.redpacket.model.po.RedpackDetailPO;
import com.bobo.redpacket.model.po.RedpackPo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author: HuangSiBo
 * @Description: 乐观锁版本，乐观锁在操作数据时非常乐观，认为别人不会同时修改数据。因此乐观锁不会加锁，只是在更新时判断一下数据是否被修改过，被修改过的话就放弃更新。
 * 乐观锁在这种场景中效率很低，因为在高竞争场景中，线程获取不到锁会不断自旋，浪费CPU资源
 * @Data: Created in 14:33 2021/9/20
 */

@Slf4j
@Service("redpackServiceImplV2")
public class RedpackServiceImplV2 implements RedpackService {

    @Autowired
    private RedpackActivityDao activityDao;

    @Autowired
    private RedpackDao packdao;

    @Autowired
    private RedpackDetailDao detailDao;

    @Resource(name = "transactionManager")
    private DataSourceTransactionManager txManager;

    /**
     * @Description 发红包，将要发的红包信息存入数据表t_redpack_activity中
     * @param
     * @return
     */
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

    @Override
    public boolean fight(FightRedpackDTO dto) throws RedpackException {

        boolean isFail = false;
        // 记录重试次数
        int i = 0;

        do {
            isFail = false;
            i++;

            // 开启事务
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            // 设置事务的传播方式
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus txStatus = txManager.getTransaction(def);

            try {
                RedpackActivityPO queryPO = new RedpackActivityPO();
                queryPO.setId(dto.getActivityID());
                RedpackActivityPO redpackActivityPO = activityDao.selectOne(queryPO);
                // 查询红包活动id是否存在
                if(redpackActivityPO == null){
                    throw new RedpackException(RspCode.REDPACK_NOT_EXIST);
                }

                // 红包剩余金额和个数
                BigDecimal surplusAmount = redpackActivityPO.getSurplusAmount();
                Integer surplusTotal = redpackActivityPO.getSurplusTotal();

                // 红包是否还有剩余金额
                if(BigDecimal.ZERO.compareTo(surplusAmount) > 0 || surplusTotal == 0){
                    return false;
                }

                // 生成一个子红包，绑定用户编号，表示用户抢到了该子红包
                BigDecimal amount;

                // 如果红包还剩一个，那么剩余金额全部注入子红包
                if(surplusTotal == 1) {
                    amount = surplusAmount;
                }else {
                    // 生成一个金额在0.01-surplusAmount之间的子红包
                    amount = UtilMix.getRedpackAmount(surplusAmount);
                }

                // 更新红包的剩余金额和个数
                redpackActivityPO.setSurplusTotal(surplusTotal - 1);
                redpackActivityPO.setSurplusAmount(surplusAmount.subtract(amount));
                Integer row = activityDao.updateById(redpackActivityPO);
                if(row == 0){
                    // 如果更新失败，抛出乐观锁异常
                    throw  new OptimisticLockException();
                }

                // 生成子红包
                RedpackPo redpackPo = new RedpackPo();
                redpackPo.setId(IdUtils.nextId());
                redpackPo.setAmount(amount);
                redpackPo.setActivity_id(redpackActivityPO.getId());
                packdao.insert(redpackPo);

                // 生成红包明细，绑定子红包和用户编号
                RedpackDetailPO redpackDetailPO = new RedpackDetailPO();
                redpackDetailPO.setAmount(amount);
                redpackDetailPO.setUserID(dto.getUserID());
                redpackDetailPO.setRepackID(redpackPo.getId());
                detailDao.insert(redpackDetailPO);
                txManager.commit(txStatus);
            } catch (OptimisticLockException e) {
                txManager.rollback(txStatus);
                // 如果该线程的自旋次数未到3次，则重新尝试获得乐观锁
                if(i != Const.OPTIMISTIC_LOCK_RETRY_MAX){
                    isFail = true;
                }else {
                    throw new RedpackException(RspCode.ERROR);
                }
            }catch (Throwable e) {
                log.error("", e);
                txManager.rollback(txStatus);
                throw new RedpackException(RspCode.ERROR);
            }
        }while (isFail);


        return true;
    }
}
