-- =============================================
-- 脚手架系统 MySQL 初始化脚本
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL                              COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL                              COMMENT '用户名',
    nickname    VARCHAR(50)  NOT NULL                              COMMENT '昵称',
    email       VARCHAR(100) DEFAULT ''                            COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT ''                            COMMENT '手机号',
    sex         TINYINT      DEFAULT 0                             COMMENT '性别(0男 1女 2未知)',
    avatar      VARCHAR(200) DEFAULT ''                            COMMENT '头像',
    password    VARCHAR(100) NOT NULL                              COMMENT '密码',
    status      TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    dept_id     BIGINT       DEFAULT NULL                          COMMENT '部门ID',
    del_flag    TINYINT      DEFAULT 0                             COMMENT '删除标志(0存在 1删除)',
    create_by   VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL                              COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL                              COMMENT '角色名称',
    role_key    VARCHAR(100) NOT NULL                              COMMENT '角色权限字符串',
    role_sort   INT          DEFAULT 0                             COMMENT '排序',
    data_scope  VARCHAR(1)   DEFAULT '1'                           COMMENT '数据范围(1全部 2自定义 3本部门 4本部门及以下 5仅本人)',
    status      TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    del_flag    TINYINT      DEFAULT 0                             COMMENT '删除标志',
    create_by   VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL                              COMMENT '菜单ID',
    menu_name   VARCHAR(50)  NOT NULL                              COMMENT '菜单名称',
    parent_id   BIGINT       DEFAULT 0                             COMMENT '父菜单ID',
    order_num   INT          DEFAULT 0                             COMMENT '排序',
    path        VARCHAR(200) DEFAULT ''                            COMMENT '路由地址',
    component   VARCHAR(200) DEFAULT NULL                          COMMENT '组件路径',
    query_param VARCHAR(255) DEFAULT NULL                          COMMENT '路由参数',
    is_frame    TINYINT      DEFAULT 1                             COMMENT '是否外链(0是 1否)',
    is_cache    TINYINT      DEFAULT 0                             COMMENT '是否缓存(0缓存 1不缓存)',
    menu_type   CHAR(1)      DEFAULT ''                            COMMENT '菜单类型(M目录 C菜单 F按钮)',
    visible     TINYINT      DEFAULT 0                             COMMENT '显示状态(0显示 1隐藏)',
    status      TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    perms       VARCHAR(100) DEFAULT NULL                          COMMENT '权限标识',
    icon        VARCHAR(100) DEFAULT '#'                           COMMENT '菜单图标',
    del_flag    TINYINT      DEFAULT 0                             COMMENT '删除标志',
    create_by   VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- 4. 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL                              COMMENT '部门ID',
    parent_id   BIGINT       DEFAULT 0                             COMMENT '父部门ID',
    ancestors   VARCHAR(500) DEFAULT ''                            COMMENT '祖级列表',
    dept_name   VARCHAR(50)  NOT NULL                              COMMENT '部门名称',
    order_num   INT          DEFAULT 0                             COMMENT '排序',
    leader      VARCHAR(50)  DEFAULT ''                            COMMENT '负责人',
    phone       VARCHAR(20)  DEFAULT ''                            COMMENT '电话',
    email       VARCHAR(100) DEFAULT ''                            COMMENT '邮箱',
    status      TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    del_flag    TINYINT      DEFAULT 0                             COMMENT '删除标志',
    create_by   VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 5. 用户与角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与角色关联表';

-- ----------------------------
-- 6. 角色与菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与菜单关联表';

-- ----------------------------
-- 7. 角色与部门关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_dept;
CREATE TABLE sys_role_dept (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色与部门关联表';

-- ----------------------------
-- 8. 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL                              COMMENT '字典ID',
    dict_name   VARCHAR(100) NOT NULL                              COMMENT '字典名称',
    dict_type   VARCHAR(100) NOT NULL                              COMMENT '字典类型',
    status      TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    del_flag    TINYINT      DEFAULT 0                             COMMENT '删除标志',
    create_by   VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by   VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- 9. 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
    id         BIGINT       NOT NULL                              COMMENT '字典数据ID',
    dict_sort  INT          DEFAULT 0                             COMMENT '排序',
    dict_label VARCHAR(100) NOT NULL                              COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL                              COMMENT '字典键值',
    dict_type  VARCHAR(100) NOT NULL                              COMMENT '字典类型',
    css_class  VARCHAR(100) DEFAULT ''                            COMMENT '样式属性',
    list_class VARCHAR(100) DEFAULT 'default'                     COMMENT '表格回显样式',
    is_default TINYINT      DEFAULT 0                             COMMENT '是否默认(0否 1是)',
    status     TINYINT      DEFAULT 0                             COMMENT '状态(0正常 1停用)',
    del_flag   TINYINT      DEFAULT 0                             COMMENT '删除标志',
    create_by  VARCHAR(50)  DEFAULT ''                            COMMENT '创建者',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
    update_by  VARCHAR(50)  DEFAULT ''                            COMMENT '更新者',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark     VARCHAR(500) DEFAULT ''                            COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- 10. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id             BIGINT        NOT NULL AUTO_INCREMENT           COMMENT '日志ID',
    title          VARCHAR(50)   DEFAULT ''                        COMMENT '模块标题',
    business_type  TINYINT       DEFAULT 0                         COMMENT '业务类型(0其它 1新增 2修改 3删除)',
    method         VARCHAR(200)  DEFAULT ''                        COMMENT '方法名称',
    request_method VARCHAR(10)   DEFAULT ''                        COMMENT '请求方式',
    operator_type  TINYINT       DEFAULT 0                         COMMENT '操作类别(0其它 1后台 2手机端)',
    oper_name      VARCHAR(50)   DEFAULT ''                        COMMENT '操作人员',
    dept_name      VARCHAR(50)   DEFAULT ''                        COMMENT '部门名称',
    oper_url       VARCHAR(500)  DEFAULT ''                        COMMENT '请求URL',
    oper_ip        VARCHAR(128)  DEFAULT ''                        COMMENT '操作IP',
    oper_location  VARCHAR(255)  DEFAULT ''                        COMMENT '操作地点',
    oper_param     TEXT                                            COMMENT '请求参数',
    json_result    TEXT                                            COMMENT '返回参数',
    status         TINYINT       DEFAULT 0                         COMMENT '状态(0正常 1异常)',
    error_msg      TEXT                                            COMMENT '错误消息',
    oper_time      DATETIME      DEFAULT CURRENT_TIMESTAMP         COMMENT '操作时间',
    cost_time      BIGINT        DEFAULT 0                         COMMENT '消耗时间(毫秒)',
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 10.1 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_login_infor;
CREATE TABLE sys_login_infor (
    id             BIGINT        NOT NULL AUTO_INCREMENT           COMMENT '访问ID',
    user_name      VARCHAR(50)   DEFAULT ''                        COMMENT '用户账号',
    ipaddr         VARCHAR(128)  DEFAULT ''                        COMMENT '登录IP地址',
    login_location VARCHAR(255)  DEFAULT ''                        COMMENT '登录地点',
    browser        VARCHAR(50)   DEFAULT ''                        COMMENT '浏览器类型',
    os             VARCHAR(50)   DEFAULT ''                        COMMENT '操作系统',
    status         TINYINT       DEFAULT 0                         COMMENT '登录状态(0成功 1失败)',
    msg            VARCHAR(255)  DEFAULT ''                        COMMENT '提示消息',
    login_time     DATETIME      DEFAULT CURRENT_TIMESTAMP         COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ----------------------------
-- 11. 定时任务表
-- ----------------------------
DROP TABLE IF EXISTS sys_job;
CREATE TABLE sys_job (
    id              BIGINT       NOT NULL                          COMMENT '任务ID',
    job_name        VARCHAR(64)  NOT NULL                          COMMENT '任务名称',
    job_group       VARCHAR(64)  DEFAULT 'DEFAULT'                 COMMENT '任务组名',
    invoke_target   VARCHAR(500) NOT NULL                          COMMENT '调用目标字符串',
    cron_expression VARCHAR(255) DEFAULT ''                        COMMENT 'cron执行表达式',
    misfire_policy  TINYINT      DEFAULT 3                         COMMENT '计划执行错误策略(1立即执行 2执行一次 3放弃执行)',
    concurrent      TINYINT      DEFAULT 1                         COMMENT '是否并发执行(0允许 1禁止)',
    status          TINYINT      DEFAULT 0                         COMMENT '状态(0正常 1暂停)',
    del_flag        TINYINT      DEFAULT 0                         COMMENT '删除标志',
    create_by       VARCHAR(50)  DEFAULT ''                        COMMENT '创建者',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
    update_by       VARCHAR(50)  DEFAULT ''                        COMMENT '更新者',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark          VARCHAR(500) DEFAULT ''                        COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

SET FOREIGN_KEY_CHECKS = 1;

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
(1, 'admin',      '系统管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 0, 100, '三员之系统管理员：负责用户/部门/字典/任务', 'admin'),
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
(1, '系统管理', 0, 1, 'system',  NULL, 'M', 0, 0, '', 'Setting',  'admin'),
(2, '系统监控', 0, 2, 'monitor', NULL, 'M', 0, 0, '', 'Monitor', 'admin');

-- 二级菜单 — 系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(100, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 0, 0, 'system:user:list', 'User',       'admin'),
(101, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 0, 0, 'system:role:list', 'UserFilled','admin'),
(102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 0, 0, 'system:menu:list', 'Menu',      'admin'),
(103, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', 0, 0, 'system:dept:list', 'OfficeBuilding', 'admin'),
(104, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', 0, 0, 'system:dict:list', 'Collection','admin');

-- 二级菜单 — 系统监控
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by) VALUES
(105, '操作日志', 2, 1, 'operlog',    'monitor/operlog/index',    'C', 0, 0, 'monitor:operlog:list',    'Document',      'admin'),
(106, '定时任务', 2, 3, 'job',        'monitor/job/index',        'C', 0, 0, 'monitor:job:list',        'AlarmClock',    'admin'),
(108, '登录日志', 2, 2, 'logininfor', 'monitor/logininfor/index', 'C', 0, 0, 'monitor:logininfor:list', 'Tickets',       'admin');

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

-- ----------------------------
-- 角色与菜单关联 — 三权分立职责拆分
-- ----------------------------
-- 系统管理员(1)：用户/部门/字典 + 定时任务（不含角色/菜单授权，不含日志审计）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),
(1,100),(1,103),(1,104),(1,106),
(1,1001),(1,1002),(1,1003),(1,1004),(1,1005),(1,1006),(1,1007),
(1,1017),(1,1018),(1,1019),(1,1020),
(1,1021),(1,1022),(1,1023),(1,1024),(1,1025),
(1,1029),(1,1030),(1,1031),(1,1032),(1,1033),(1,1044);

-- 普通角色(2)：只读查看用户/部门/字典
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2,1),
(2,100),(2,103),(2,104),
(2,1001),(2,1017),(2,1021);

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
