package com.bobo.redpacket.service;

import com.bobo.redpacket.common.RspCode;
import com.bobo.redpacket.common.utils.AssertUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author: HuangSiBo
 * @Description: 悲观锁版本
 *
 * @Data: Created in 20:17 2021/9/20
 */
@Service("redpackServiceImplV3")
public class RedpackServiceImplV3 implements RedpackService {

    @Autowired
    private RedpackActivityDao activityDao;
    @Autowired
    private RedpackDao redpackDao;
    @Autowired
    protected RedpackDetailDao detailDao;

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
        return new CreateActivityResultDTO(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    @Override
    public boolean fight(FightRedpackDTO dto) throws RedpackException {
        // 这里实现了悲观锁，第一个通过ID查到红包的线程已经将该行数据锁定
        RedpackActivityPO redpackActivityPO = activityDao.selectByIDForUpdate(dto.getActivityID());
        if(activityDao == null){
            throw new RedpackException(RspCode.REDPACK_NOT_EXIST);
        }

        //红包剩余金额
        BigDecimal surplusAmount = redpackActivityPO.getSurplusAmount();
        Integer surplusTotal = redpackActivityPO.getSurplusTotal();

        //红包是否还有剩余金额
        if (BigDecimal.ZERO.compareTo(surplusAmount) > 0 || surplusTotal == 0) {
            return false;
        }

        // 生成一个子红包，绑定用户编号，表示用户抢到了该子红包
        BigDecimal amount = BigDecimal.ZERO;

        //如果剩下一次了，那么此次金额就是剩余金额
        if (surplusTotal == 1) {
            amount = surplusAmount;
        } else {
            //否则就在0和剩余金额之间生成一个随机数
            amount = UtilMix.getRedpackAmount(surplusAmount);
        }

        // 更新红包的剩余金额和个数
        redpackActivityPO.setSurplusTotal(surplusTotal - 1);
        redpackActivityPO.setSurplusAmount(surplusAmount.subtract(amount));
        Integer row = activityDao.updateById(redpackActivityPO);
        AssertUtil.assertOne(row, "更新红包活动失败");

        // 生成子红包
        RedpackPo redpackPo = new RedpackPo();
        redpackPo.setId(IdUtils.nextId());
        redpackPo.setAmount(amount);
        redpackPo.setActivity_id(redpackActivityPO.getId());
        redpackDao.insert(redpackPo);

        // 生成红包明细，绑定子红包和用户编号
        RedpackDetailPO redpackDetailPO = new RedpackDetailPO();
        redpackDetailPO.setAmount(amount);
        redpackDetailPO.setUserID(dto.getUserID());
        redpackDetailPO.setRepackID(redpackPo.getId());
        detailDao.insert(redpackDetailPO);

        return true;
    }
}
