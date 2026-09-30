package com.example.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.domain.entity.SysDictType;

public interface SysDictTypeService extends IService<SysDictType> {

    IPage<SysDictType> listPage(Page<SysDictType> page, String dictName, String dictType, Integer status);
}
