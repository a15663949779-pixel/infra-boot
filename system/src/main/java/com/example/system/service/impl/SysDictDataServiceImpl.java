package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.CacheConstants;
import com.example.system.domain.entity.SysDictData;
import com.example.system.mapper.SysDictDataMapper;
import com.example.system.service.SysDictDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<SysDictData> listPage(Page<SysDictData> page, String dictType, String dictLabel, Integer status) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType)
               .like(StringUtils.hasText(dictLabel), SysDictData::getDictLabel, dictLabel)
               .eq(status != null, SysDictData::getStatus, status)
               .orderByAsc(SysDictData::getDictSort);
        return this.page(page, wrapper);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        String cacheKey = CacheConstants.SYS_DICT_KEY + dictType;
        String cacheJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cacheJson)) {
            try {
                return objectMapper.readValue(cacheJson, new TypeReference<List<SysDictData>>() {});
            } catch (JsonProcessingException e) {
                log.warn("字典缓存反序列化失败，将重新查询数据库: {}", dictType, e);
            }
        }

        List<SysDictData> dbList = this.lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getDictSort)
                .list();

        if (!CollectionUtils.isEmpty(dbList)) {
            try {
                stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dbList));
            } catch (JsonProcessingException e) {
                log.warn("字典缓存序列化失败: {}", dictType, e);
            }
        }
        return dbList;
    }

    @Override
    public void clearDictCache(String dictType) {
        stringRedisTemplate.delete(CacheConstants.SYS_DICT_KEY + dictType);
    }

    @Override
    public void clearAllCache() {
        var keys = stringRedisTemplate.keys(CacheConstants.SYS_DICT_KEY + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            stringRedisTemplate.delete(keys);
        }
    }
}
