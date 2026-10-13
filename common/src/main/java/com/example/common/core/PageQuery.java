package com.example.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String orderBy;

    private Boolean isAsc = false;

    public <T> IPage<T> toPage() {
        int current = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;

        if (size > 100) {
            size = 100;
        }

        Page<T> page = new Page<>(current, size);

        if (StringUtils.hasText(orderBy)) {
            String column = toUnderline(orderBy);
            page.addOrder(Boolean.TRUE.equals(isAsc) ? OrderItem.asc(column) : OrderItem.desc(column));
        }

        return page;
    }

    private String toUnderline(String param) {
        if (!StringUtils.hasText(param)) return "";
        return param.replaceAll("[A-Z]", "_$0").toLowerCase();
    }
}
