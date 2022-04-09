package com.bobo.redpacket.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.bobo.redpacket.common.Const;
import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.common.utils.IdUtils;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: HuangSiBo
 * @Description: 创建红包时预先分配好红包，再用version字段的乐观锁机制
 * 将冲突分散到红包表，线程抢到分配好的红包后再去更新数据库，当多个线程抢到同一个小红包时，只能
 * 有一个线程能成功更新到数据库。对比版本2，该版本将冲突分散到了红包表，效果更好。
 * @Data: Created in 14:11 2021/9/21
 */
@Slf4j
@Service("redpackServiceImplV4")
public class RedpackServiceImplV4 implements RedpackService {

    @Autowired
    private RedpackActivityDao activityDao;
    @Autowired
    private RedpackDao redpackDao;
    @Autowired
    protected RedpackDetailDao detailDao;
    @Resource(name = "transactionManager")
    private DataSourceTransactionManager txManager;


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

        //平均分配
        BigDecimal decimal = dto.getTotalAmount().divide(dto.getTotalAmount(), RoundingMode.HALF_DOWN)
                .setScale(2, RoundingMode.HALF_DOWN);

        // 预先分配红包
        for (int i = 0; i < dto.getTotal(); i++) {
            RedpackPo redpackPO = new RedpackPo();
            redpackPO.setId(IdUtils.nextId());
            redpackPO.setAmount(decimal);
            redpackPO.setStatus(RedpackPo.ENABLE);
            redpackPO.setActivity_id(redpackActivityPO.getId());
            redpackPO.setVersion(0);
            redpackDao.insert(redpackPO);
        }
        return new CreateActivityResultDTO(id);
    }

    @Override
    public boolean fight(FightRedpackDTO dto) throws RedpackException {

        boolean isFail = false;
        int i = 0;

        do {
            isFail = false;
            i++;
            // 开启事物
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus txStatus = txManager.getTransaction(def);

            RedpackPo query = new RedpackPo();
            query.setStatus(RedpackPo.ENABLE);
            query.setActivity_id(dto.getActivityID());

            // 根据Status和Activity_id两个属性，查询预先分配好的红包
            EntityWrapper<RedpackPo> wrapper = new EntityWrapper<>(query);
            List<RedpackPo> pos = redpackDao.selectList(wrapper);

            if( CollectionUtils.isEmpty(pos) ){
                return false;
            }

            // 用ThreadLocalRandom生成一个随机下标，从pos中随机取出一个红包
            // ThreadLocalRandom类在多线程环境下比Random类更高效
            int index = ThreadLocalRandom.current().nextInt(pos.size());
            RedpackPo redpackPo = pos.get(index);
            redpackPo.setStatus(RedpackPo.DISABLE);

            try{
                // 乐观锁更新
                // 拿到随机红包后，去更新数据库
                Integer row = redpackDao.updateById(redpackPo);
                if(row == 0){
                    throw new OptimisticLockException();
                }
                RedpackDetailPO detailPO = new RedpackDetailPO();
                detailPO.setAmount(redpackPo.getAmount());
                detailPO.setRepackID(redpackPo.getActivity_id());
                detailPO.setUserID(dto.getUserID());
                detailDao.insert(detailPO);
                txManager.commit(txStatus);
            } catch (OptimisticLockException e) {
                txManager.rollback(txStatus);
                if (i != Const.OPTIMISTIC_LOCK_RETRY_MAX) {
                    isFail = true;
                } else {
                    throw new RedpackException(RspCode.ERROR);
                }
            } catch (Throwable e){
                log.error("" + e);
                txManager.rollback(txStatus);
                throw new RedpackException(RspCode.ERROR);
            }
        } while (isFail);

        return true;
    }
}
