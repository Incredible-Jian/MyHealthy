SET NAMES utf8mb4;
INSERT INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon) VALUES(6020,'商城订单与支付',6000,20,'commerce','health/Commerce',1,1,'C','0','0','','shopping');
INSERT INTO sys_role_menu(role_id,menu_id) VALUES(102,6020);