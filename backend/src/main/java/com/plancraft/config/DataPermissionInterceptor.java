package com.plancraft.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.plancraft.module.user.service.UserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * 查询 plan / result 表时，自动拼接 WHERE user_id IN (下属集合)
 * - LEADER：只能看到下属的数据
 * - EMPLOYEE：只能看到自己的数据
 */
@Component
public class DataPermissionInterceptor implements InnerInterceptor {

    @Lazy
    @Autowired
    private UserRelationService userRelationService;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler,
                            BoundSql boundSql) throws SQLException {
        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Long currentUserId)) {
            return;
        }
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        String sql = boundSql.getSql();

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select select)) {
                return;
            }
            if (!(select.getSelectBody() instanceof PlainSelect plainSelect)) {
                return;
            }

            // 只拦截 plan 和 result 表的查询
            if (!isTargetTable(plainSelect)) {
                return;
            }

            // 构建数据权限条件
            String condition = buildCondition(role, currentUserId);
            if (condition == null) {
                return;
            }

            // 追加 WHERE 条件
            var parsedCondition = CCJSqlParserUtil.parseCondExpression(condition);
            if (plainSelect.getWhere() == null) {
                plainSelect.setWhere(parsedCondition);
            } else {
                plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), parsedCondition));
            }

            // 回写修改后的 SQL
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", select.toString());

        } catch (Exception e) {
            // SQL 解析失败时不做拦截，避免影响正常业务
        }
    }

    /**
     * 判断查询目标是否为 plan 或 result 表
     */
    private boolean isTargetTable(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() instanceof Table table) {
            String name = table.getName().toLowerCase();
            return "plan".equals(name) || "result".equals(name);
        }
        return false;
    }

    /**
     * 根据角色构建权限条件
     * @return 条件表达式字符串，null 表示不拦截
     */
    private String buildCondition(String role, Long currentUserId) {
        if ("ROLE_LEADER".equals(role)) {
            List<Long> subordinateIds = userRelationService.getSubordinateIds(currentUserId);
            if (subordinateIds.isEmpty()) {
                // 没有下属 → 查不到任何数据
                return "1 = 0";
            }
            String ids = subordinateIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return "user_id IN (" + ids + ")";
        } else if ("ROLE_EMPLOYEE".equals(role)) {
            return "user_id = " + currentUserId;
        }
        // 其他角色不拦截
        return null;
    }
}
