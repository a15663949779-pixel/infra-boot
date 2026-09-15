package com.example.framework.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        }),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                org.apache.ibatis.cache.CacheKey.class, BoundSql.class
        })
})
public class SqlExecutionLogInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        BoundSql boundSql = resolveBoundSql(invocation, mappedStatement, parameter);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
        String params = resolveParams(boundSql, parameter);

        try {
            Object result = invocation.proceed();
            long cost = System.currentTimeMillis() - start;
            System.out.printf("""

                    [SQL执行监听]
                    Mapper : %s
                    SQL    : %s
                    Params : %s
                    Time   : %d ms
                    Status : SUCCESS
                    """, mappedStatement.getId(), sql, params, cost);
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            System.out.printf("""

                    [SQL执行监听]
                    Mapper : %s
                    SQL    : %s
                    Params : %s
                    Time   : %d ms
                    Status : FAILED - %s
                    """, mappedStatement.getId(), sql, params, cost, getRootMessage(ex));
            throw ex;
        }
    }

    private BoundSql resolveBoundSql(Invocation invocation, MappedStatement mappedStatement, Object parameter) {
        Object[] args = invocation.getArgs();
        if (args.length >= 6 && args[5] instanceof BoundSql boundSql) {
            return boundSql;
        }
        return mappedStatement.getBoundSql(parameter);
    }

    private String resolveParams(BoundSql boundSql, Object parameter) {
        List<ParameterMapping> mappings = boundSql.getParameterMappings();
        if (mappings == null || mappings.isEmpty()) {
            return "[]";
        }
        MetaObject metaObject = parameter == null ? null : SystemMetaObject.forObject(parameter);
        return mappings.stream()
                .map(mapping -> mapping.getProperty() + "=" + resolveValue(boundSql, metaObject, mapping.getProperty()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private Object resolveValue(BoundSql boundSql, MetaObject metaObject, String property) {
        if (boundSql.hasAdditionalParameter(property)) {
            return boundSql.getAdditionalParameter(property);
        }
        if (metaObject != null && metaObject.hasGetter(property)) {
            return metaObject.getValue(property);
        }
        return null;
    }

    private String getRootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
