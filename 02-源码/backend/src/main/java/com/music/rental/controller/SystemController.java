package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.rental.common.BizException;
import com.music.rental.common.R;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/** 用户、角色、菜单、字典、消息、日志 */
@RestController
@RequestMapping("/api")
public class SystemController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysDictMapper dictMapper;
    private final SysMessageMapper messageMapper;
    private final SysLogMapper logMapper;
    private final PasswordEncoder encoder;

    public SystemController(SysUserMapper userMapper, SysRoleMapper roleMapper, SysMenuMapper menuMapper,
                            SysRoleMenuMapper roleMenuMapper, SysDictMapper dictMapper, SysMessageMapper messageMapper,
                            SysLogMapper logMapper, PasswordEncoder encoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.dictMapper = dictMapper;
        this.messageMapper = messageMapper;
        this.logMapper = logMapper;
        this.encoder = encoder;
    }

    @GetMapping("/user/page")
    public R<Map<String, Object>> userPage(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String roleCode,
                                           @RequestParam(required = false) String keyword) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (roleCode != null && !roleCode.isBlank() && !"全部".equals(roleCode)) qw.eq("role_code", roleCode);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("real_name", keyword).or().like("username", keyword).or().like("phone", keyword));
        }
        qw.orderByAsc("id");
        Page<SysUser> p = userMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        p.getRecords().forEach(u -> u.setPassword(null));
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", p.getRecords());
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return R.ok(res);
    }

    @PostMapping("/user")
    public R<Long> createUser(@RequestBody SysUser u) {
        if (u.getUsername() == null || u.getUsername().isBlank()) throw new BizException("账号必填");
        if (userMapper.selectCount(new QueryWrapper<SysUser>().eq("username", u.getUsername())) > 0) {
            throw new BizException("账号已存在");
        }
        u.setPassword(encoder.encode(u.getPassword() == null || u.getPassword().isBlank() ? "123456" : u.getPassword()));
        u.setStatus(u.getStatus() == null ? 1 : u.getStatus());
        u.setCreateTime(LocalDateTime.now());
        if (u.getAvatar() == null) u.setAvatar("/uploads/seed/avatar/01.jpg");
        userMapper.insert(u);
        return R.ok(u.getId());
    }

    @PutMapping("/user/{id}")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody SysUser u) {
        SysUser old = userMapper.selectById(id);
        if (old == null) throw new BizException("用户不存在");
        u.setId(id);
        u.setPassword(null); // 资料编辑不改密码
        userMapper.updateById(u);
        return R.ok();
    }

    @PostMapping("/user/{id}/reset")
    public R<Void> reset(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BizException("用户不存在");
        u.setPassword(encoder.encode("123456"));
        userMapper.updateById(u);
        return R.ok();
    }

    @DeleteMapping("/user/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u != null && "admin".equals(u.getUsername())) throw new BizException("默认管理员不可删除");
        userMapper.deleteById(id);
        return R.ok();
    }

    @GetMapping("/role/list")
    public R<List<Map<String, Object>>> roles() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysRole r : roleMapper.selectList(new QueryWrapper<SysRole>().orderByAsc("id"))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("roleCode", r.getRoleCode());
            m.put("roleName", r.getRoleName());
            m.put("dataScope", r.getDataScope());
            m.put("remark", r.getRemark());
            m.put("userCount", userMapper.selectCount(new QueryWrapper<SysUser>().eq("role_code", r.getRoleCode())));
            list.add(m);
        }
        return R.ok(list);
    }

    @GetMapping("/role/{id}/menus")
    public R<List<Long>> roleMenus(@PathVariable Long id) {
        List<Long> ids = new ArrayList<>();
        for (SysRoleMenu rm : roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().eq("role_id", id))) {
            ids.add(rm.getMenuId());
        }
        return R.ok(ids);
    }

    @PostMapping("/role/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleMenuMapper.delete(new QueryWrapper<SysRoleMenu>().eq("role_id", id));
        for (Long mid : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(id);
            rm.setMenuId(mid);
            roleMenuMapper.insert(rm);
        }
        return R.ok();
    }

    @GetMapping("/menu/tree")
    public R<List<Map<String, Object>>> menuTree() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysMenu m : menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"))) {
            if ("菜单管理".equals(m.getMenuName())) continue;
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("id", m.getId());
            one.put("name", m.getMenuName());
            one.put("path", m.getPath());
            one.put("icon", m.getIcon());
            one.put("sort", m.getSort());
            list.add(one);
        }
        return R.ok(list);
    }

    @PostMapping("/menu")
    public R<Long> saveMenu(@RequestBody SysMenu m) {
        if (m.getMenuName() == null || m.getMenuName().isBlank()) throw new BizException("菜单名称必填");
        if (m.getId() == null) {
            m.setParentId(0L);
            menuMapper.insert(m);
        } else {
            menuMapper.updateById(m);
        }
        return R.ok(m.getId());
    }

    @GetMapping("/dict/{type}")
    public R<List<SysDict>> dict(@PathVariable String type) {
        return R.ok(dictMapper.selectList(new QueryWrapper<SysDict>().eq("dict_type", type).orderByAsc("sort")));
    }

    @GetMapping("/message/page")
    public R<Map<String, Object>> messages(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        Long uid = com.music.rental.common.CurrentUser.id();
        Page<SysMessage> p = messageMapper.selectPage(new Page<>(pageNum, pageSize),
                new QueryWrapper<SysMessage>().eq("user_id", uid).orderByAsc("is_read").orderByDesc("create_time"));
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", p.getRecords());
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return R.ok(res);
    }

    @PostMapping("/message/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        SysMessage m = messageMapper.selectById(id);
        if (m != null) {
            m.setIsRead(1);
            messageMapper.updateById(m);
        }
        return R.ok();
    }

    @GetMapping("/log/page")
    public R<Map<String, Object>> logs(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        Page<SysLog> p = logMapper.selectPage(new Page<>(pageNum, pageSize), new QueryWrapper<SysLog>().orderByDesc("create_time"));
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", p.getRecords());
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return R.ok(res);
    }
}
