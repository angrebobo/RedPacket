package com.bobo.redpacket.service;

import com.bobo.redpacket.common.Const;
import com.bobo.redpacket.common.utils.IdUtils;
import com.bobo.redpacket.dao.RedpackActivityDao;
import com.bobo.redpacket.dao.RedpackDao;
import com.bobo.redpacket.dao.RedpackDetailDao;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import com.bobo.redpacket.model.dto.RedpackPersistDTO;
import com.bobo.redpacket.model.po.RedpackActivityPO;
import com.bobo.redpacket.model.po.RedpackPo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: HuangSiBo
 * @Description: 抢到红包后，再异步更新红包明细表
 * @Data: Created in 19:25 2021/9/21
 */
@Slf4j
@Service("redpackServiceImplV6")
public class RedpackServiceImplV6 implements RedpackService {

    @Autowired
    private RedpackActivityDao activityDao;
    @Autowired
    private RedpackDao redpackDao;
    @Autowired
    protected RedpackDetailDao detailDao;
    @Resource
    private RedisTemplate<String, RedpackPo> redisTemplate;
    @Resource
    private RedisTemplate<String, RedpackPersistDTO> persistRedisTemplate;


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
        Long activityId = dto.getActivityID();

        String queueKey = Const.REDIS_QUEUE_NAME.concat(activityId.toString());

        RedpackPo po = (RedpackPo) this.redisTemplate.opsForList().leftPop(queueKey);
        if (po == null) {
            return false;
        }

        RedpackPersistDTO persistDTO = new RedpackPersistDTO();
        BeanUtils.copyProperties(po, persistDTO);

        persistDTO.setUserID(dto.getUserID());
        persistDTO.setCreateTime(LocalDateTime.now());

        persistRedisTemplate.opsForList().rightPush(Const.REDIS_PERSIST_QUEUE_NAME_BAK, persistDTO);
        return false;
    }
}
