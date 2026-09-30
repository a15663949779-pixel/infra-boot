package com.example.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {

    IPage<SysDictData> listPage(Page<SysDictData> page, String dictType, String dictLabel, Integer status);

    List<SysDictData> selectDictDataByType(String dictType);

    void clearDictCache(String dictType);

    void clearAllCache();
}
