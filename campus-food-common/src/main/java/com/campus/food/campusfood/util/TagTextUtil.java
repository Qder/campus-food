package com.campus.food.campusfood.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 逗号分隔标签的解析与匹配（口味、食材等）。
 *
 * @author yang
 * @since 2026-10-16
 */
public final class TagTextUtil {

    private TagTextUtil() {
    }

    /**
     * 解析逗号分隔的标签列表。
     *
     * @param raw 原始文本，可为 null 或空
     * @return 去首尾空白后的标签列表；无有效标签时返回空列表
     */
    public static List<String> parseTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String[] parts = raw.split(",");
        List<String> tags = new ArrayList<>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) {
                tags.add(t);
            }
        }
        return tags;
    }

    /**
     * 解析逗号分隔的标签集合（去重）。
     *
     * @param raw 原始文本，可为 null 或空
     * @return 去重后的标签集合；无有效标签时返回空集合
     */
    public static Set<String> parseTagSet(String raw) {
        List<String> list = parseTags(raw);
        if (list.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(list);
    }

    /**
     * 合并多段标签文本为去重集合（保持插入顺序）。
     *
     * @param raws 多段原始文本，可为 null
     * @return 合并去重后的标签集合；无有效标签时返回空集合
     */
    public static Set<String> mergeTagSets(String... raws) {
        if (raws == null || raws.length == 0) {
            return Set.of();
        }
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (String raw : raws) {
            merged.addAll(parseTagSet(raw));
        }
        return merged.isEmpty() ? Set.of() : merged;
    }

    /**
     * 判断菜品食材是否包含任一忌口标签（忽略大小写）。
     *
     * @param haystack 菜品食材标签集合
     * @param needles  用户忌口标签集合
     * @return 命中任一忌口返回 true
     */
    public static boolean containsAnyTag(Set<String> haystack, Set<String> needles) {
        if (haystack.isEmpty() || needles.isEmpty()) {
            return false;
        }
        Set<String> normalizedHay = new LinkedHashSet<>();
        for (String h : haystack) {
            normalizedHay.add(h.toLowerCase(Locale.ROOT));
        }
        for (String needle : needles) {
            if (normalizedHay.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 求用户口味标签在菜品标签集中的命中列表（忽略大小写，保持用户标签顺序）。
     *
     * @param userTastes 用户口味标签列表
     * @param foodTags   菜品标签集合
     * @return 命中的用户标签；无命中时返回空列表
     */
    public static List<String> matchedTags(List<String> userTastes, Set<String> foodTags) {
        if (userTastes.isEmpty() || foodTags.isEmpty()) {
            return List.of();
        }
        Set<String> normalizedFood = new LinkedHashSet<>();
        for (String tag : foodTags) {
            normalizedFood.add(tag.toLowerCase(Locale.ROOT));
        }
        List<String> hits = new ArrayList<>();
        for (String taste : userTastes) {
            if (normalizedFood.contains(taste.toLowerCase(Locale.ROOT))) {
                hits.add(taste);
            }
        }
        return hits;
    }

    /**
     * 将标签列表拼接为逗号分隔的字符串。
     *
     * @param tags 标签列表，可为 null
     * @return 逗号分隔串；列表为空时返回空串
     */
    public static String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }
}
