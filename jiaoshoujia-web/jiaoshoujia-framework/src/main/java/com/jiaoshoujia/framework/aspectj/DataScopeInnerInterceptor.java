package com.jiaoshoujia.framework.aspectj;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.jiaoshoujia.common.utils.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

public class DataScopeInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String dataScope = DataScopeContextHolder.get();
        if (StringUtils.isEmpty(dataScope)) {
            return;
        }
        DataScopeContextHolder.clear();
        String originalSql = boundSql.getSql();
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof PlainSelect plainSelect) {
                Expression where = plainSelect.getWhere();
                Expression scopeExpression = CCJSqlParserUtil.parseCondExpression(dataScope);
                if (where == null) {
                    plainSelect.setWhere(scopeExpression);
                } else {
                    plainSelect.setWhere(new AndExpression(where, scopeExpression));
                }
                PluginUtils.mpBoundSql(boundSql).sql(plainSelect.toString());
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DataScopeInnerInterceptor.class)
                    .warn("数据权限SQL解析失败，跳过过滤: {}", originalSql, e);
        }
    }
}
