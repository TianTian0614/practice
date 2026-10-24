package com.agriassist.service;

import java.util.List;
import java.util.Map;

public interface FavoriteService {
    boolean toggle(Long userId, String targetType, Long targetId);
    boolean isFav(Long userId, String targetType, Long targetId);
    List<Map<String, Object>> list(Long userId, String targetType);
}
