package com.jiaoshoujia.framework.aspectj;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.jiaoshoujia.common.utils.StringUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
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
        String originalSql = boundSql.getSql();
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select select) {
                if (select.getSelectBody() instanceof PlainSelect plainSelect) {
                    Expression where = plainSelect.getWhere();
                    Expression scopeExpression = CCJSqlParserUtil.parseCondExpression(dataScope);
                    if (where == null) {
                        plainSelect.setWhere(scopeExpression);
                    } else {
                        plainSelect.setWhere(new AndExpression(where, scopeExpression));
                    }
                    PluginUtils.mpBoundSql(boundSql).sql(select.toString());
                }
            }
        } catch (Exception e) {
            String newSql = "SELECT * FROM (" + originalSql + ") _t WHERE " + dataScope;
            PluginUtils.mpBoundSql(boundSql).sql(newSql);
        }
    }
}
