package com.bobo.redpacket.Verson;

import com.bobo.redpacket.service.RedpackService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 14:57 2021/9/20
 */

@Component
public class VersionFactory {

    @Resource(name = "redpackServiceImplV1")
    private RedpackService serviceV1;
    @Resource(name = "redpackServiceImplV2")
    private RedpackService serviceV2;
    @Resource(name = "redpackServiceImplV3")
    private RedpackService serviceV3;
    @Resource(name = "redpackServiceImplV4")
    private RedpackService serviceV4;
    @Resource(name = "redpackServiceImplV5")
    private RedpackService serviceV5;
    @Resource(name = "redpackServiceImplV6")
    private RedpackService serviceV6;

    public RedpackService route(String version) {
        switch (version) {
            case "v1":
                return serviceV1;
            case "v2":
                return serviceV2;
            case "v3":
                return serviceV3;
            case "v4":
                return serviceV4;
            case "v5":
                return serviceV5;
            case "v6":
                return serviceV6;
        }
        return null;
    }

}