-- 乐器租赁与维修工单管理系统 数据库初始化脚本
-- MySQL 8 / utf8mb4；库名 new_040，只操作本库
DROP DATABASE IF EXISTS new_040;
CREATE DATABASE new_040 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE new_040;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号',
  `password` VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt 密码',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(80) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像',
  `role_code` VARCHAR(20) DEFAULT NULL COMMENT '角色编码',
  `store_name` VARCHAR(60) DEFAULT NULL COMMENT '所属门店',
  `status` TINYINT DEFAULT NULL COMMENT '1 启用 0 停用',
  `last_login` DATETIME DEFAULT NULL COMMENT '最近登录',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code` VARCHAR(20) DEFAULT NULL COMMENT '角色编码',
  `role_name` VARCHAR(50) DEFAULT NULL COMMENT '角色名称',
  `data_scope` VARCHAR(20) DEFAULT NULL COMMENT 'ALL 全部 / SELF 本人',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父级',
  `menu_name` VARCHAR(50) DEFAULT NULL COMMENT '菜单名称',
  `path` VARCHAR(100) DEFAULT NULL COMMENT '路由地址',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `sort` INT DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` BIGINT DEFAULT NULL COMMENT '角色',
  `menu_id` BIGINT DEFAULT NULL COMMENT '菜单',
  PRIMARY KEY (`id`),
  KEY `idx_sys_role_menu_role_id` (`role_id`),
  KEY `idx_sys_role_menu_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单';

DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT DEFAULT NULL COMMENT '接收人',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `content` VARCHAR(300) DEFAULT NULL COMMENT '内容',
  `biz_no` VARCHAR(50) DEFAULT NULL COMMENT '业务编号',
  `biz_type` VARCHAR(20) DEFAULT NULL COMMENT '业务类型',
  `link` VARCHAR(120) DEFAULT NULL COMMENT '跳转地址',
  `is_read` TINYINT DEFAULT NULL COMMENT '0 未读',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_message_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type` VARCHAR(40) DEFAULT NULL COMMENT '字典类型',
  `dict_label` VARCHAR(50) DEFAULT NULL COMMENT '显示名',
  `dict_value` VARCHAR(50) DEFAULT NULL COMMENT '值',
  `sort` INT DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典';

DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录名',
  `module` VARCHAR(40) DEFAULT NULL COMMENT '模块',
  `action` VARCHAR(40) DEFAULT NULL COMMENT '动作',
  `biz_no` VARCHAR(50) DEFAULT NULL COMMENT '业务编号',
  `content` VARCHAR(400) DEFAULT NULL COMMENT '内容',
  `create_time` DATETIME DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_log_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` VARCHAR(30) DEFAULT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '业务主键',
  `file_name` VARCHAR(120) DEFAULT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(200) DEFAULT NULL COMMENT '相对路径',
  `file_size` BIGINT DEFAULT NULL COMMENT '大小',
  `uploader` VARCHAR(50) DEFAULT NULL COMMENT '上传人',
  `create_time` DATETIME DEFAULT NULL COMMENT '上传时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件';

DROP TABLE IF EXISTS `instrument`;
CREATE TABLE `instrument` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instrument_no` VARCHAR(50) DEFAULT NULL COMMENT '乐器编号（一物一码）',
  `name` VARCHAR(60) DEFAULT NULL COMMENT '乐器名称',
  `category` VARCHAR(30) DEFAULT NULL COMMENT '类别',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(60) DEFAULT NULL COMMENT '型号',
  `level` VARCHAR(20) DEFAULT NULL COMMENT '级别',
  `size_spec` VARCHAR(50) DEFAULT NULL COMMENT '尺寸规格',
  `purchase_price` DECIMAL(10,2) DEFAULT NULL COMMENT '购置价',
  `condition_level` VARCHAR(20) DEFAULT NULL COMMENT '当前成色',
  `accessory_desc` VARCHAR(200) DEFAULT NULL COMMENT '随附配件',
  `location` VARCHAR(40) DEFAULT NULL COMMENT '存放仓位',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '租赁/维修状态',
  `image` VARCHAR(200) DEFAULT NULL COMMENT '乐器图片',
  `purchase_date` DATE DEFAULT NULL COMMENT '购置日期',
  `total_rent_days` INT DEFAULT NULL COMMENT '累计租期天数',
  `repair_count` INT DEFAULT NULL COMMENT '维修次数',
  `last_maintain_date` DATE DEFAULT NULL COMMENT '最近保养日期',
  `appearance_score` INT DEFAULT NULL COMMENT '外观评分',
  `pitch_score` INT DEFAULT NULL COMMENT '音准评分',
  `part_score` INT DEFAULT NULL COMMENT '部件状态评分',
  `score_value` DECIMAL(5,1) DEFAULT NULL COMMENT '当前状态分',
  `score_level` VARCHAR(20) DEFAULT NULL COMMENT '当前评分等级',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `version` INT DEFAULT 0 COMMENT '版本号',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instrument_instrument_no` (`instrument_no`),
  KEY `idx_instrument_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乐器主档';

DROP TABLE IF EXISTS `instrument_accessory`;
CREATE TABLE `instrument_accessory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `accessory_name` VARCHAR(50) DEFAULT NULL COMMENT '配件名称',
  `spec` VARCHAR(50) DEFAULT NULL COMMENT '规格',
  `quantity` INT DEFAULT NULL COMMENT '数量',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '状态',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_instrument_accessory_instrument_id` (`instrument_id`),
  KEY `idx_instrument_accessory_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乐器配件';

DROP TABLE IF EXISTS `rental_price`;
CREATE TABLE `rental_price` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plan_name` VARCHAR(50) DEFAULT NULL COMMENT '方案名称',
  `category` VARCHAR(30) DEFAULT NULL COMMENT '适用类别',
  `day_price` DECIMAL(10,2) DEFAULT NULL COMMENT '日租金',
  `week_price` DECIMAL(10,2) DEFAULT NULL COMMENT '周租金',
  `month_price` DECIMAL(10,2) DEFAULT NULL COMMENT '月租金',
  `deposit` DECIMAL(10,2) DEFAULT NULL COMMENT '押金标准',
  `min_days` INT DEFAULT NULL COMMENT '最短租期',
  `max_days` INT DEFAULT NULL COMMENT '最长租期',
  `overdue_rate` DECIMAL(4,2) DEFAULT NULL COMMENT '逾期费率倍数',
  `gift_accessory` VARCHAR(120) DEFAULT NULL COMMENT '赠送配件',
  `status` TINYINT DEFAULT NULL COMMENT '1 启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rental_price_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁价格方案';

DROP TABLE IF EXISTS `rental_order`;
CREATE TABLE `rental_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单号',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户',
  `customer_name` VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
  `start_date` DATE DEFAULT NULL COMMENT '起租日',
  `end_date` DATE DEFAULT NULL COMMENT '应归还日',
  `purpose` VARCHAR(200) DEFAULT NULL COMMENT '用途',
  `delivery_type` VARCHAR(20) DEFAULT NULL COMMENT '自提/配送',
  `need_accessory` VARCHAR(120) DEFAULT NULL COMMENT '需要配件',
  `id_proof` VARCHAR(50) DEFAULT NULL COMMENT '身份证明类型',
  `id_proof_image` VARCHAR(200) DEFAULT NULL COMMENT '身份证明照片',
  `deposit_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '押金',
  `rent_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '租金',
  `overdue_fee` DECIMAL(10,2) DEFAULT NULL COMMENT '逾期费',
  `compensate_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '赔偿金',
  `deposit_status` VARCHAR(20) DEFAULT NULL COMMENT '押金状态',
  `status` VARCHAR(30) DEFAULT NULL COMMENT '订单状态',
  `handler_id` BIGINT DEFAULT NULL COMMENT '受理营业员',
  `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '受理营业员姓名',
  `technician_id` BIGINT DEFAULT NULL COMMENT '维修技师',
  `technician_name` VARCHAR(50) DEFAULT NULL COMMENT '维修技师姓名',
  `remark` VARCHAR(300) DEFAULT NULL COMMENT '备注',
  `image` VARCHAR(200) DEFAULT NULL COMMENT '乐器快照',
  `apply_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `handover_time` DATETIME DEFAULT NULL COMMENT '出库时间',
  `actual_return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `version` INT DEFAULT 0 COMMENT '版本号',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_order_order_no` (`order_no`),
  KEY `idx_rental_order_instrument_id` (`instrument_id`),
  KEY `idx_rental_order_customer_id` (`customer_id`),
  KEY `idx_rental_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁与维修工单';

DROP TABLE IF EXISTS `rental_handover`;
CREATE TABLE `rental_handover` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单',
  `handover_type` VARCHAR(20) DEFAULT NULL COMMENT 'OUT 出库 / RETURN 归还',
  `appearance_check` VARCHAR(200) DEFAULT NULL COMMENT '外观检查',
  `function_check` VARCHAR(200) DEFAULT NULL COMMENT '功能检查',
  `instrument_no_check` VARCHAR(50) DEFAULT NULL COMMENT '编号核对',
  `accessory_list` VARCHAR(200) DEFAULT NULL COMMENT '配件清单',
  `scratches` VARCHAR(200) DEFAULT NULL COMMENT '已有划痕',
  `photos` VARCHAR(300) DEFAULT NULL COMMENT '交接照片',
  `customer_confirm` TINYINT DEFAULT NULL COMMENT '客户确认',
  `staff_confirm` TINYINT DEFAULT NULL COMMENT '店员确认',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '经办人',
  `handover_time` DATETIME DEFAULT NULL COMMENT '交接时间',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_rental_handover_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库/归还交接单';

DROP TABLE IF EXISTS `renewal_application`;
CREATE TABLE `renewal_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单',
  `old_end_date` DATE DEFAULT NULL COMMENT '原归还日',
  `new_end_date` DATE DEFAULT NULL COMMENT '新归还日',
  `extra_rent` DECIMAL(10,2) DEFAULT NULL COMMENT '补租金',
  `conflict_flag` TINYINT DEFAULT NULL COMMENT '1 存在冲突',
  `conflict_reason` VARCHAR(200) DEFAULT NULL COMMENT '冲突原因',
  `status` VARCHAR(20) DEFAULT NULL COMMENT 'PENDING/APPROVED/REJECTED',
  `apply_reason` VARCHAR(200) DEFAULT NULL COMMENT '申请说明',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `auditor` VARCHAR(50) DEFAULT NULL COMMENT '审核人',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`id`),
  KEY `idx_renewal_application_order_id` (`order_id`),
  KEY `idx_renewal_application_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='续租申请';

DROP TABLE IF EXISTS `return_inspection`;
CREATE TABLE `return_inspection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单',
  `function_check` VARCHAR(20) DEFAULT NULL COMMENT '功能检查',
  `appearance_check` VARCHAR(20) DEFAULT NULL COMMENT '外观检查',
  `accessory_check` VARCHAR(20) DEFAULT NULL COMMENT '配件检查',
  `clean_check` VARCHAR(20) DEFAULT NULL COMMENT '清洁情况',
  `overdue_days` INT DEFAULT NULL COMMENT '逾期天数',
  `overdue_fee` DECIMAL(10,2) DEFAULT NULL COMMENT '逾期费',
  `result` VARCHAR(20) DEFAULT NULL COMMENT 'PASS/DAMAGE/REPAIR',
  `damage_desc` VARCHAR(200) DEFAULT NULL COMMENT '损坏描述',
  `photos` VARCHAR(300) DEFAULT NULL COMMENT '归还照片',
  `inspector` VARCHAR(50) DEFAULT NULL COMMENT '验收人',
  `inspect_time` DATETIME DEFAULT NULL COMMENT '验收时间',
  PRIMARY KEY (`id`),
  KEY `idx_return_inspection_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归还验收';

DROP TABLE IF EXISTS `damage_claim`;
CREATE TABLE `damage_claim` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单',
  `damage_point` VARCHAR(200) DEFAULT NULL COMMENT '损坏点',
  `missing_accessory` VARCHAR(100) DEFAULT NULL COMMENT '缺失配件',
  `repair_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '维修成本',
  `liability` VARCHAR(20) DEFAULT NULL COMMENT '责任结论',
  `conclusion` VARCHAR(200) DEFAULT NULL COMMENT '处理结论',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '赔偿金额',
  `pay_status` VARCHAR(20) DEFAULT NULL COMMENT 'PAID/UNPAID',
  `pay_proof` VARCHAR(200) DEFAULT NULL COMMENT '收款凭证',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '经办人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_damage_claim_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='损坏赔偿';

DROP TABLE IF EXISTS `repair_order`;
CREATE TABLE `repair_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `repair_no` VARCHAR(50) DEFAULT NULL COMMENT '工单号',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联租赁订单',
  `fault_desc` VARCHAR(300) DEFAULT NULL COMMENT '故障描述',
  `fault_images` VARCHAR(300) DEFAULT NULL COMMENT '故障照片',
  `diagnosis` VARCHAR(300) DEFAULT NULL COMMENT '检测结论',
  `estimated_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '预估费用',
  `actual_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '实际费用',
  `handle_desc` VARCHAR(300) DEFAULT NULL COMMENT '实际处理',
  `test_result` VARCHAR(200) DEFAULT NULL COMMENT '试奏检验',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '工单状态',
  `technician_id` BIGINT DEFAULT NULL COMMENT '技师',
  `technician_name` VARCHAR(50) DEFAULT NULL COMMENT '技师姓名',
  `source` VARCHAR(20) DEFAULT NULL COMMENT 'RENTAL 租期报修 / STORE 门店送修',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完工时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '验收时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_order_repair_no` (`repair_no`),
  KEY `idx_repair_order_instrument_id` (`instrument_id`),
  KEY `idx_repair_order_order_id` (`order_id`),
  KEY `idx_repair_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单';

DROP TABLE IF EXISTS `repair_part`;
CREATE TABLE `repair_part` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `repair_id` BIGINT DEFAULT NULL COMMENT '工单',
  `part_name` VARCHAR(50) DEFAULT NULL COMMENT '零件名称',
  `quantity` INT DEFAULT NULL COMMENT '数量',
  `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
  `create_time` DATETIME DEFAULT NULL COMMENT '领用时间',
  PRIMARY KEY (`id`),
  KEY `idx_repair_part_repair_id` (`repair_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修零件领用';

DROP TABLE IF EXISTS `maintain_plan`;
CREATE TABLE `maintain_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `plan_date` DATE DEFAULT NULL COMMENT '计划日期',
  `cycle_days` INT DEFAULT NULL COMMENT '保养周期',
  `content` VARCHAR(200) DEFAULT NULL COMMENT '保养内容',
  `status` VARCHAR(20) DEFAULT NULL COMMENT 'PLAN/DUE/DONE',
  `finish_date` DATE DEFAULT NULL COMMENT '完成日期',
  `executor` VARCHAR(50) DEFAULT NULL COMMENT '执行人',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_maintain_plan_instrument_id` (`instrument_id`),
  KEY `idx_maintain_plan_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保养计划';

DROP TABLE IF EXISTS `customer_feedback`;
CREATE TABLE `customer_feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户',
  `rating` INT DEFAULT NULL COMMENT '评分 1-5',
  `content` VARCHAR(300) DEFAULT NULL COMMENT '评价内容',
  `create_time` DATETIME DEFAULT NULL COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_feedback_order_id` (`order_id`),
  KEY `idx_customer_feedback_instrument_id` (`instrument_id`),
  KEY `idx_customer_feedback_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户评价';

DROP TABLE IF EXISTS `score_rule`;
CREATE TABLE `score_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version_no` VARCHAR(20) DEFAULT NULL COMMENT '规则版本',
  `weight_appearance` INT DEFAULT NULL COMMENT '外观权重',
  `weight_pitch` INT DEFAULT NULL COMMENT '音准权重',
  `weight_part` INT DEFAULT NULL COMMENT '部件权重',
  `weight_rent_days` INT DEFAULT NULL COMMENT '累计租期权重',
  `weight_repair_count` INT DEFAULT NULL COMMENT '维修次数权重',
  `weight_maintain` INT DEFAULT NULL COMMENT '保养间隔权重',
  `threshold` INT DEFAULT NULL COMMENT '出租阈值',
  `enabled` TINYINT DEFAULT NULL COMMENT '1 启用',
  `effect_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '说明',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分规则';

DROP TABLE IF EXISTS `score_record`;
CREATE TABLE `score_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `instrument_id` BIGINT DEFAULT NULL COMMENT '乐器',
  `rule_id` BIGINT DEFAULT NULL COMMENT '规则',
  `rule_version` VARCHAR(20) DEFAULT NULL COMMENT '规则版本',
  `appearance_input` INT DEFAULT NULL COMMENT '外观输入',
  `pitch_input` INT DEFAULT NULL COMMENT '音准输入',
  `part_input` INT DEFAULT NULL COMMENT '部件输入',
  `rent_days_input` INT DEFAULT NULL COMMENT '累计租期输入',
  `repair_count_input` INT DEFAULT NULL COMMENT '维修次数输入',
  `maintain_days_input` INT DEFAULT NULL COMMENT '保养间隔输入',
  `detail_json` TEXT DEFAULT NULL COMMENT '明细快照 JSON',
  `total_score` DECIMAL(5,1) DEFAULT NULL COMMENT '总分',
  `level` VARCHAR(20) DEFAULT NULL COMMENT '等级',
  `recommend` VARCHAR(30) DEFAULT NULL COMMENT '推荐结果',
  `main_factors` VARCHAR(300) DEFAULT NULL COMMENT '主要影响因素',
  `unmet` VARCHAR(300) DEFAULT NULL COMMENT '未满足条件',
  `improve` VARCHAR(300) DEFAULT NULL COMMENT '可改进项',
  `manual_result` VARCHAR(30) DEFAULT NULL COMMENT '人工修正结果',
  `adjust_reason` VARCHAR(200) DEFAULT NULL COMMENT '修正原因',
  `adjust_by` VARCHAR(50) DEFAULT NULL COMMENT '修正人',
  `adjust_time` DATETIME DEFAULT NULL COMMENT '修正时间',
  `create_time` DATETIME DEFAULT NULL COMMENT '计算时间',
  PRIMARY KEY (`id`),
  KEY `idx_score_record_instrument_id` (`instrument_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分计算留痕';

INSERT INTO sys_user (id,username,password,real_name,phone,email,avatar,role_code,store_name,status,last_login,create_time) VALUES
(1,'admin','admin123','门店管理员','13800000001','admin@music-store.cn','/uploads/seed/avatar/01.jpg','ADMIN','总店',1,'2026-10-24 09:20:00','2026-05-06 09:00:00'),
(2,'clerk01','123456','李婉','13800000002','liwan@music-store.cn','/uploads/seed/avatar/02.jpg','CLERK','总店',1,'2026-10-28 09:20:00','2026-05-06 09:00:00'),
(3,'clerk02','123456','郭程','13800000003','guocheng@music-store.cn','/uploads/seed/avatar/03.jpg','CLERK','分店',1,'2026-10-26 09:20:00','2026-05-06 09:00:00'),
(4,'tech01','123456','周晗','13800000004','zhouhan@music-store.cn','/uploads/seed/avatar/04.jpg','TECHNICIAN','总店',1,'2026-10-11 09:20:00','2026-05-06 09:00:00'),
(5,'tech02','123456','孙立','13800000005','sunli@music-store.cn','/uploads/seed/avatar/01.jpg','TECHNICIAN','总店',1,'2026-10-17 09:20:00','2026-05-06 09:00:00'),
(6,'customer01','123456','陈一鸣','13800000006','chenym@example.com','/uploads/seed/avatar/02.jpg','CUSTOMER','总店',1,'2026-10-19 09:20:00','2026-05-06 09:00:00'),
(7,'customer02','123456','林小雨','13800000007','linxy@example.com','/uploads/seed/avatar/03.jpg','CUSTOMER','总店',1,'2026-10-31 09:20:00','2026-05-06 09:00:00'),
(8,'customer03','123456','赵梓涵','13800000008','zhaozh@example.com','/uploads/seed/avatar/04.jpg','CUSTOMER','分店',1,'2026-10-30 09:20:00','2026-05-06 09:00:00'),
(9,'customer04','123456','吴桐','13800000009','wutong@example.com','/uploads/seed/avatar/01.jpg','CUSTOMER','分店',1,'2026-10-31 09:20:00','2026-05-06 09:00:00');

INSERT INTO sys_role (id,role_code,role_name,data_scope,remark) VALUES
(1,'ADMIN','门店管理员','ALL','乐器、价格、仓位、保养、备件、权限与统计'),
(2,'CLERK','门店营业员','ALL','预约审核、押金登记、交接、归还与赔偿'),
(3,'TECHNICIAN','维修技师','SELF','接单、检测、报价、维修、试奏与验收'),
(4,'CUSTOMER','客户','SELF','浏览乐器、预约、续租、报修、归还与台账');

INSERT INTO sys_menu (id,parent_id,menu_name,path,icon,sort) VALUES
(1,0,'资产经营看板','/dashboard','Odometer',1),
(2,0,'乐器租赁目录','/catalog','Grid',2),
(3,0,'可租日历','/calendar','Calendar',3),
(4,0,'租赁订单台','/order-desk','Tickets',4),
(5,0,'出库交接','/handover','Share',5),
(6,0,'归还验收','/return-check','RefreshLeft',6),
(7,0,'损坏赔偿','/damage','Warning',7),
(8,0,'维修工单','/repair','Tools',8),
(9,0,'乐器台账','/instrument','Box',9),
(10,0,'租赁价格','/price','PriceTag',10),
(11,0,'保养计划','/maintain','Clock',11),
(12,0,'评分规则','/score-rule','SetUp',12),
(13,0,'用户与角色','/user','User',13),
(14,0,'菜单管理','/menu','Menu',14),
(15,0,'消息中心','/message','Bell',15),
(16,0,'个人中心','/profile','Avatar',16),
(17,0,'我的租赁','/my-orders','Document',17),
(18,0,'我的报修','/my-repair','EditPen',18),
(19,0,'费用台账','/ledger','Money',19);

INSERT INTO sys_role_menu (id,role_id,menu_id) VALUES
(1,1,1),
(2,1,2),
(3,1,3),
(4,1,4),
(5,1,5),
(6,1,6),
(7,1,7),
(8,1,8),
(9,1,9),
(10,1,10),
(11,1,11),
(12,1,12),
(13,1,13),
(14,1,14),
(15,1,15),
(16,1,16),
(17,2,4),
(18,2,5),
(19,2,6),
(20,2,7),
(21,2,3),
(22,2,9),
(23,2,2),
(24,2,15),
(25,2,16),
(26,3,8),
(27,3,9),
(28,3,15),
(29,3,16),
(30,4,2),
(31,4,3),
(32,4,17),
(33,4,18),
(34,4,19),
(35,4,15),
(36,4,16);

INSERT INTO sys_dict (id,dict_type,dict_label,dict_value,sort) VALUES
(1,'category','吉他','category_1',1),
(2,'category','钢琴','category_2',2),
(3,'category','弦乐','category_3',3),
(4,'category','管乐','category_4',4),
(5,'category','打击乐','category_5',5),
(6,'category','民乐','category_6',6),
(7,'category','键盘','category_7',7),
(8,'condition','九成新','condition_1',1),
(9,'condition','八成新','condition_2',2),
(10,'condition','七成新','condition_3',3),
(11,'condition','六成新','condition_4',4),
(12,'level','入门','level_1',1),
(13,'level','进阶','level_2',2),
(14,'level','专业','level_3',3),
(15,'purpose','考级练习','purpose_1',1),
(16,'purpose','演出排练','purpose_2',2),
(17,'purpose','短期体验','purpose_3',3),
(18,'purpose','教学使用','purpose_4',4),
(19,'purpose','录音制作','purpose_5',5),
(20,'delivery','到店自提','delivery_1',1),
(21,'delivery','门店配送','delivery_2',2),
(22,'liability','客户责任','liability_1',1),
(23,'liability','自然损耗','liability_2',2),
(24,'liability','门店责任','liability_3',3);

INSERT INTO instrument (id,instrument_no,name,category,brand,model,level,size_spec,purchase_price,condition_level,
accessory_desc,location,status,image,purchase_date,total_rent_days,repair_count,last_maintain_date,
appearance_score,pitch_score,part_score,score_value,score_level,deleted,version,create_time,update_time) VALUES
(1,'INS-2026-0011','民谣吉他','吉他','Yamaha','FG830','进阶','41寸',2380,'九成新','琴包、背带、变调夹','A-01-03','AVAILABLE','/uploads/seed/instrument/08.jpg','2022-02-11',186,1,'2026-02-11',82,88,75,77.7,'OBSERVE',0,0,'2026-06-11 09:01:00','2026-11-21 14:01:00'),
(2,'INS-2026-0012','立式钢琴','钢琴','Kawai','K300','专业','122cm',48000,'八成新','琴凳、防潮管','B-02-01','AVAILABLE','/uploads/seed/instrument/03.jpg','2023-03-12',420,2,'2026-03-12',78,90,82,68.5,'MAINTAIN',0,0,'2026-07-12 09:02:00','2026-10-22 14:02:00'),
(3,'INS-2026-0013','小提琴','弦乐','Scott Cao','STV-750','专业','4/4',9600,'七成新','琴盒、松香、肩托','A-03-02','RENTING','/uploads/seed/instrument/10.jpg','2024-04-13',268,3,'2026-04-13',74,86,68,69.1,'MAINTAIN',0,0,'2026-08-13 09:03:00','2026-11-23 14:03:00'),
(4,'INS-2026-0014','中音萨克斯','管乐','Selmer','AS500','进阶','降E调',14800,'九成新','琴盒、清洁套装','C-01-01','REPAIRING','/uploads/seed/instrument/05.jpg','2021-05-14',152,2,'2026-05-14',80,72,62,71.6,'MAINTAIN',0,0,'2026-09-14 09:04:00','2026-10-24 14:04:00'),
(5,'INS-2026-0015','电子鼓','打击乐','Roland','TD-17KVX','入门','标准套装',7200,'九成新','鼓槌、耳机','C-02-04','MAINTAINING','/uploads/seed/instrument/12.jpg','2022-06-15',210,2,'2026-06-15',84,80,70,73.1,'MAINTAIN',0,0,'2026-010-15 09:05:00','2026-11-25 14:05:00'),
(6,'INS-2026-0016','古典吉他','吉他','Cordoba','C9','进阶','39寸',3600,'八成新','琴包、脚凳','A-01-05','AVAILABLE','/uploads/seed/instrument/07.jpg','2023-07-16',240,1,'2026-07-16',76,84,72,72.1,'MAINTAIN',0,0,'2026-05-16 09:06:00','2026-10-26 14:06:00'),
(7,'INS-2026-0017','电吉他','吉他','Fender','Player Strat','专业','标准',7800,'九成新','琴包、背带、连接线','A-02-01','RENTING','/uploads/seed/instrument/02.jpg','2024-08-17',96,0,'2026-08-17',88,90,86,87.6,'RECOMMEND',0,0,'2026-06-17 09:07:00','2026-11-27 14:07:00'),
(8,'INS-2026-0018','大提琴','弦乐','Yamaha','VC5','进阶','4/4',12800,'八成新','琴盒、松香','A-03-04','AVAILABLE','/uploads/seed/instrument/09.jpg','2021-09-18',312,2,'2026-01-10',72,82,66,67.3,'MAINTAIN',0,0,'2026-07-18 09:08:00','2026-10-20 14:08:00'),
(9,'INS-2026-0019','长笛','管乐','Muramatsu','EX','专业','C调',18600,'九成新','硬盒、通条','C-01-03','AVAILABLE','/uploads/seed/instrument/04.jpg','2022-01-10',128,1,'2026-02-11',86,88,80,81.8,'OBSERVE',0,0,'2026-08-10 09:09:00','2026-11-21 14:09:00'),
(10,'INS-2026-0020','单簧管','管乐','Buffet','E12','入门','降B调',6200,'八成新','硬盒、哨片×3','C-01-04','RESERVED','/uploads/seed/instrument/11.jpg','2023-02-11',176,1,'2026-03-12',78,80,70,72.2,'MAINTAIN',0,0,'2026-09-11 09:10:00','2026-10-22 14:10:00'),
(11,'INS-2026-0021','数码钢琴','钢琴','Roland','FP-30X','入门','88键',5600,'九成新','琴架、踏板','B-01-02','AVAILABLE','/uploads/seed/instrument/06.jpg','2024-03-12',264,1,'2026-04-13',84,88,78,75.8,'OBSERVE',0,0,'2026-010-12 09:11:00','2026-11-23 14:11:00'),
(12,'INS-2026-0022','尤克里里','吉他','Kala','KA-15S','入门','23寸',680,'九成新','琴包、拨片','A-01-08','AVAILABLE','/uploads/seed/instrument/01.jpg','2021-04-13',88,0,'2026-05-14',90,92,88,88.4,'RECOMMEND',0,0,'2026-05-13 09:12:00','2026-10-24 14:12:00'),
(13,'INS-2026-0023','手风琴','键盘','Hohner','Bravo III','进阶','96贝斯',9800,'七成新','琴包、背带','B-03-01','AVAILABLE','/uploads/seed/instrument/08.jpg','2022-05-14',340,3,'2026-06-15',68,76,60,58.6,'FORBID',0,0,'2026-06-14 09:13:00','2026-11-25 14:13:00'),
(14,'INS-2026-0024','架子鼓','打击乐','Pearl','Roadshow','入门','5鼓3镲',8600,'八成新','鼓槌、鼓凳','C-02-01','AVAILABLE','/uploads/seed/instrument/03.jpg','2023-06-15',198,2,'2026-07-16',74,78,68,70.2,'MAINTAIN',0,0,'2026-07-15 09:14:00','2026-10-26 14:14:00'),
(15,'INS-2026-0025','二胡','民乐','敦煌','09A','进阶','标准',2800,'九成新','琴盒、松香、弓','A-04-01','AVAILABLE','/uploads/seed/instrument/10.jpg','2024-07-16',156,1,'2026-08-17',82,84,76,78.7,'OBSERVE',0,0,'2026-08-16 09:15:00','2026-11-27 14:15:00'),
(16,'INS-2026-0026','古筝','民乐','敦煌','694KK','专业','163cm',12800,'八成新','琴架、义甲、调音器','B-04-01','AVAILABLE','/uploads/seed/instrument/05.jpg','2021-08-17',288,2,'2026-01-10',76,80,70,68.0,'MAINTAIN',0,0,'2026-09-17 09:16:00','2026-10-20 14:16:00'),
(17,'INS-2026-0027','次中音萨克斯','管乐','Yamaha','YTS-280','进阶','降B调',16800,'九成新','琴盒、吊带','C-01-06','AVAILABLE','/uploads/seed/instrument/12.jpg','2022-09-18',142,1,'2026-02-11',84,86,76,79.5,'OBSERVE',0,0,'2026-010-18 09:17:00','2026-11-21 14:17:00'),
(18,'INS-2026-0028','电贝司','吉他','Squier','Affinity J','入门','4弦',3200,'八成新','琴包、连接线','A-02-03','AVAILABLE','/uploads/seed/instrument/07.jpg','2023-01-10',168,1,'2026-03-12',78,82,72,74.5,'MAINTAIN',0,0,'2026-05-10 09:18:00','2026-10-22 14:18:00'),
(19,'INS-2026-0029','中提琴','弦乐','Scott Cao','STV-650','进阶','16寸',8800,'八成新','琴盒、松香','A-03-06','AVAILABLE','/uploads/seed/instrument/02.jpg','2024-02-11',204,2,'2026-04-13',74,80,66,69.0,'MAINTAIN',0,0,'2026-06-11 09:19:00','2026-11-23 14:19:00'),
(20,'INS-2026-0030','小号','管乐','Bach','TR300','入门','降B调',7400,'七成新','硬盒、号嘴','C-01-08','SCRAPPED','/uploads/seed/instrument/09.jpg','2021-03-12',366,4,'2026-05-14',62,68,52,49.6,'FORBID',0,0,'2026-07-12 09:20:00','2026-10-24 14:20:00'),
(21,'INS-2026-0031','木吉他','吉他','Martin','DX1AE','专业','41寸',6800,'九成新','琴包、背带','A-01-09','RENTING','/uploads/seed/instrument/04.jpg','2022-04-13',120,0,'2026-06-15',88,92,84,86.9,'RECOMMEND',0,0,'2026-08-13 09:21:00','2026-11-25 14:21:00'),
(22,'INS-2026-0032','电子琴','键盘','Casio','CT-S500','入门','61键',2400,'九成新','琴架、电源','B-01-05','AVAILABLE','/uploads/seed/instrument/11.jpg','2023-05-14',96,0,'2026-07-16',86,88,82,85.5,'RECOMMEND',0,0,'2026-09-14 09:22:00','2026-10-26 14:22:00');

INSERT INTO instrument_accessory (id,instrument_id,accessory_name,spec,quantity,status,remark) VALUES
(1,1,'琴包','标准',1,'IN_STOCK','随琴出库'),
(2,1,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(3,1,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(4,2,'琴包','标准',1,'IN_STOCK','随琴出库'),
(5,2,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(6,3,'琴包','标准',1,'IN_STOCK','随琴出库'),
(7,3,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(8,3,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(9,4,'琴包','标准',1,'IN_STOCK','随琴出库'),
(10,4,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(11,5,'琴包','标准',1,'IN_STOCK','随琴出库'),
(12,5,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(13,5,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(14,6,'琴包','标准',1,'IN_STOCK','随琴出库'),
(15,6,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(16,7,'琴包','标准',1,'IN_STOCK','随琴出库'),
(17,7,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(18,7,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(19,8,'琴包','标准',1,'IN_STOCK','随琴出库'),
(20,8,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(21,9,'琴包','标准',1,'IN_STOCK','随琴出库'),
(22,9,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(23,9,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(24,10,'琴包','标准',1,'IN_STOCK','随琴出库'),
(25,10,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(26,11,'琴包','标准',1,'IN_STOCK','随琴出库'),
(27,11,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(28,11,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(29,12,'琴包','标准',1,'IN_STOCK','随琴出库'),
(30,12,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(31,13,'琴包','标准',1,'IN_STOCK','随琴出库'),
(32,13,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(33,13,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(34,14,'琴包','标准',1,'IN_STOCK','随琴出库'),
(35,14,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(36,15,'琴包','标准',1,'IN_STOCK','随琴出库'),
(37,15,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(38,15,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(39,16,'琴包','标准',1,'IN_STOCK','随琴出库'),
(40,16,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(41,17,'琴包','标准',1,'IN_STOCK','随琴出库'),
(42,17,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(43,17,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(44,18,'琴包','标准',1,'IN_STOCK','随琴出库'),
(45,18,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(46,19,'琴包','标准',1,'IN_STOCK','随琴出库'),
(47,19,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(48,19,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(49,20,'琴包','标准',1,'IN_STOCK','随琴出库'),
(50,20,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(51,21,'琴包','标准',1,'IN_STOCK','随琴出库'),
(52,21,'背带','尼龙',1,'IN_STOCK','随琴出库'),
(53,21,'调音器','夹式',1,'IN_STOCK','随琴出库'),
(54,22,'琴包','标准',1,'IN_STOCK','随琴出库'),
(55,22,'背带','尼龙',1,'IN_STOCK','随琴出库');

INSERT INTO rental_price (id,plan_name,category,day_price,week_price,month_price,deposit,min_days,max_days,overdue_rate,gift_accessory,status,create_time) VALUES
(1,'吉他标准','吉他',38,228,760,800,3,90,1.5,'琴包、背带',1,'2026-05-06 09:00:00'),
(2,'钢琴长租','钢琴',260,1500,4800,6000,30,365,2.0,'琴凳、防潮管',1,'2026-05-06 09:01:00'),
(3,'弦乐标准','弦乐',56,336,1120,1500,7,180,1.5,'松香、肩托',1,'2026-05-06 09:02:00'),
(4,'管乐短期','管乐',88,528,1760,2500,3,60,1.5,'清洁套装',0,'2026-05-06 09:03:00'),
(5,'打击乐标准','打击乐',120,720,2400,3000,3,120,1.5,'鼓槌',1,'2026-05-06 09:04:00'),
(6,'键盘标准','键盘',72,432,1440,2000,7,180,1.5,'琴架',1,'2026-05-06 09:05:00'),
(7,'民乐标准','民乐',46,276,920,1200,7,180,1.5,'松香、义甲',1,'2026-05-06 09:06:00');

INSERT INTO rental_order (id,order_no,instrument_id,customer_id,customer_name,start_date,end_date,purpose,delivery_type,
need_accessory,id_proof,id_proof_image,deposit_amount,rent_amount,overdue_fee,compensate_amount,deposit_status,status,
handler_id,handler_name,technician_id,technician_name,remark,image,apply_time,handover_time,actual_return_time,
deleted,version,create_time,update_time) VALUES
(1,'RO-20260508-0001',1,6,'陈一鸣','2026-05-10','2026-05-17','考级练习','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,266,0.0,0,'UNPAID','PENDING_AUDIT',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/08.jpg','2026-05-08 09:00:00',NULL,NULL,0,0,'2026-05-08 09:00:00','2026-05-09 10:00:00'),
(2,'RO-20260513-0002',6,7,'林小雨','2026-05-15','2026-05-29','演出排练','门店配送','琴包','身份证','/uploads/seed/idproof/02.jpg',800,532,0.0,0,'UNPAID','PENDING_DEPOSIT',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/07.jpg','2026-05-13 10:01:00',NULL,NULL,0,0,'2026-05-13 10:01:00','2026-05-14 10:01:00'),
(3,'RO-20260518-0003',11,8,'赵梓涵','2026-05-20','2026-06-19','短期体验','到店自提','琴架','身份证','/uploads/seed/idproof/01.jpg',6000,6630.0,0.0,0,'PAID','PENDING_HANDOVER',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/06.jpg','2026-05-18 11:02:00',NULL,NULL,0,0,'2026-05-18 11:02:00','2026-05-19 10:02:00'),
(4,'RO-20260523-0004',16,9,'吴桐','2026-05-25','2026-07-24','教学使用','门店配送','琴架','身份证','/uploads/seed/idproof/02.jpg',1200,2346.0,0.0,0,'PAID','RENTING',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/05.jpg','2026-05-23 12:03:00','2026-05-25 15:03:00',NULL,0,0,'2026-05-23 12:03:00','2026-05-24 10:03:00'),
(5,'RO-20260528-0005',21,6,'陈一鸣','2026-05-30','2026-08-28','录音制作','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,2907.0,114.0,0,'PAID','PENDING_RETURN',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/04.jpg','2026-05-28 13:04:00','2026-05-30 15:04:00',NULL,0,0,'2026-05-28 13:04:00','2026-05-29 10:04:00'),
(6,'RO-20260602-0006',4,7,'林小雨','2026-06-04','2026-06-11','考级练习','门店配送','琴盒','身份证','/uploads/seed/idproof/02.jpg',2500,616,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/05.jpg','2026-06-02 14:05:00','2026-06-04 15:05:00','2026-06-13 11:05:00',0,0,'2026-06-02 14:05:00','2026-06-03 10:05:00'),
(7,'RO-20260607-0007',9,8,'赵梓涵','2026-06-09','2026-06-23','演出排练','到店自提','硬盒','身份证','/uploads/seed/idproof/01.jpg',2500,1232,0.0,0,'REFUNDED','COMPLETED',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/04.jpg','2026-06-07 15:06:00','2026-06-09 15:06:00','2026-06-25 11:06:00',0,0,'2026-06-07 15:06:00','2026-06-08 10:06:00'),
(8,'RO-20260612-0008',14,9,'吴桐','2026-06-14','2026-07-14','短期体验','门店配送','鼓槌','身份证','/uploads/seed/idproof/02.jpg',3000,3060.0,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/03.jpg','2026-06-12 16:07:00','2026-06-14 15:07:00','2026-07-16 11:07:00',0,0,'2026-06-12 16:07:00','2026-06-13 10:07:00'),
(9,'RO-20260617-0009',19,6,'陈一鸣','2026-06-19','2026-08-18','教学使用','到店自提','琴盒','身份证','/uploads/seed/idproof/01.jpg',1500,2856.0,0.0,0,'UNPAID','REJECTED',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/02.jpg','2026-06-17 09:08:00',NULL,NULL,0,0,'2026-06-17 09:08:00','2026-06-18 10:08:00'),
(10,'RO-20260622-0010',2,7,'林小雨','2026-06-24','2026-09-22','录音制作','门店配送','琴凳','身份证','/uploads/seed/idproof/02.jpg',6000,19890.0,0.0,0,'UNPAID','CANCELLED',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/03.jpg','2026-06-22 10:09:00',NULL,NULL,0,0,'2026-06-22 10:09:00','2026-06-23 10:09:00'),
(11,'RO-20260627-0011',7,8,'赵梓涵','2026-06-29','2026-07-06','考级练习','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,266,0.0,0,'PAID','RENTING',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/02.jpg','2026-06-27 11:10:00','2026-06-29 15:10:00',NULL,0,0,'2026-06-27 11:10:00','2026-06-28 10:10:00'),
(12,'RO-20260702-0012',12,9,'吴桐','2026-07-04','2026-07-18','演出排练','门店配送','琴包','身份证','/uploads/seed/idproof/02.jpg',800,532,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/01.jpg','2026-07-02 12:11:00','2026-07-04 15:11:00','2026-07-20 11:11:00',0,0,'2026-07-02 12:11:00','2026-07-03 10:11:00'),
(13,'RO-20260707-0013',17,6,'陈一鸣','2026-07-09','2026-08-08','短期体验','到店自提','琴盒','身份证','/uploads/seed/idproof/01.jpg',2500,2244.0,0.0,0,'UNPAID','PENDING_AUDIT',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/12.jpg','2026-07-07 13:12:00',NULL,NULL,0,0,'2026-07-07 13:12:00','2026-07-08 10:12:00'),
(14,'RO-20260712-0014',22,7,'林小雨','2026-07-14','2026-09-12','教学使用','门店配送','琴架','身份证','/uploads/seed/idproof/02.jpg',2000,3672.0,0.0,0,'UNPAID','PENDING_DEPOSIT',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/11.jpg','2026-07-12 14:13:00',NULL,NULL,0,0,'2026-07-12 14:13:00','2026-07-13 10:13:00'),
(15,'RO-20260717-0015',5,8,'赵梓涵','2026-07-19','2026-10-17','录音制作','到店自提','鼓槌','身份证','/uploads/seed/idproof/01.jpg',3000,9180.0,0.0,0,'PAID','RENTING',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/12.jpg','2026-07-17 15:14:00','2026-07-19 15:14:00',NULL,0,0,'2026-07-17 15:14:00','2026-07-18 10:14:00'),
(16,'RO-20260722-0016',10,9,'吴桐','2026-07-24','2026-07-31','考级练习','门店配送','硬盒','身份证','/uploads/seed/idproof/02.jpg',2500,616,264.0,0,'PAID','PENDING_RETURN',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/11.jpg','2026-07-22 16:15:00','2026-07-24 15:15:00',NULL,0,0,'2026-07-22 16:15:00','2026-07-23 10:15:00'),
(17,'RO-20260727-0017',15,6,'陈一鸣','2026-07-29','2026-08-12','演出排练','到店自提','琴盒','身份证','/uploads/seed/idproof/01.jpg',1200,644,0.0,0,'REFUNDED','COMPLETED',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/10.jpg','2026-07-27 09:16:00','2026-07-29 15:16:00','2026-08-14 11:16:00',0,0,'2026-07-27 09:16:00','2026-07-28 10:16:00'),
(18,'RO-20260801-0018',20,7,'林小雨','2026-08-03','2026-09-02','短期体验','门店配送','硬盒','身份证','/uploads/seed/idproof/02.jpg',2500,2244.0,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/09.jpg','2026-08-01 10:17:00','2026-08-03 15:17:00','2026-09-04 11:17:00',0,0,'2026-08-01 10:17:00','2026-08-02 10:17:00'),
(19,'RO-20260806-0019',3,8,'赵梓涵','2026-08-08','2026-10-07','教学使用','到店自提','琴盒','身份证','/uploads/seed/idproof/01.jpg',1500,2856.0,0.0,0,'UNPAID','REJECTED',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/10.jpg','2026-08-06 11:18:00',NULL,NULL,0,0,'2026-08-06 11:18:00','2026-08-07 10:18:00'),
(20,'RO-20260811-0020',8,9,'吴桐','2026-08-13','2026-11-11','录音制作','门店配送','琴盒','身份证','/uploads/seed/idproof/02.jpg',1500,4284.0,0.0,0,'PAID','RENTING',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/09.jpg','2026-08-11 12:19:00','2026-08-13 15:19:00',NULL,0,0,'2026-08-11 12:19:00','2026-08-12 10:19:00'),
(21,'RO-20260816-0021',13,6,'陈一鸣','2026-08-18','2026-08-25','考级练习','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',2000,504,0.0,0,'REFUNDED','COMPLETED',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/08.jpg','2026-08-16 13:20:00','2026-08-18 15:20:00','2026-08-27 11:20:00',0,0,'2026-08-16 13:20:00','2026-08-17 10:20:00'),
(22,'RO-20260821-0022',18,7,'林小雨','2026-08-23','2026-09-06','演出排练','门店配送','琴包','身份证','/uploads/seed/idproof/02.jpg',800,532,0.0,0,'UNPAID','PENDING_AUDIT',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/07.jpg','2026-08-21 14:21:00',NULL,NULL,0,0,'2026-08-21 14:21:00','2026-08-22 10:21:00'),
(23,'RO-20260826-0023',1,8,'赵梓涵','2026-08-28','2026-09-27','短期体验','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,969.0,0.0,0,'UNPAID','CANCELLED',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/08.jpg','2026-08-26 15:22:00',NULL,NULL,0,0,'2026-08-26 15:22:00','2026-08-27 10:22:00'),
(24,'RO-20260831-0024',6,9,'吴桐','2026-09-02','2026-11-01','教学使用','门店配送','琴包','身份证','/uploads/seed/idproof/02.jpg',800,1938.0,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/07.jpg','2026-08-31 16:23:00','2026-09-02 15:23:00','2026-11-03 11:23:00',0,0,'2026-08-31 16:23:00','2026-09-01 10:23:00'),
(25,'RO-20260905-0025',11,6,'陈一鸣','2026-09-07','2026-12-06','录音制作','到店自提','琴架','身份证','/uploads/seed/idproof/01.jpg',6000,19890.0,0.0,0,'PAID','RENTING',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/06.jpg','2026-09-05 09:24:00','2026-09-07 15:24:00',NULL,0,0,'2026-09-05 09:24:00','2026-09-06 10:24:00'),
(26,'RO-20260910-0026',16,7,'林小雨','2026-09-12','2026-09-19','考级练习','门店配送','琴架','身份证','/uploads/seed/idproof/02.jpg',1200,322,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/05.jpg','2026-09-10 10:25:00','2026-09-12 15:25:00','2026-09-21 11:25:00',0,0,'2026-09-10 10:25:00','2026-09-11 10:25:00'),
(27,'RO-20261003-0027',21,8,'赵梓涵','2026-10-05','2026-10-19','演出排练','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,532,0.0,0,'UNPAID','PENDING_DEPOSIT',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/04.jpg','2026-10-03 11:26:00',NULL,NULL,0,0,'2026-10-03 11:26:00','2026-10-04 10:26:00'),
(28,'RO-20261006-0028',4,9,'吴桐','2026-10-08','2026-11-07','短期体验','门店配送','琴盒','身份证','/uploads/seed/idproof/02.jpg',2500,2244.0,0.0,0.0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/05.jpg','2026-10-06 12:27:00','2026-10-08 15:27:00','2026-11-09 11:27:00',0,0,'2026-10-06 12:27:00','2026-10-07 10:27:00'),
(29,'RO-20261009-0029',9,6,'陈一鸣','2026-10-11','2026-12-10','教学使用','到店自提','硬盒','身份证','/uploads/seed/idproof/01.jpg',2500,4488.0,0.0,0,'PAID','RENTING',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/04.jpg','2026-10-09 13:28:00','2026-10-11 15:28:00',NULL,0,0,'2026-10-09 13:28:00','2026-10-10 10:28:00'),
(30,'RO-20261012-0030',14,7,'林小雨','2026-10-14','2027-01-12','录音制作','门店配送','鼓槌','身份证','/uploads/seed/idproof/02.jpg',3000,9180.0,0.0,0,'PAID','PENDING_HANDOVER',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/03.jpg','2026-10-12 14:29:00',NULL,NULL,0,0,'2026-10-12 14:29:00','2026-10-13 10:29:00'),
(31,'RO-20261015-0031',19,8,'赵梓涵','2026-10-17','2026-10-24','考级练习','到店自提','琴盒','身份证','/uploads/seed/idproof/01.jpg',1500,392,0.0,0,'REFUNDED','COMPLETED',2,'李婉',4,'周晗','老客户复租，加赠变调夹','/uploads/seed/instrument/02.jpg','2026-10-15 15:30:00','2026-10-17 15:30:00','2026-10-26 11:30:00',0,0,'2026-10-15 15:30:00','2026-10-16 10:30:00'),
(32,'RO-20261018-0032',2,9,'吴桐','2026-10-20','2026-11-03','演出排练','门店配送','琴凳','身份证','/uploads/seed/idproof/02.jpg',6000,3640,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','考级用琴，需调音至 A440','/uploads/seed/instrument/03.jpg','2026-10-18 16:31:00','2026-10-20 15:31:00','2026-11-05 11:31:00',0,0,'2026-10-18 16:31:00','2026-10-19 10:31:00'),
(33,'RO-20261021-0033',7,6,'陈一鸣','2026-10-23','2026-11-22','短期体验','到店自提','琴包','身份证','/uploads/seed/idproof/01.jpg',800,969.0,0.0,0,'UNPAID','REJECTED',2,'李婉',4,'周晗','客户自提，门店已讲解保养注意','/uploads/seed/instrument/02.jpg','2026-10-21 09:32:00',NULL,NULL,0,0,'2026-10-21 09:32:00','2026-10-22 10:32:00'),
(34,'RO-20261024-0034',12,7,'林小雨','2026-10-26','2026-12-25','教学使用','门店配送','琴包','身份证','/uploads/seed/idproof/02.jpg',800,1938.0,0.0,0,'REFUNDED','COMPLETED',3,'郭程',5,'孙立','配送上门，含安装调试','/uploads/seed/instrument/01.jpg','2026-10-24 10:33:00','2026-10-26 15:33:00','2026-12-27 11:33:00',0,0,'2026-10-24 10:33:00','2026-10-25 10:33:00');

INSERT INTO rental_handover (id,order_id,handover_type,appearance_check,function_check,instrument_no_check,accessory_list,scratches,photos,customer_confirm,staff_confirm,operator,handover_time,remark) VALUES
(1,27,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/07.jpg',1,1,'李婉','2026-10-05 15:26:00','出库检查完成'),
(2,27,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/07.jpg',1,1,'李婉','2026-10-12 11:26:00','归还验收通过'),
(3,28,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/10.jpg',1,1,'郭程','2026-10-08 15:27:00','出库检查完成'),
(4,28,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/10.jpg',1,1,'郭程','2026-10-15 11:27:00','归还验收通过'),
(5,29,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/01.jpg',1,1,'李婉','2026-10-11 15:28:00','出库检查完成'),
(6,29,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/01.jpg',1,1,'李婉','2026-10-18 11:28:00','归还验收通过'),
(7,30,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/04.jpg',1,1,'郭程','2026-10-14 15:29:00','出库检查完成'),
(8,30,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/04.jpg',1,1,'郭程','2026-10-21 11:29:00','归还验收通过'),
(9,31,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/07.jpg',1,1,'李婉','2026-10-17 15:30:00','出库检查完成'),
(10,31,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/07.jpg',1,1,'李婉','2026-10-24 11:30:00','归还验收通过'),
(11,32,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/10.jpg',1,1,'郭程','2026-10-20 15:31:00','出库检查完成'),
(12,32,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/10.jpg',1,1,'郭程','2026-10-27 11:31:00','归还验收通过'),
(13,33,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/01.jpg',1,1,'李婉','2026-10-23 15:32:00','出库检查完成'),
(14,33,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/01.jpg',1,1,'李婉','2026-10-30 11:32:00','归还验收通过'),
(15,34,'OUT','良好，无新增磕碰','各部位正常，音准 A440','已核对一致','琴包、背带、调音器','底部 2cm 旧划痕（已拍照）','/uploads/seed/instrument/04.jpg',1,1,'郭程','2026-10-26 15:33:00','出库检查完成'),
(16,34,'RETURN','轻微使用痕迹','功能正常','已核对一致','齐全','无新增划痕','/uploads/seed/instrument/04.jpg',1,1,'郭程','2026-11-02 11:33:00','归还验收通过');

INSERT INTO renewal_application (id,order_id,old_end_date,new_end_date,extra_rent,conflict_flag,conflict_reason,status,apply_reason,apply_time,audit_time,auditor,audit_remark) VALUES
(1,4,'2026-08-20','2026-09-05',608.0,0,'','APPROVED','考级延期','2026-10-20 10:01:00','2026-10-20 15:01:00','李婉','同意，已补收租金'),
(2,11,'2026-09-12','2026-09-30',1008.0,0,'','APPROVED','演出加场','2026-10-20 10:02:00','2026-10-20 15:02:00','李婉','同意，已补收租金'),
(3,15,'2026-10-02','2026-10-20',1008.0,1,'该乐器 10-05 起已有预约，时段冲突','REJECTED','课程延长','2026-10-20 10:03:00','2026-10-20 15:03:00','李婉','冲突未通过，建议改约其他乐器'),
(4,20,'2026-10-25','2026-11-10',896.0,0,'','PENDING','孩子想多练一段','2026-10-20 10:04:00',NULL,'李婉','待营业员核对档期'),
(5,25,'2026-11-08','2026-11-20',672.0,1,'存在未处理故障报修（RP-20261028-0011）','PENDING','搬家期间继续使用','2026-10-20 10:05:00',NULL,'李婉','待故障处理后再定');

INSERT INTO return_inspection (id,order_id,function_check,appearance_check,accessory_check,clean_check,overdue_days,overdue_fee,result,damage_desc,photos,inspector,inspect_time) VALUES
(1,27,'正常','正常','齐全','已清洁',0,0.0,'PASS','','/uploads/seed/instrument/11.jpg','李婉','2026-10-14 11:26:00'),
(2,27,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/11.jpg','李婉','2026-10-09 10:26:00'),
(3,28,'正常','正常','齐全','已清洁',2,84.0,'DAMAGE','军鼓网皮凹陷 3cm，已协商赔偿','/uploads/seed/instrument/04.jpg','郭程','2026-10-17 11:27:00'),
(4,28,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/04.jpg','郭程','2026-10-12 10:27:00'),
(5,29,'正常','正常','齐全','已清洁',0,0.0,'PASS','','/uploads/seed/instrument/09.jpg','李婉','2026-10-20 11:28:00'),
(6,29,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/09.jpg','李婉','2026-10-15 10:28:00'),
(7,30,'正常','正常','齐全','已清洁',0,0.0,'PASS','','/uploads/seed/instrument/02.jpg','郭程','2026-10-23 11:29:00'),
(8,30,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/02.jpg','郭程','2026-10-18 10:29:00'),
(9,31,'正常','正常','齐全','已清洁',2,84.0,'PASS','','/uploads/seed/instrument/07.jpg','李婉','2026-10-26 11:30:00'),
(10,31,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/07.jpg','李婉','2026-10-21 10:30:00'),
(11,32,'正常','正常','齐全','已清洁',0,0.0,'PASS','','/uploads/seed/instrument/12.jpg','郭程','2026-10-29 11:31:00'),
(12,32,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/12.jpg','郭程','2026-10-24 10:31:00'),
(13,33,'正常','正常','齐全','已清洁',0,0.0,'PASS','','/uploads/seed/instrument/05.jpg','李婉','2026-11-01 11:32:00'),
(14,33,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/05.jpg','李婉','2026-10-27 10:32:00'),
(15,34,'正常','正常','齐全','已清洁',2,84.0,'PASS','','/uploads/seed/instrument/10.jpg','郭程','2026-11-04 11:33:00'),
(16,34,'正常','正常','齐全','需清洁',5,180.00,'PASS','','/uploads/seed/instrument/10.jpg','郭程','2026-10-30 10:33:00');

INSERT INTO damage_claim (id,order_id,damage_point,missing_accessory,repair_cost,liability,conclusion,amount,pay_status,pay_proof,operator,create_time) VALUES
(1,8,'军鼓网皮凹陷 3cm','鼓槌（1 副）',260.00,'客户责任','客户确认按成本价赔偿，已线下收取',260.00,'PAID','/uploads/seed/pay/01.jpg','李婉','2026-09-30 18:40:00'),
(2,17,'琴箱合页松动','无',80.00,'自然损耗','自然损耗，门店承担维修',0.00,'PAID','','郭程','2026-10-12 16:20:00');

INSERT INTO repair_order (id,repair_no,instrument_id,order_id,fault_desc,fault_images,diagnosis,estimated_cost,actual_cost,
handle_desc,test_result,status,technician_id,technician_name,source,remark,create_time,finish_time,accept_time) VALUES
(1,'RP-20261028-0011',3,25,'琴码前倾、D 弦按弦有杂音','/uploads/seed/repair/03.jpg','琴码前倾 3mm，弦枕毛刺',180,165,'重新修整琴码角度并打磨弦枕','各弦音准稳定，无杂音','TESTING',4,'周晗','RENTAL',NULL,'2026-09-17 09:01:00',NULL,NULL),
(2,'RP-20261025-0009',4,NULL,'按键漏气、皮垫老化','/uploads/seed/repair/01.jpg','上管三处皮垫老化漏气',420,0,NULL,NULL,'PENDING',NULL,NULL,'STORE',NULL,'2026-09-24 09:02:00',NULL,NULL),
(3,'RP-20261012-0006',5,8,'网皮凹陷、触发不灵敏','/uploads/seed/repair/03.jpg','军鼓网皮破损，触发器接触不良',260,260,'更换网皮并调整触发器','各鼓面触发正常','CLOSED',5,'孙立','RENTAL',NULL,'2026-10-01 09:03:00','2026-10-02 16:03:00','2026-10-03 10:03:00'),
(4,'RP-20261005-0004',1,6,'琴颈轻微变形、弦距偏高','/uploads/seed/repair/01.jpg','琴颈曲度 0.4mm，需调整钢筋',150,150,'调整钢筋并重新设定弦距','手感恢复，音准正常','CLOSED',4,'周晗','STORE',NULL,'2026-10-08 09:04:00','2026-10-09 16:04:00','2026-10-10 10:04:00'),
(5,'RP-20260920-0002',14,NULL,'鼓皮老化、镲片松动','/uploads/seed/repair/03.jpg','鼓皮自然老化，支架螺丝松动',220,210,'更换鼓皮、紧固支架','试奏正常','COMPLETED',5,'孙立','STORE',NULL,'2026-10-15 09:05:00','2026-10-16 16:05:00',NULL),
(6,'RP-20260910-0001',8,NULL,'按键粘连、风箱漏气','/uploads/seed/repair/01.jpg','风箱密封条老化',360,380,'更换密封条并调校按键','风箱气密性正常','CLOSED',4,'周晗','STORE',NULL,'2026-10-22 09:06:00','2026-10-23 16:06:00','2026-10-24 10:06:00'),
(7,'RP-20261101-0012',20,33,'吹奏有异音','/uploads/seed/repair/03.jpg',NULL,0,0,NULL,NULL,'DIAGNOSING',4,'周晗','RENTAL',NULL,'2026-10-29 09:07:00',NULL,NULL);

INSERT INTO repair_part (id,repair_id,part_name,quantity,unit_price,amount,create_time) VALUES
(1,1,'琴码',1,60,60,'2026-09-15 10:00:00'),
(2,1,'D 弦',1,45,45,'2026-09-15 10:00:00'),
(3,3,'网皮',1,160,160,'2026-09-15 10:00:00'),
(4,4,'琴弦套装',1,65,65,'2026-09-15 10:00:00'),
(5,5,'鼓皮',1,150,150,'2026-09-15 10:00:00'),
(6,6,'密封条',2,90,180,'2026-09-15 10:00:00');

INSERT INTO maintain_plan (id,instrument_id,plan_date,cycle_days,content,status,finish_date,executor,remark) VALUES
(1,1,'2026-11-10',90,'更换易损件','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(2,2,'2026-11-19',180,'全面检测与校音','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(3,3,'2026-11-28',90,'防潮保养','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(4,4,'2026-12-07',180,'结构紧固','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(5,5,'2026-12-16',90,'清洁与调音','DUE',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(6,6,'2026-12-25',180,'更换易损件','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(7,7,'2027-01-03',90,'全面检测与校音','DONE','2027-01-02','周晗','按周期生成，避开租赁与维修时段'),
(8,8,'2027-01-12',180,'防潮保养','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(9,9,'2027-01-21',90,'结构紧固','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(10,10,'2027-01-30',180,'清洁与调音','DUE',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(11,11,'2027-02-08',90,'更换易损件','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(12,12,'2027-02-17',180,'全面检测与校音','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(13,13,'2027-02-26',90,'防潮保养','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(14,14,'2026-11-07',180,'结构紧固','DONE','2026-11-06','孙立','按周期生成，避开租赁与维修时段'),
(15,15,'2026-11-16',90,'清洁与调音','DUE',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(16,16,'2026-11-25',180,'更换易损件','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(17,17,'2026-12-04',90,'全面检测与校音','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(18,18,'2026-12-13',180,'防潮保养','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(19,19,'2026-12-22',90,'结构紧固','PLAN',NULL,'周晗','按周期生成，避开租赁与维修时段'),
(20,20,'2026-12-31',180,'清洁与调音','DUE',NULL,'孙立','按周期生成，避开租赁与维修时段'),
(21,21,'2027-01-09',90,'更换易损件','DONE','2027-01-08','周晗','按周期生成，避开租赁与维修时段'),
(22,22,'2027-01-18',180,'全面检测与校音','PLAN',NULL,'孙立','按周期生成，避开租赁与维修时段');

INSERT INTO customer_feedback (id,order_id,instrument_id,customer_id,rating,content,create_time) VALUES
(1,27,21,8,4,'包装到位，配送师傅专业','2026-10-03 19:26:00'),
(2,28,4,9,5,'续租流程顺畅','2026-10-06 19:27:00'),
(3,29,9,6,4,'琴况很好，交接讲解细致','2026-10-09 19:28:00'),
(4,30,14,7,5,'音准稳定，门店响应快','2026-10-12 19:29:00'),
(5,31,19,8,4,'包装到位，配送师傅专业','2026-10-15 19:30:00'),
(6,32,2,9,5,'续租流程顺畅','2026-10-18 19:31:00'),
(7,33,7,6,4,'琴况很好，交接讲解细致','2026-10-21 19:32:00'),
(8,34,12,7,5,'音准稳定，门店响应快','2026-10-24 19:33:00');

INSERT INTO score_rule (id,version_no,weight_appearance,weight_pitch,weight_part,weight_rent_days,weight_repair_count,weight_maintain,threshold,enabled,effect_time,remark,create_time) VALUES
(1,'v1',20,25,20,15,10,10,60,0,'2026-05-06 09:00:00','初始规则','2026-05-06 09:00:00'),
(2,'v2',20,25,20,15,10,10,65,0,'2026-08-11 09:00:00','阈值提高到 65','2026-08-11 09:00:00'),
(3,'v3',20,25,20,15,10,10,60,1,'2026-10-20 09:00:00','部件状态权重提高至 20%','2026-10-20 09:00:00');

INSERT INTO score_record (id,instrument_id,rule_id,rule_version,appearance_input,pitch_input,part_input,rent_days_input,
repair_count_input,maintain_days_input,detail_json,total_score,level,recommend,main_factors,unmet,improve,
manual_result,adjust_reason,adjust_by,adjust_time,create_time) VALUES
(1,1,3,'v3',92,82,88,186,1,92,'{"appearance":{"input":92,"weight":0.2,"score":92},"pitch":{"input":82,"weight":0.25,"score":82},"part":{"input":88,"weight":0.2,"score":88},"rentDays":{"input":186,"weight":0.15,"score":69.0},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":92,"weight":0.1,"score":54.0}}',80.8,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-21 09:01:00'),
(2,2,3,'v3',150,78,90,420,2,150,'{"appearance":{"input":150,"weight":0.2,"score":150},"pitch":{"input":78,"weight":0.25,"score":78},"part":{"input":90,"weight":0.2,"score":90},"rentDays":{"input":420,"weight":0.15,"score":30.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":150,"weight":0.1,"score":25.0}}',81.5,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-22 09:02:00'),
(3,3,3,'v3',92,74,86,268,3,92,'{"appearance":{"input":92,"weight":0.2,"score":92},"pitch":{"input":74,"weight":0.25,"score":74},"part":{"input":86,"weight":0.2,"score":86},"rentDays":{"input":268,"weight":0.15,"score":55.3},"repairCount":{"input":3,"weight":0.1,"score":55},"maintainGap":{"input":92,"weight":0.1,"score":54.0}}',73.3,'MAINTAIN','建议保养后再租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-23 09:03:00'),
(4,4,3,'v3',60,80,72,152,2,60,'{"appearance":{"input":60,"weight":0.2,"score":60},"pitch":{"input":80,"weight":0.25,"score":80},"part":{"input":72,"weight":0.2,"score":72},"rentDays":{"input":152,"weight":0.15,"score":74.7},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":60,"weight":0.1,"score":70.0}}',71.6,'MAINTAIN','建议保养后再租','音准评分、部件状态','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-24 09:04:00'),
(5,5,3,'v3',88,84,80,210,2,88,'{"appearance":{"input":88,"weight":0.2,"score":88},"pitch":{"input":84,"weight":0.25,"score":84},"part":{"input":80,"weight":0.2,"score":80},"rentDays":{"input":210,"weight":0.15,"score":65.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":88,"weight":0.1,"score":56.0}}',76.9,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-25 09:05:00'),
(6,6,3,'v3',120,76,84,240,1,120,'{"appearance":{"input":120,"weight":0.2,"score":120},"pitch":{"input":76,"weight":0.25,"score":76},"part":{"input":84,"weight":0.2,"score":84},"rentDays":{"input":240,"weight":0.15,"score":60.0},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":120,"weight":0.1,"score":40.0}}',81.3,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-26 09:06:00'),
(7,7,3,'v3',45,88,90,96,0,45,'{"appearance":{"input":45,"weight":0.2,"score":45},"pitch":{"input":88,"weight":0.25,"score":88},"part":{"input":90,"weight":0.2,"score":90},"rentDays":{"input":96,"weight":0.15,"score":84.0},"repairCount":{"input":0,"weight":0.1,"score":100},"maintainGap":{"input":45,"weight":0.1,"score":77.5}}',79.3,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-27 09:07:00'),
(8,8,3,'v3',100,72,82,312,2,100,'{"appearance":{"input":100,"weight":0.2,"score":100},"pitch":{"input":72,"weight":0.25,"score":72},"part":{"input":82,"weight":0.2,"score":82},"rentDays":{"input":312,"weight":0.15,"score":48.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":100,"weight":0.1,"score":50.0}}',73.6,'MAINTAIN','建议保养后再租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-20 09:08:00'),
(9,9,3,'v3',75,86,88,128,1,75,'{"appearance":{"input":75,"weight":0.2,"score":75},"pitch":{"input":86,"weight":0.25,"score":86},"part":{"input":88,"weight":0.2,"score":88},"rentDays":{"input":128,"weight":0.15,"score":78.7},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":75,"weight":0.1,"score":62.5}}',80.7,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-21 09:09:00'),
(10,10,3,'v3',130,78,80,176,1,130,'{"appearance":{"input":130,"weight":0.2,"score":130},"pitch":{"input":78,"weight":0.25,"score":78},"part":{"input":80,"weight":0.2,"score":80},"rentDays":{"input":176,"weight":0.15,"score":70.7},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":130,"weight":0.1,"score":35.0}}',84.1,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-22 09:10:00'),
(11,11,3,'v3',110,84,88,264,1,110,'{"appearance":{"input":110,"weight":0.2,"score":110},"pitch":{"input":84,"weight":0.25,"score":84},"part":{"input":88,"weight":0.2,"score":88},"rentDays":{"input":264,"weight":0.15,"score":56.0},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":110,"weight":0.1,"score":45.0}}',82.0,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-23 09:11:00'),
(12,12,3,'v3',60,90,92,88,0,60,'{"appearance":{"input":60,"weight":0.2,"score":60},"pitch":{"input":90,"weight":0.25,"score":90},"part":{"input":92,"weight":0.2,"score":92},"rentDays":{"input":88,"weight":0.15,"score":85.3},"repairCount":{"input":0,"weight":0.1,"score":100},"maintainGap":{"input":60,"weight":0.1,"score":70.0}}',82.7,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-24 09:12:00'),
(13,13,3,'v3',160,68,76,340,3,160,'{"appearance":{"input":160,"weight":0.2,"score":160},"pitch":{"input":68,"weight":0.25,"score":68},"part":{"input":76,"weight":0.2,"score":76},"rentDays":{"input":340,"weight":0.15,"score":43.3},"repairCount":{"input":3,"weight":0.1,"score":55},"maintainGap":{"input":160,"weight":0.1,"score":20.0}}',78.2,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-25 09:13:00'),
(14,14,3,'v3',95,74,78,198,2,95,'{"appearance":{"input":95,"weight":0.2,"score":95},"pitch":{"input":74,"weight":0.25,"score":74},"part":{"input":78,"weight":0.2,"score":78},"rentDays":{"input":198,"weight":0.15,"score":67.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":95,"weight":0.1,"score":52.5}}',75.4,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-26 09:14:00'),
(15,15,3,'v3',70,82,84,156,1,70,'{"appearance":{"input":70,"weight":0.2,"score":70},"pitch":{"input":82,"weight":0.25,"score":82},"part":{"input":84,"weight":0.2,"score":84},"rentDays":{"input":156,"weight":0.15,"score":74.0},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":70,"weight":0.1,"score":65.0}}',77.4,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-27 09:15:00'),
(16,16,3,'v3',120,76,80,288,2,120,'{"appearance":{"input":120,"weight":0.2,"score":120},"pitch":{"input":76,"weight":0.25,"score":76},"part":{"input":80,"weight":0.2,"score":80},"rentDays":{"input":288,"weight":0.15,"score":52.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":120,"weight":0.1,"score":40.0}}',77.8,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-20 09:16:00'),
(17,17,3,'v3',80,84,86,142,1,80,'{"appearance":{"input":80,"weight":0.2,"score":80},"pitch":{"input":84,"weight":0.25,"score":84},"part":{"input":86,"weight":0.2,"score":86},"rentDays":{"input":142,"weight":0.15,"score":76.3},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":80,"weight":0.1,"score":60.0}}',80.2,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-21 09:17:00'),
(18,18,3,'v3',105,78,82,168,1,105,'{"appearance":{"input":105,"weight":0.2,"score":105},"pitch":{"input":78,"weight":0.25,"score":78},"part":{"input":82,"weight":0.2,"score":82},"rentDays":{"input":168,"weight":0.15,"score":72.0},"repairCount":{"input":1,"weight":0.1,"score":85},"maintainGap":{"input":105,"weight":0.1,"score":47.5}}',81.0,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-22 09:18:00'),
(19,19,3,'v3',118,74,80,204,2,118,'{"appearance":{"input":118,"weight":0.2,"score":118},"pitch":{"input":74,"weight":0.25,"score":74},"part":{"input":80,"weight":0.2,"score":80},"rentDays":{"input":204,"weight":0.15,"score":66.0},"repairCount":{"input":2,"weight":0.1,"score":70},"maintainGap":{"input":118,"weight":0.1,"score":41.0}}',79.1,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-23 09:19:00'),
(20,20,3,'v3',200,62,68,366,4,200,'{"appearance":{"input":200,"weight":0.2,"score":200},"pitch":{"input":62,"weight":0.25,"score":62},"part":{"input":68,"weight":0.2,"score":68},"rentDays":{"input":366,"weight":0.15,"score":39.0},"repairCount":{"input":4,"weight":0.1,"score":40},"maintainGap":{"input":200,"weight":0.1,"score":0}}',78.9,'OBSERVE','建议出租','音准评分、部件状态','','安排一次全面保养可提升约 4 分','MAINTAIN','门店复核发现风箱漏气，人工下调为待保养并停租','门店管理员','2026-10-29 09:20:00','2026-10-24 09:20:00'),
(21,21,3,'v3',50,88,92,120,0,50,'{"appearance":{"input":50,"weight":0.2,"score":50},"pitch":{"input":88,"weight":0.25,"score":88},"part":{"input":92,"weight":0.2,"score":92},"rentDays":{"input":120,"weight":0.15,"score":80.0},"repairCount":{"input":0,"weight":0.1,"score":100},"maintainGap":{"input":50,"weight":0.1,"score":75.0}}',79.9,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-11-25 09:21:00'),
(22,22,3,'v3',55,86,88,96,0,55,'{"appearance":{"input":55,"weight":0.2,"score":55},"pitch":{"input":86,"weight":0.25,"score":86},"part":{"input":88,"weight":0.2,"score":88},"rentDays":{"input":96,"weight":0.15,"score":84.0},"repairCount":{"input":0,"weight":0.1,"score":100},"maintainGap":{"input":55,"weight":0.1,"score":72.5}}',80.0,'OBSERVE','建议出租','外观评分、累计租期','','安排一次全面保养可提升约 4 分','','','',NULL,'2026-10-26 09:22:00');

INSERT INTO sys_message (id,user_id,title,content,biz_no,biz_type,link,is_read,create_time) VALUES
(1,2,'待审批：新租赁预约','民谣吉他 INS-2026-0011 申请 11-05 起租','RO-20261101-0001','RENTAL','/order-desk',0,'2026-11-01 09:12:00'),
(2,2,'待登记押金','订单已审核通过，押金 ¥6,000 未登记','RO-20261030-0002','RENTAL','/order-desk',0,'2026-10-30 17:20:00'),
(3,4,'待接单：萨克斯维修','中音萨克斯按键漏气，等待技师接单','RP-20261025-0009','REPAIR','/repair',0,'2026-10-25 14:02:00'),
(4,4,'待试奏检验','小提琴维修已完成，请试奏并提交验收','RP-20261028-0011','REPAIR','/repair',0,'2026-10-29 09:20:00'),
(5,6,'续租被拒','该乐器 10-05 起已有预约，时段冲突','RO-20260912-0015','RENTAL','/my-orders',1,'2026-10-20 15:10:00'),
(6,6,'归还提醒','租期将于 3 天后到期，请按时归还或申请续租','RO-20261018-0004','RENTAL','/my-orders',0,'2026-11-01 08:00:00'),
(7,1,'低分乐器需保养','电子鼓状态分 68，低于阈值，已生成保养建议','INS-2026-0015','SCORE','/score-rule',0,'2026-10-29 09:30:00'),
(8,1,'逾期订单提醒','订单已逾期 5 天，请跟进客户','RO-20260930-0008','RENTAL','/order-desk',1,'2026-10-05 09:00:00');

INSERT INTO sys_log (id,user_id,username,module,action,biz_no,content,create_time) VALUES
(1,2,'clerk01','租赁订单','审核通过','RO-20261030-0002','审核通过，生成应收押金 ¥6,000.00','2026-10-30 16:05:00'),
(2,2,'clerk01','出库交接','完成交接','RO-20261029-0003','外观/功能/配件核对一致，客户已签字','2026-10-29 11:40:00'),
(3,4,'tech01','维修工单','完成维修','RP-20261028-0011','更换琴码、打磨弦枕，待试奏','2026-10-29 09:20:00'),
(4,1,'admin','评分规则','发布新版本','v3','部件状态权重 15%→20%，阈值 60','2026-10-20 09:00:00');

-- 演示数据时间分布修正：让近 10 天租赁单量呈分散趋势
update rental_order set create_time=date_sub(now(), interval 1 day) where id=34;
update rental_order set create_time=date_sub(now(), interval 2 day) where id=33;
update rental_order set create_time=date_sub(now(), interval 3 day) where id=32;
update rental_order set create_time=date_sub(now(), interval 5 day) where id=31;
update rental_order set create_time=date_sub(now(), interval 6 day) where id=30;
update rental_order set create_time=date_sub(now(), interval 8 day) where id=29;
