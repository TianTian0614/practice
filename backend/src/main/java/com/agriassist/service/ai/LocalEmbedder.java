package com.agriassist.service.ai;

import org.springframework.stereotype.Component;

/**
 * 本地轻量文本向量化器（无模型、无依赖，纯 Java 计算）。
 * 采用字符 n-gram + 哈希技巧（hashing trick）将文本映射为固定维度向量，
 * 用于中文农技文本的相似度检索。向量维度固定为 DIM，检索时做 L2 归一化后余弦相似度。
 *
 * 说明：该向量仅用于“本地知识库检索”，不依赖任何外部大模型，因此零内存/显存占用。
 */
@Component
public class LocalEmbedder {

    private static final int DIM = 512;

    /** 将文本转换为 DIM 维 L2 归一化向量 */
    public double[] embed(String text) {
        double[] vec = new double[DIM];
        String t = normalize(text);
        if (t.isEmpty()) {
            return vec;
        }
        // 一元、二元、三元字符片段，兼顾中文分词效果
        for (int i = 0; i < t.length(); i++) {
            add(vec, t.substring(i, i + 1));
        }
        for (int i = 0; i + 1 < t.length(); i++) {
            add(vec, t.substring(i, i + 2));
        }
        for (int i = 0; i + 2 < t.length(); i++) {
            add(vec, t.substring(i, i + 3));
        }
        normalize(vec);
        return vec;
    }

    private void add(double[] v, String gram) {
        int idx = Math.floorMod(gram.hashCode(), DIM);
        v[idx] += 1.0;
    }

    private void normalize(double[] v) {
        double n = 0;
        for (double x : v) {
            n += x * x;
        }
        n = Math.sqrt(n);
        if (n > 0) {
            for (int i = 0; i < v.length; i++) {
                v[i] /= n;
            }
        }
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        // 去除标点、符号与空白，保留中英文字符与数字
        return s.toLowerCase().replaceAll("[\\p{P}\\p{S}\\s]+", "");
    }
}
