package com.agriassist.service;

import java.util.Map;

public interface RecommendService {
    Map<String, Object> recommend(Long userId, int limit);
}
