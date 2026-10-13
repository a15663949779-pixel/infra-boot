package com.example.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private Long total;

    private List<T> list;

    private Integer pageNum;

    private Integer pageSize;

    public static <T> PageResult<T> build(IPage<T> page) {
        if (page == null) {
            return new PageResult<>(0L, Collections.emptyList(), 1, 10);
        }
        return new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
    }
}
