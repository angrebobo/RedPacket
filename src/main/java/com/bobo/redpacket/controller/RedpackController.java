package com.bobo.redpacket.controller;

import com.bobo.redpacket.Verson.VersionFactory;
import com.bobo.redpacket.exception.RedpackException;
import com.bobo.redpacket.model.dto.CreateActivityDTO;
import com.bobo.redpacket.model.dto.CreateActivityResultDTO;
import com.bobo.redpacket.model.dto.FightRedpackDTO;
import com.bobo.redpacket.model.dto.ResultMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 14:55 2021/9/20
 */

@Slf4j
@RestController
@RequestMapping("/redpack")
public class RedpackController {

    @Autowired
    private VersionFactory versionFactory;

    @RequestMapping(value = "createActivity", method = RequestMethod.POST)
    public ResultMessageDTO<CreateActivityResultDTO> createActivity(@RequestHeader("version") String version, @RequestBody CreateActivityDTO dto){
        return ResultMessageDTO.success(versionFactory.route(version).create(dto));
    }

    @RequestMapping(value = "fight", method = RequestMethod.POST)
    public ResultMessageDTO<Boolean> fight(@RequestHeader("version") String version, @RequestBody FightRedpackDTO dto){
        try {
            return ResultMessageDTO.success(versionFactory.route(version).fight(dto));
        }
        catch (RedpackException e) {
            log.warn("抢红包异常，redpackId={}，userId={}，{}", dto.getActivityID(), dto.getUserID(), e);
            return ResultMessageDTO.fail(e.getMessage());
        }
    }



}
