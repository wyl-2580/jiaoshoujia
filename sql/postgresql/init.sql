-- =============================================
-- 脚手架系统 PostgreSQL 初始化脚本
-- =============================================

-- ----------------------------
-- 通用 update_time 触发器函数
-- ----------------------------
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) DEFAULT '',
    phone       VARCHAR(20)  DEFAULT '',
    sex         SMALLINT     DEFAULT 0,
    avatar      VARCHAR(200) DEFAULT '',
    password    VARCHAR(100) NOT NULL,
    status      SMALLINT     DEFAULT 0,
    dept_id     BIGINT       DEFAULT NULL,
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT uk_username UNIQUE (username)
);
COMMENT ON TABLE  sys_user              IS '用户表';
COMMENT ON COLUMN sys_user.id           IS '用户ID';
COMMENT ON COLUMN sys_user.username     IS '用户名';
COMMENT ON COLUMN sys_user.nickname     IS '昵称';
COMMENT ON COLUMN sys_user.email        IS '邮箱';
COMMENT ON COLUMN sys_user.phone        IS '手机号';
COMMENT ON COLUMN sys_user.sex          IS '性别(0男 1女 2未知)';
COMMENT ON COLUMN sys_user.avatar       IS '头像';
COMMENT ON COLUMN sys_user.password     IS '密码';
COMMENT ON COLUMN sys_user.status       IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_user.dept_id      IS '部门ID';
COMMENT ON COLUMN sys_user.del_flag     IS '删除标志(0存在 1删除)';
COMMENT ON COLUMN sys_user.create_by    IS '创建者';
COMMENT ON COLUMN sys_user.create_time  IS '创建时间';
COMMENT ON COLUMN sys_user.update_by    IS '更新者';
COMMENT ON COLUMN sys_user.update_time  IS '更新时间';
COMMENT ON COLUMN sys_user.remark       IS '备注';

CREATE TRIGGER trg_sys_user_update_time
    BEFORE UPDATE ON sys_user
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    role_key    VARCHAR(100) NOT NULL,
    role_sort   INT          DEFAULT 0,
    data_scope  VARCHAR(1)   DEFAULT '1',
    status      SMALLINT     DEFAULT 0,
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT uk_role_key UNIQUE (role_key)
);
COMMENT ON TABLE  sys_role              IS '角色表';
COMMENT ON COLUMN sys_role.id           IS '角色ID';
COMMENT ON COLUMN sys_role.role_name    IS '角色名称';
COMMENT ON COLUMN sys_role.role_key     IS '角色权限字符串';
COMMENT ON COLUMN sys_role.role_sort    IS '排序';
COMMENT ON COLUMN sys_role.data_scope   IS '数据范围(1全部 2自定义 3本部门 4本部门及以下 5仅本人)';
COMMENT ON COLUMN sys_role.status       IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_role.del_flag     IS '删除标志';
COMMENT ON COLUMN sys_role.create_by    IS '创建者';
COMMENT ON COLUMN sys_role.create_time  IS '创建时间';
COMMENT ON COLUMN sys_role.update_by    IS '更新者';
COMMENT ON COLUMN sys_role.update_time  IS '更新时间';
COMMENT ON COLUMN sys_role.remark       IS '备注';

CREATE TRIGGER trg_sys_role_update_time
    BEFORE UPDATE ON sys_role
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 3. 菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu CASCADE;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL,
    menu_name   VARCHAR(50)  NOT NULL,
    parent_id   BIGINT       DEFAULT 0,
    order_num   INT          DEFAULT 0,
    path        VARCHAR(200) DEFAULT '',
    component   VARCHAR(200) DEFAULT NULL,
    query_param VARCHAR(255) DEFAULT NULL,
    is_frame    SMALLINT     DEFAULT 1,
    is_cache    SMALLINT     DEFAULT 0,
    menu_type   CHAR(1)      DEFAULT '',
    visible     SMALLINT     DEFAULT 0,
    status      SMALLINT     DEFAULT 0,
    perms       VARCHAR(100) DEFAULT NULL,
    icon        VARCHAR(100) DEFAULT '#',
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_menu              IS '菜单权限表';
COMMENT ON COLUMN sys_menu.id           IS '菜单ID';
COMMENT ON COLUMN sys_menu.menu_name    IS '菜单名称';
COMMENT ON COLUMN sys_menu.parent_id    IS '父菜单ID';
COMMENT ON COLUMN sys_menu.order_num    IS '排序';
COMMENT ON COLUMN sys_menu.path         IS '路由地址';
COMMENT ON COLUMN sys_menu.component    IS '组件路径';
COMMENT ON COLUMN sys_menu.query_param  IS '路由参数';
COMMENT ON COLUMN sys_menu.is_frame     IS '是否外链(0是 1否)';
COMMENT ON COLUMN sys_menu.is_cache     IS '是否缓存(0缓存 1不缓存)';
COMMENT ON COLUMN sys_menu.menu_type    IS '菜单类型(M目录 C菜单 F按钮)';
COMMENT ON COLUMN sys_menu.visible      IS '显示状态(0显示 1隐藏)';
COMMENT ON COLUMN sys_menu.status       IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_menu.perms        IS '权限标识';
COMMENT ON COLUMN sys_menu.icon         IS '菜单图标';
COMMENT ON COLUMN sys_menu.del_flag     IS '删除标志';

CREATE TRIGGER trg_sys_menu_update_time
    BEFORE UPDATE ON sys_menu
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 4. 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept CASCADE;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL,
    parent_id   BIGINT       DEFAULT 0,
    ancestors   VARCHAR(500) DEFAULT '',
    dept_name   VARCHAR(50)  NOT NULL,
    order_num   INT          DEFAULT 0,
    leader      VARCHAR(50)  DEFAULT '',
    phone       VARCHAR(20)  DEFAULT '',
    email       VARCHAR(100) DEFAULT '',
    status      SMALLINT     DEFAULT 0,
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_dept              IS '部门表';
COMMENT ON COLUMN sys_dept.id           IS '部门ID';
COMMENT ON COLUMN sys_dept.parent_id    IS '父部门ID';
COMMENT ON COLUMN sys_dept.ancestors    IS '祖级列表';
COMMENT ON COLUMN sys_dept.dept_name    IS '部门名称';
COMMENT ON COLUMN sys_dept.order_num    IS '排序';
COMMENT ON COLUMN sys_dept.leader       IS '负责人';
COMMENT ON COLUMN sys_dept.phone        IS '电话';
COMMENT ON COLUMN sys_dept.email        IS '邮箱';
COMMENT ON COLUMN sys_dept.status       IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_dept.del_flag     IS '删除标志';

CREATE TRIGGER trg_sys_dept_update_time
    BEFORE UPDATE ON sys_dept
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 5. 用户与角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE  sys_user_role         IS '用户与角色关联表';
COMMENT ON COLUMN sys_user_role.user_id IS '用户ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';

-- ----------------------------
-- 6. 角色与菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu CASCADE;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
COMMENT ON TABLE  sys_role_menu         IS '角色与菜单关联表';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';

-- ----------------------------
-- 7. 角色与部门关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_dept CASCADE;
CREATE TABLE sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);
COMMENT ON TABLE  sys_role_dept         IS '角色与部门关联表';
COMMENT ON COLUMN sys_role_dept.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_dept.dept_id IS '部门ID';

-- ----------------------------
-- 8. 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type CASCADE;
CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL,
    dict_name   VARCHAR(100) NOT NULL,
    dict_type   VARCHAR(100) NOT NULL,
    status      SMALLINT     DEFAULT 0,
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT uk_dict_type UNIQUE (dict_type)
);
COMMENT ON TABLE  sys_dict_type             IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id          IS '字典ID';
COMMENT ON COLUMN sys_dict_type.dict_name   IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_type   IS '字典类型';
COMMENT ON COLUMN sys_dict_type.status      IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_dict_type.del_flag    IS '删除标志';

CREATE TRIGGER trg_sys_dict_type_update_time
    BEFORE UPDATE ON sys_dict_type
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 9. 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_data CASCADE;
CREATE TABLE sys_dict_data (
    id          BIGINT       NOT NULL,
    dict_sort   INT          DEFAULT 0,
    dict_label  VARCHAR(100) NOT NULL,
    dict_value  VARCHAR(100) NOT NULL,
    dict_type   VARCHAR(100) NOT NULL,
    css_class   VARCHAR(100) DEFAULT '',
    list_class  VARCHAR(100) DEFAULT 'default',
    is_default  SMALLINT     DEFAULT 0,
    status      SMALLINT     DEFAULT 0,
    del_flag    SMALLINT     DEFAULT 0,
    create_by   VARCHAR(50)  DEFAULT '',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by   VARCHAR(50)  DEFAULT '',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark      VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_dict_data              IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.id           IS '字典数据ID';
COMMENT ON COLUMN sys_dict_data.dict_sort    IS '排序';
COMMENT ON COLUMN sys_dict_data.dict_label   IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value   IS '字典键值';
COMMENT ON COLUMN sys_dict_data.dict_type    IS '字典类型';
COMMENT ON COLUMN sys_dict_data.css_class    IS '样式属性';
COMMENT ON COLUMN sys_dict_data.list_class   IS '表格回显样式';
COMMENT ON COLUMN sys_dict_data.is_default   IS '是否默认(0否 1是)';
COMMENT ON COLUMN sys_dict_data.status       IS '状态(0正常 1停用)';
COMMENT ON COLUMN sys_dict_data.del_flag     IS '删除标志';

CREATE TRIGGER trg_sys_dict_data_update_time
    BEFORE UPDATE ON sys_dict_data
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 10. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log CASCADE;
CREATE TABLE sys_oper_log (
    id             BIGSERIAL     NOT NULL,
    title          VARCHAR(50)   DEFAULT '',
    business_type  SMALLINT      DEFAULT 0,
    method         VARCHAR(200)  DEFAULT '',
    request_method VARCHAR(10)   DEFAULT '',
    operator_type  SMALLINT      DEFAULT 0,
    oper_name      VARCHAR(50)   DEFAULT '',
    dept_name      VARCHAR(50)   DEFAULT '',
    oper_url       VARCHAR(500)  DEFAULT '',
    oper_ip        VARCHAR(128)  DEFAULT '',
    oper_location  VARCHAR(255)  DEFAULT '',
    oper_param     TEXT,
    json_result    TEXT,
    status         SMALLINT      DEFAULT 0,
    error_msg      TEXT,
    oper_time      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    cost_time      BIGINT        DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_oper_log                 IS '操作日志表';
COMMENT ON COLUMN sys_oper_log.id              IS '日志ID';
COMMENT ON COLUMN sys_oper_log.title           IS '模块标题';
COMMENT ON COLUMN sys_oper_log.business_type   IS '业务类型(0其它 1新增 2修改 3删除)';
COMMENT ON COLUMN sys_oper_log.method          IS '方法名称';
COMMENT ON COLUMN sys_oper_log.request_method  IS '请求方式';
COMMENT ON COLUMN sys_oper_log.operator_type   IS '操作类别(0其它 1后台 2手机端)';
COMMENT ON COLUMN sys_oper_log.oper_name       IS '操作人员';
COMMENT ON COLUMN sys_oper_log.dept_name       IS '部门名称';
COMMENT ON COLUMN sys_oper_log.oper_url        IS '请求URL';
COMMENT ON COLUMN sys_oper_log.oper_ip         IS '操作IP';
COMMENT ON COLUMN sys_oper_log.oper_location   IS '操作地点';
COMMENT ON COLUMN sys_oper_log.oper_param      IS '请求参数';
COMMENT ON COLUMN sys_oper_log.json_result     IS '返回参数';
COMMENT ON COLUMN sys_oper_log.status          IS '状态(0正常 1异常)';
COMMENT ON COLUMN sys_oper_log.error_msg       IS '错误消息';
COMMENT ON COLUMN sys_oper_log.oper_time       IS '操作时间';
COMMENT ON COLUMN sys_oper_log.cost_time       IS '消耗时间(毫秒)';

CREATE INDEX idx_oper_log_oper_time ON sys_oper_log (oper_time);

-- ----------------------------
-- 10.1 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_login_infor CASCADE;
CREATE TABLE sys_login_infor (
    id             BIGSERIAL     NOT NULL,
    user_name      VARCHAR(50)   DEFAULT '',
    ipaddr         VARCHAR(128)  DEFAULT '',
    login_location VARCHAR(255)  DEFAULT '',
    browser        VARCHAR(50)   DEFAULT '',
    os             VARCHAR(50)   DEFAULT '',
    status         SMALLINT      DEFAULT 0,
    msg            VARCHAR(255)  DEFAULT '',
    login_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_login_infor                IS '登录日志表';
COMMENT ON COLUMN sys_login_infor.id             IS '访问ID';
COMMENT ON COLUMN sys_login_infor.user_name      IS '用户账号';
COMMENT ON COLUMN sys_login_infor.ipaddr         IS '登录IP地址';
COMMENT ON COLUMN sys_login_infor.login_location IS '登录地点';
COMMENT ON COLUMN sys_login_infor.browser        IS '浏览器类型';
COMMENT ON COLUMN sys_login_infor.os             IS '操作系统';
COMMENT ON COLUMN sys_login_infor.status         IS '登录状态(0成功 1失败)';
COMMENT ON COLUMN sys_login_infor.msg            IS '提示消息';
COMMENT ON COLUMN sys_login_infor.login_time     IS '登录时间';

CREATE INDEX idx_login_infor_login_time ON sys_login_infor (login_time);

-- ----------------------------
-- 11. 定时任务表
-- ----------------------------
DROP TABLE IF EXISTS sys_job CASCADE;
CREATE TABLE sys_job (
    id              BIGINT       NOT NULL,
    job_name        VARCHAR(64)  NOT NULL,
    job_group       VARCHAR(64)  DEFAULT 'DEFAULT',
    invoke_target   VARCHAR(500) NOT NULL,
    cron_expression VARCHAR(255) DEFAULT '',
    misfire_policy  SMALLINT     DEFAULT 3,
    concurrent      SMALLINT     DEFAULT 1,
    status          SMALLINT     DEFAULT 0,
    del_flag        SMALLINT     DEFAULT 0,
    create_by       VARCHAR(50)  DEFAULT '',
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(50)  DEFAULT '',
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(500) DEFAULT '',
    PRIMARY KEY (id)
);
COMMENT ON TABLE  sys_job                   IS '定时任务表';
COMMENT ON COLUMN sys_job.id                IS '任务ID';
COMMENT ON COLUMN sys_job.job_name          IS '任务名称';
COMMENT ON COLUMN sys_job.job_group         IS '任务组名';
COMMENT ON COLUMN sys_job.invoke_target     IS '调用目标字符串';
COMMENT ON COLUMN sys_job.cron_expression   IS 'cron执行表达式';
COMMENT ON COLUMN sys_job.misfire_policy    IS '计划执行错误策略(1立即执行 2执行一次 3放弃执行)';
COMMENT ON COLUMN sys_job.concurrent        IS '是否并发执行(0允许 1禁止)';
COMMENT ON COLUMN sys_job.status            IS '状态(0正常 1暂停)';
COMMENT ON COLUMN sys_job.del_flag          IS '删除标志';

CREATE TRIGGER trg_sys_job_update_time
    BEFORE UPDATE ON sys_job
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 12. 代码生成 - 表信息
-- ----------------------------
DROP TABLE IF EXISTS gen_table CASCADE;
CREATE TABLE gen_table (
    id              BIGSERIAL     NOT NULL,
    table_name      VARCHAR(200)  NOT NULL,
    table_comment   VARCHAR(500)  DEFAULT '',
    sub_table_name  VARCHAR(200)  DEFAULT NULL,
    sub_table_fk    VARCHAR(200)  DEFAULT NULL,
    class_name      VARCHAR(200)  NOT NULL,
    tpl_category    VARCHAR(200)  DEFAULT 'crud',
    package_name    VARCHAR(200)  DEFAULT '',
    module_name     VARCHAR(50)   DEFAULT '',
    business_name   VARCHAR(50)   DEFAULT '',
    function_name   VARCHAR(50)   DEFAULT '',
    function_author VARCHAR(50)   DEFAULT '',
    gen_type        SMALLINT      DEFAULT 0,
    gen_path        VARCHAR(200)  DEFAULT '/',
    options         VARCHAR(1000) DEFAULT NULL,
    del_flag        SMALLINT      DEFAULT 0,
    create_by       VARCHAR(50)   DEFAULT '',
    create_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_by       VARCHAR(50)   DEFAULT '',
    update_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(500)  DEFAULT '',
    PRIMARY KEY (id)
);
COMMENT ON TABLE  gen_table                  IS '代码生成业务表';
COMMENT ON COLUMN gen_table.id               IS '编号';
COMMENT ON COLUMN gen_table.table_name       IS '表名称';
COMMENT ON COLUMN gen_table.table_comment    IS '表描述';
COMMENT ON COLUMN gen_table.sub_table_name   IS '关联子表名';
COMMENT ON COLUMN gen_table.sub_table_fk     IS '子表关联外键';
COMMENT ON COLUMN gen_table.class_name       IS '实体类名称';
COMMENT ON COLUMN gen_table.tpl_category     IS '模板类型(crud单表 tree树表 sub主子表)';
COMMENT ON COLUMN gen_table.package_name     IS '生成包路径';
COMMENT ON COLUMN gen_table.module_name      IS '生成模块名';
COMMENT ON COLUMN gen_table.business_name    IS '生成业务名';
COMMENT ON COLUMN gen_table.function_name    IS '生成功能名';
COMMENT ON COLUMN gen_table.function_author  IS '生成功能作者';
COMMENT ON COLUMN gen_table.gen_type         IS '生成代码方式(0zip压缩包 1自定义路径)';
COMMENT ON COLUMN gen_table.gen_path         IS '生成路径';
COMMENT ON COLUMN gen_table.options          IS '其它生成选项';
COMMENT ON COLUMN gen_table.del_flag         IS '删除标志';

CREATE TRIGGER trg_gen_table_update_time
    BEFORE UPDATE ON gen_table
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 13. 代码生成 - 字段信息
-- ----------------------------
DROP TABLE IF EXISTS gen_table_column CASCADE;
CREATE TABLE gen_table_column (
    id             BIGSERIAL     NOT NULL,
    table_id       BIGINT        NOT NULL,
    column_name    VARCHAR(200)  NOT NULL,
    column_comment VARCHAR(500)  DEFAULT '',
    column_type    VARCHAR(100)  NOT NULL,
    java_type      VARCHAR(100)  DEFAULT '',
    java_field     VARCHAR(200)  DEFAULT '',
    is_pk          SMALLINT      DEFAULT 0,
    is_increment   SMALLINT      DEFAULT 0,
    is_required    SMALLINT      DEFAULT 0,
    is_insert      SMALLINT      DEFAULT 0,
    is_edit        SMALLINT      DEFAULT 0,
    is_list        SMALLINT      DEFAULT 0,
    is_query       SMALLINT      DEFAULT 0,
    query_type     VARCHAR(200)  DEFAULT 'EQ',
    html_type      VARCHAR(200)  DEFAULT '',
    dict_type      VARCHAR(200)  DEFAULT '',
    sort           INT           DEFAULT 0,
    del_flag       SMALLINT      DEFAULT 0,
    create_by      VARCHAR(50)   DEFAULT '',
    create_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_by      VARCHAR(50)   DEFAULT '',
    update_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
COMMENT ON TABLE  gen_table_column                IS '代码生成字段表';
COMMENT ON COLUMN gen_table_column.id             IS '编号';
COMMENT ON COLUMN gen_table_column.table_id       IS '归属表编号';
COMMENT ON COLUMN gen_table_column.column_name    IS '列名称';
COMMENT ON COLUMN gen_table_column.column_comment IS '列描述';
COMMENT ON COLUMN gen_table_column.column_type    IS '列类型';
COMMENT ON COLUMN gen_table_column.java_type      IS 'Java类型';
COMMENT ON COLUMN gen_table_column.java_field     IS 'Java字段名';
COMMENT ON COLUMN gen_table_column.is_pk          IS '是否主键(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_increment   IS '是否自增(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_required    IS '是否必填(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_insert      IS '是否为插入字段(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_edit        IS '是否编辑字段(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_list        IS '是否列表字段(0否 1是)';
COMMENT ON COLUMN gen_table_column.is_query       IS '是否查询字段(0否 1是)';
COMMENT ON COLUMN gen_table_column.query_type     IS '查询方式';
COMMENT ON COLUMN gen_table_column.html_type      IS '显示类型';
COMMENT ON COLUMN gen_table_column.dict_type      IS '字典类型';
COMMENT ON COLUMN gen_table_column.sort           IS '排序';
COMMENT ON COLUMN gen_table_column.del_flag       IS '删除标志';

CREATE INDEX idx_gen_table_column_table_id ON gen_table_column (table_id);

CREATE TRIGGER trg_gen_table_column_update_time
    BEFORE UPDATE ON gen_table_column
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- =============================================
-- 初始数据
-- =============================================

-- ----------------------------
-- 部门数据
-- ----------------------------
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, order_num, leader, phone, email, create_by) VALUES
(100, 0, '0',     '总公司', 0, '管理员', '15888888888', 'admin@example.com', 'admin'),
(101, 100, '0,100', '技术部', 1, '',      '',            '',                  'admin'),
(102, 100, '0,100', '产品部', 2, '',      '',            '',                  'admin'),
(103, 100, '0,100', '市场部', 3, '',      '',            '',                  'admin');

-- ----------------------------
-- 用户数据  密码: admin123 => BCrypt
-- ----------------------------
INSERT INTO sys_user (id, username, nickname, password, status, dept_id, remark, create_by) VALUES
(1, 'admin',      '系统管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 100, '三员之系统管理员：负责用户/部门/字典/任务/代码生成', 'admin'),
(2, 'user',       '普通用户',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 101, '演示普通用户',                                     'admin'),
(3, 'secadmin',   '安全管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 100, '三员之安全(配置)管理员：负责角色/菜单授权配置',    'admin'),
(4, 'auditadmin', '审计管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 100, '三员之审计管理员：负责操作日志/登录日志审计',      'admin');

-- ----------------------------
-- 角色数据（三权分立 / 等保三员：系统管理员、安全管理员、审计管理员职责互斥）
-- ----------------------------
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, remark, create_by) VALUES
(1, '系统管理员', 'sysadmin',   1, '1', '系统运维与账号管理，不含授权配置与审计', 'admin'),
(2, '普通角色',   'common',     2, '2', '演示普通角色',                           'admin'),
(3, '安全管理员', 'secadmin',   3, '1', '角色与菜单授权配置，不含业务与审计',     'admin'),
(4, '审计管理员', 'auditadmin', 4, '1', '操作日志与登录日志审计，不含业务与授权', 'admin');

-- ----------------------------
-- 用户与角色关联（每员一角，互斥）
-- ----------------------------
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- ----------------------------
-- 菜单数据
-- ----------------------------
-- 一级目录
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1, '系统管理', 0, 1, 'system',  NULL, 'M', 0, 0, '', 'system',  'admin'),
(2, '系统监控', 0, 2, 'monitor', NULL, 'M', 0, 0, '', 'monitor', 'admin'),
(3, '系统工具', 0, 3, 'tool',    NULL, 'M', 0, 0, '', 'tool',    'admin');

-- 二级菜单 — 系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(100, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 0, 0, 'system:user:list', 'user',   'admin'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 0, 0, 'system:role:list', 'peoples','admin'),
(102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 0, 0, 'system:menu:list', 'tree-table', 'admin'),
(103, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', 0, 0, 'system:dept:list', 'tree',   'admin'),
(104, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', 0, 0, 'system:dict:list', 'dict',   'admin');

-- 二级菜单 — 系统监控
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(105, '操作日志', 2, 1, 'operlog',    'monitor/operlog/index',    'C', 0, 0, 'monitor:operlog:list',    'form',  'admin'),
(106, '定时任务', 2, 3, 'job',        'monitor/job/index',        'C', 0, 0, 'monitor:job:list',        'job',   'admin'),
(108, '登录日志', 2, 2, 'logininfor', 'monitor/logininfor/index', 'C', 0, 0, 'monitor:logininfor:list', 'logininfor', 'admin');

-- 二级菜单 — 系统工具
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(107, '代码生成', 3, 1, 'gen', 'tool/gen/index', 'C', 0, 0, 'tool:gen:list', 'code', 'admin');

-- 用户管理按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1001, '用户查询', 100, 1, '', NULL, 'F', 0, 0, 'system:user:query',          '#', 'admin'),
(1002, '用户新增', 100, 2, '', NULL, 'F', 0, 0, 'system:user:add',            '#', 'admin'),
(1003, '用户修改', 100, 3, '', NULL, 'F', 0, 0, 'system:user:edit',           '#', 'admin'),
(1004, '用户删除', 100, 4, '', NULL, 'F', 0, 0, 'system:user:remove',         '#', 'admin'),
(1005, '用户导出', 100, 5, '', NULL, 'F', 0, 0, 'system:user:export',         '#', 'admin'),
(1006, '用户导入', 100, 6, '', NULL, 'F', 0, 0, 'system:user:import',         '#', 'admin'),
(1007, '重置密码', 100, 7, '', NULL, 'F', 0, 0, 'system:user:resetPwd',       '#', 'admin');

-- 角色管理按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1008, '角色查询', 101, 1, '', NULL, 'F', 0, 0, 'system:role:query',          '#', 'admin'),
(1009, '角色新增', 101, 2, '', NULL, 'F', 0, 0, 'system:role:add',            '#', 'admin'),
(1010, '角色修改', 101, 3, '', NULL, 'F', 0, 0, 'system:role:edit',           '#', 'admin'),
(1011, '角色删除', 101, 4, '', NULL, 'F', 0, 0, 'system:role:remove',         '#', 'admin'),
(1012, '角色导出', 101, 5, '', NULL, 'F', 0, 0, 'system:role:export',         '#', 'admin');

-- 菜单管理按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1013, '菜单查询', 102, 1, '', NULL, 'F', 0, 0, 'system:menu:query',          '#', 'admin'),
(1014, '菜单新增', 102, 2, '', NULL, 'F', 0, 0, 'system:menu:add',            '#', 'admin'),
(1015, '菜单修改', 102, 3, '', NULL, 'F', 0, 0, 'system:menu:edit',           '#', 'admin'),
(1016, '菜单删除', 102, 4, '', NULL, 'F', 0, 0, 'system:menu:remove',         '#', 'admin');

-- 部门管理按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1017, '部门查询', 103, 1, '', NULL, 'F', 0, 0, 'system:dept:query',          '#', 'admin'),
(1018, '部门新增', 103, 2, '', NULL, 'F', 0, 0, 'system:dept:add',            '#', 'admin'),
(1019, '部门修改', 103, 3, '', NULL, 'F', 0, 0, 'system:dept:edit',           '#', 'admin'),
(1020, '部门删除', 103, 4, '', NULL, 'F', 0, 0, 'system:dept:remove',         '#', 'admin');

-- 字典管理按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1021, '字典查询', 104, 1, '', NULL, 'F', 0, 0, 'system:dict:query',          '#', 'admin'),
(1022, '字典新增', 104, 2, '', NULL, 'F', 0, 0, 'system:dict:add',            '#', 'admin'),
(1023, '字典修改', 104, 3, '', NULL, 'F', 0, 0, 'system:dict:edit',           '#', 'admin'),
(1024, '字典删除', 104, 4, '', NULL, 'F', 0, 0, 'system:dict:remove',         '#', 'admin'),
(1025, '字典导出', 104, 5, '', NULL, 'F', 0, 0, 'system:dict:export',         '#', 'admin');

-- 操作日志按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1026, '操作查询', 105, 1, '', NULL, 'F', 0, 0, 'monitor:operlog:query',      '#', 'admin'),
(1027, '操作删除', 105, 2, '', NULL, 'F', 0, 0, 'monitor:operlog:remove',     '#', 'admin'),
(1028, '日志清空', 105, 3, '', NULL, 'F', 0, 0, 'monitor:operlog:clear',      '#', 'admin'),
(1039, '日志导出', 105, 4, '', NULL, 'F', 0, 0, 'monitor:operlog:export',     '#', 'admin');

-- 登录日志按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1040, '登录查询', 108, 1, '', NULL, 'F', 0, 0, 'monitor:logininfor:query',   '#', 'admin'),
(1041, '登录删除', 108, 2, '', NULL, 'F', 0, 0, 'monitor:logininfor:remove',  '#', 'admin'),
(1042, '日志清空', 108, 3, '', NULL, 'F', 0, 0, 'monitor:logininfor:clear',   '#', 'admin'),
(1043, '日志导出', 108, 4, '', NULL, 'F', 0, 0, 'monitor:logininfor:export',  '#', 'admin');

-- 定时任务按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1029, '任务查询', 106, 1, '', NULL, 'F', 0, 0, 'monitor:job:query',          '#', 'admin'),
(1030, '任务新增', 106, 2, '', NULL, 'F', 0, 0, 'monitor:job:add',            '#', 'admin'),
(1031, '任务修改', 106, 3, '', NULL, 'F', 0, 0, 'monitor:job:edit',           '#', 'admin'),
(1032, '任务删除', 106, 4, '', NULL, 'F', 0, 0, 'monitor:job:remove',         '#', 'admin'),
(1033, '任务状态', 106, 5, '', NULL, 'F', 0, 0, 'monitor:job:changeStatus',   '#', 'admin'),
(1044, '任务执行', 106, 6, '', NULL, 'F', 0, 0, 'monitor:job:run',            '#', 'admin');

-- 代码生成按钮
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(1034, '生成查询', 107, 1, '', NULL, 'F', 0, 0, 'tool:gen:query',             '#', 'admin'),
(1035, '生成修改', 107, 2, '', NULL, 'F', 0, 0, 'tool:gen:edit',              '#', 'admin'),
(1036, '生成删除', 107, 3, '', NULL, 'F', 0, 0, 'tool:gen:remove',            '#', 'admin'),
(1037, '导入代码', 107, 4, '', NULL, 'F', 0, 0, 'tool:gen:import',            '#', 'admin'),
(1038, '预览代码', 107, 5, '', NULL, 'F', 0, 0, 'tool:gen:preview',           '#', 'admin'),
(1045, '生成代码', 107, 6, '', NULL, 'F', 0, 0, 'tool:gen:code',              '#', 'admin');

-- ----------------------------
-- 角色与菜单关联 — 三权分立职责拆分
-- ----------------------------
-- 系统管理员(1)：用户/部门/字典 + 定时任务 + 代码生成（不含角色/菜单授权，不含日志审计）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),(1,3),
(1,100),(1,103),(1,104),(1,106),(1,107),
(1,1001),(1,1002),(1,1003),(1,1004),(1,1005),(1,1006),(1,1007),
(1,1017),(1,1018),(1,1019),(1,1020),
(1,1021),(1,1022),(1,1023),(1,1024),(1,1025),
(1,1029),(1,1030),(1,1031),(1,1032),(1,1033),(1,1044),
(1,1034),(1,1035),(1,1036),(1,1037),(1,1038),(1,1045);

-- 安全管理员(3)：角色管理 + 菜单管理（授权配置）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3,1),
(3,101),(3,102),
(3,1008),(3,1009),(3,1010),(3,1011),(3,1012),
(3,1013),(3,1014),(3,1015),(3,1016);

-- 审计管理员(4)：操作日志 + 登录日志（审计，独占，其它管理员无法查看/删除）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4,2),
(4,105),(4,108),
(4,1026),(4,1027),(4,1028),(4,1039),
(4,1040),(4,1041),(4,1042),(4,1043);

-- ----------------------------
-- 字典类型
-- ----------------------------
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, create_by) VALUES
(1, '用户性别', 'sys_user_sex',       0, 'admin'),
(2, '系统状态', 'sys_normal_disable', 0, 'admin'),
(3, '任务状态', 'sys_job_status',     0, 'admin'),
(4, '任务分组', 'sys_job_group',      0, 'admin'),
(5, '系统是否', 'sys_yes_no',         0, 'admin');

-- ----------------------------
-- 字典数据
-- ----------------------------
-- 用户性别
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by) VALUES
(1,  1, '男',   '0', 'sys_user_sex', '', 'default', 1, 0, 'admin'),
(2,  2, '女',   '1', 'sys_user_sex', '', 'default', 0, 0, 'admin'),
(3,  3, '未知', '2', 'sys_user_sex', '', 'default', 0, 0, 'admin');

-- 系统状态
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by) VALUES
(4,  1, '正常', '0', 'sys_normal_disable', '', 'primary', 1, 0, 'admin'),
(5,  2, '停用', '1', 'sys_normal_disable', '', 'danger',  0, 0, 'admin');

-- 任务状态
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by) VALUES
(6,  1, '正常', '0', 'sys_job_status', '', 'primary', 1, 0, 'admin'),
(7,  2, '暂停', '1', 'sys_job_status', '', 'danger',  0, 0, 'admin');

-- 任务分组
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by) VALUES
(8,  1, '默认', 'DEFAULT', 'sys_job_group', '', 'default', 1, 0, 'admin'),
(9,  2, '系统', 'SYSTEM',  'sys_job_group', '', 'default', 0, 0, 'admin');

-- 系统是否
INSERT INTO sys_dict_data (id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by) VALUES
(10, 1, '是', '1', 'sys_yes_no', '', 'primary', 1, 0, 'admin'),
(11, 2, '否', '0', 'sys_yes_no', '', 'danger',  0, 0, 'admin');
