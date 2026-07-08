package com.jiaoshoujia.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.system.domain.SysLoginInfor;

public interface ISysLoginInforService extends IService<SysLoginInfor> {

    void insertLoginInfor(SysLoginInfor loginInfor);

    /**
     * 记录一条登录日志（异步、失败不影响主流程）。
     */
    void recordLogin(String userName, String ipaddr, String userAgent, int status, String msg);

    Page<SysLoginInfor> selectLoginInforPage(Page<SysLoginInfor> page, SysLoginInfor loginInfor);

    List<SysLoginInfor> selectLoginInforList(SysLoginInfor loginInfor);

    int deleteLoginInforByIds(Long[] infoIds);

    void cleanLoginInfor();
}
