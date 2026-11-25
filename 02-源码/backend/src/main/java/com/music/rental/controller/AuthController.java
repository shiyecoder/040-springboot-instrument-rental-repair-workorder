package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.rental.common.BizException;
import com.music.rental.common.CurrentUser;
import com.music.rental.common.R;
import com.music.rental.config.JwtUtil;
import com.music.rental.entity.SysMenu;
import com.music.rental.entity.SysRoleMenu;
import com.music.rental.entity.SysUser;
import com.music.rental.mapper.SysMenuMapper;
import com.music.rental.mapper.SysRoleMenuMapper;
import com.music.rental.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** 登录、会话、个人中心。密码统一 BCrypt，任何返回体都不含 password。 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    /** 登录失败计数：连续 5 次锁定 10 分钟（内存实现，单机足够） */
    private final Map<String, int[]> fails = new ConcurrentHashMap<>();
    private final Map<String, Long> lockUntil = new ConcurrentHashMap<>();

    public AuthController(SysUserMapper userMapper, SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper,
                          PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/health")
    public R<String> health() {
        return R.ok("ok");
    }

    @PostMapping("/auth/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = Optional.ofNullable(body.get("username")).orElse("").trim();
        String password = Optional.ofNullable(body.get("password")).orElse("");
        if (username.isEmpty() || password.isEmpty()) throw new BizException("请输入账号和密码");

        Long lock = lockUntil.get(username);
        if (lock != null && lock > System.currentTimeMillis()) {
            long min = (lock - System.currentTimeMillis()) / 60000 + 1;
            throw new BizException("连续登录失败，账号已锁定，请 " + min + " 分钟后再试");
        }

        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("username", username).last("limit 1"));
        // 兼容重新导入 init.sql 后的明文初始口令：校验通过即就地加密
        boolean pwdOk = user != null && (user.getPassword() != null && user.getPassword().startsWith("$2")
                ? encoder.matches(password, user.getPassword()) : password.equals(user.getPassword()));
        if (user != null && pwdOk && !user.getPassword().startsWith("$2")) {
            user.setPassword(encoder.encode(password));
            userMapper.updateById(user);
        }
        if (user == null || !pwdOk) {
            int n = fails.computeIfAbsent(username, k -> new int[]{0})[0] + 1;
            fails.get(username)[0] = n;
            if (n >= 5) {
                lockUntil.put(username, System.currentTimeMillis() + 10 * 60_000L);
                fails.get(username)[0] = 0;
                throw new BizException("连续失败 5 次，账号锁定 10 分钟");
            }
            throw new BizException("账号或密码不正确，还可尝试 " + (5 - n) + " 次");
        }
        if (user.getStatus() != null && user.getStatus() == 0) throw new BizException("账号已停用，请联系管理员");

        fails.remove(username);
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRoleCode());

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("user", safe(user));
        return R.ok(res);
    }

    @GetMapping("/auth/me")
    public R<Map<String, Object>> me() {
        SysUser u = CurrentUser.get();
        Map<String, Object> res = new HashMap<>();
        res.put("user", safe(u));
        res.put("menus", menusOf(u.getRoleCode()));
        return R.ok(res);
    }

    /** 菜单按角色动态返回：先查角色关联，再按 sort 排序 */
    private List<Map<String, Object>> menusOf(String roleCode) {
        var role = roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>());
        long roleId = switch (roleCode == null ? "" : roleCode) {
            case "ADMIN" -> 1L;
            case "CLERK" -> 2L;
            case "TECHNICIAN" -> 3L;
            default -> 4L;
        };
        Set<Long> ids = new HashSet<>();
        for (SysRoleMenu rm : role) if (rm.getRoleId() == roleId) ids.add(rm.getMenuId());
        List<SysMenu> all = menuMapper.selectList(new QueryWrapper<SysMenu>().orderByAsc("sort"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysMenu m : all) {
            if (!ids.contains(m.getId())) continue;
            if ("菜单管理".equals(m.getMenuName())) continue; // 按需求隐藏菜单管理入口
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("id", m.getId());
            one.put("name", m.getMenuName());
            one.put("path", m.getPath());
            one.put("icon", m.getIcon());
            out.add(one);
        }
        return out;
    }

    @PutMapping("/auth/profile")
    public R<Void> profile(@RequestBody Map<String, String> body) {
        SysUser u = CurrentUser.get();
        if (body.containsKey("realName")) u.setRealName(body.get("realName"));
        if (body.containsKey("phone")) u.setPhone(body.get("phone"));
        if (body.containsKey("email")) u.setEmail(body.get("email"));
        userMapper.updateById(u);
        return R.ok();
    }

    @PostMapping("/auth/avatar")
    public R<String> avatar(@RequestBody Map<String, String> body) {
        String path = body.get("avatar");
        if (path == null || path.isBlank()) throw new BizException("头像地址不能为空");
        if (path.startsWith("http")) throw new BizException("头像必须通过上传组件上传，不允许外链");
        SysUser u = CurrentUser.get();
        u.setAvatar(path);
        userMapper.updateById(u);
        return R.ok(path);
    }

    @PostMapping("/auth/password")
    public R<Void> password(@RequestBody Map<String, String> body) {
        SysUser u = CurrentUser.get();
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (!encoder.matches(oldPwd == null ? "" : oldPwd, u.getPassword())) throw new BizException("原密码不正确");
        if (newPwd == null || newPwd.length() < 6) throw new BizException("新密码至少 6 位");
        u.setPassword(encoder.encode(newPwd));
        userMapper.updateById(u);
        return R.ok();
    }

    private Map<String, Object> safe(SysUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("realName", u.getRealName());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("avatar", u.getAvatar());
        m.put("roleCode", u.getRoleCode());
        m.put("storeName", u.getStoreName());
        m.put("lastLogin", u.getLastLogin());
        return m;
    }
}
