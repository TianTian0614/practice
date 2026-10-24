package com.agriassist.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPublicVO {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String region;
    private String contact;
    private List<String> badges;   // 已通过的认证徽章标签
    private Integer points;        // 社区激励积分
    private String levelName;      // 积分等级名称
}
