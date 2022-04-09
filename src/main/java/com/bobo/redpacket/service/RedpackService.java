package com.bobo.redpacket.service;

import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 14:58 2021/9/20
 */
public interface RedpackService {

    /**
     * @Description 发红包
     * @param
     * @return
     */
    CreateActivityResultDTO create(CreateActivityDTO createActivityDTO);

    /**
     * @Description 抢红包
     * @param
     * @return
     */
    boolean fight(FightRedpackDTO fightRedpackDTO) throws RedpackException;

}
