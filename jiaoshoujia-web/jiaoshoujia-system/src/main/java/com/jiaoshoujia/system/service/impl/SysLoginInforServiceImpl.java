package com.jiaoshoujia.system.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysLoginInfor;
import com.jiaoshoujia.system.mapper.SysLoginInforMapper;
import com.jiaoshoujia.system.service.ISysLoginInforService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SysLoginInforServiceImpl extends ServiceImpl<SysLoginInforMapper, SysLoginInfor>
        implements ISysLoginInforService {

    private static final Logger log = LoggerFactory.getLogger(SysLoginInforServiceImpl.class);

    @Override
    public void insertLoginInfor(SysLoginInfor loginInfor) {
        save(loginInfor);
    }

    @Async
    @Override
    public void recordLogin(String userName, String ipaddr, String userAgent, int status, String msg) {
        try {
            SysLoginInfor infor = new SysLoginInfor();
            infor.setUserName(userName);
            infor.setIpaddr(ipaddr);
            infor.setStatus(status);
            infor.setMsg(msg);
            infor.setLoginTime(LocalDateTime.now());
            if (StringUtils.isNotEmpty(userAgent)) {
                UserAgent ua = UserAgentUtil.parse(userAgent);
                if (ua != null) {
                    infor.setBrowser(ua.getBrowser() != null ? ua.getBrowser().getName() : "");
                    infor.setOs(ua.getOs() != null ? ua.getOs().getName() : "");
                }
            }
            save(infor);
        } catch (Exception e) {
            log.warn("Failed to record login infor: {}", e.getMessage());
        }
    }

    @Override
    public Page<SysLoginInfor> selectLoginInforPage(Page<SysLoginInfor> page, SysLoginInfor loginInfor) {
        return baseMapper.selectPage(page, buildQueryWrapper(loginInfor));
    }

    @Override
    public List<SysLoginInfor> selectLoginInforList(SysLoginInfor loginInfor) {
        return baseMapper.selectList(buildQueryWrapper(loginInfor));
    }

    @Override
    public int deleteLoginInforByIds(Long[] infoIds) {
        removeBatchByIds(Arrays.asList(infoIds));
        return 1;
    }

    @Override
    public void cleanLoginInfor() {
        baseMapper.delete(null);
    }

    private LambdaQueryWrapper<SysLoginInfor> buildQueryWrapper(SysLoginInfor loginInfor) {
        LambdaQueryWrapper<SysLoginInfor> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(loginInfor.getUserName()), SysLoginInfor::getUserName, loginInfor.getUserName())
                .like(StringUtils.isNotEmpty(loginInfor.getIpaddr()), SysLoginInfor::getIpaddr, loginInfor.getIpaddr())
                .eq(loginInfor.getStatus() != null, SysLoginInfor::getStatus, loginInfor.getStatus())
                .orderByDesc(SysLoginInfor::getLoginTime);
        return wrapper;
    }
}
