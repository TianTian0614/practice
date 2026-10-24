package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.dto.LoginRequest;
import com.agriassist.dto.LoginResponse;
import com.agriassist.dto.RegisterRequest;
import com.agriassist.dto.UserPublicVO;
import com.agriassist.entity.Certification;
import com.agriassist.entity.User;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.CertificationRepository;
import com.agriassist.repository.UserRepository;
import com.agriassist.service.CertificationService;
import com.agriassist.service.PointService;
import com.agriassist.service.UserService;
import com.agriassist.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CertificationRepository certRepo;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                           CertificationRepository certRepo) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.certRepo = certRepo;
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getRole());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setRole(user.getRole());
        resp.setNickname(user.getNickname());
        return resp;
    }

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        // 角色合并：注册统一为普通用户(USER)，管理员仅由种子数据创建
        user.setRole("USER");
        user.setNickname(req.getNickname());
        user.setContact(req.getContact());
        user.setRegion(req.getRegion());
        userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    }

    @Override
    public UserPublicVO getPublicById(Long id, boolean exposeContact) {
        User u = getById(id);
        UserPublicVO v = toVO(u);
        if (exposeContact && u.getContact() != null) {
            v.setContact(maskContact(u.getContact()));
        } else {
            v.setContact(null);
        }
        return v;
    }

    private String maskContact(String contact) {
        if (contact == null) return null;
        // 11 位手机号脱敏：138****1234
        if (contact.matches("\\d{11}")) {
            return contact.substring(0, 3) + "****" + contact.substring(7);
        }
        // 其他联系方式（座机/微信等）保留首尾
        if (contact.length() > 6) {
            return contact.substring(0, 3) + "****" + contact.substring(contact.length() - 2);
        }
        return contact;
    }

    @Override
    public UserPublicVO updateProfile(Long uid, String nickname, String contact, String region) {
        User u = getById(uid);
        if (nickname != null) u.setNickname(nickname);
        if (contact != null) u.setContact(contact);
        if (region != null) u.setRegion(region);
        userRepository.save(u);
        return toVO(u);
    }

    private UserPublicVO toVO(User u) {
        UserPublicVO v = new UserPublicVO();
        v.setId(u.getId());
        v.setUsername(u.getUsername());
        v.setNickname(u.getNickname());
        v.setRole(u.getRole());
        v.setRegion(u.getRegion());
        v.setContact(u.getContact());

        // 认证徽章：仅展示已通过的认证
        List<String> badges = new ArrayList<>();
        if (u.getId() != null) {
            for (Certification c : certRepo.findByUserIdAndStatus(u.getId(), "APPROVED")) {
                badges.add(CertificationService.labelOf(c.getType()));
            }
        }
        v.setBadges(badges);

        int p = u.getPoints() == null ? 0 : u.getPoints();
        v.setPoints(p);
        v.setLevelName(PointService.levelName(p));
        return v;
    }
}
