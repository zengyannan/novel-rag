package com.novel.backend.crawler.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 站点解析器注册表
 * 管理所有站点解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SiteParserRegistry {

    private final List<SiteParser> parsers;

    /**
     * 根据URL获取合适的解析器
     *
     * @param url 目标URL
     * @return 解析器（如果没有匹配的，返回通用解析器）
     */
    public SiteParser getParser(String url) {
        return parsers.stream()
            .filter(parser -> parser.supports(url))
            .findFirst()
            .orElseGet(() -> {
                log.warn("没有找到支持URL的解析器: {}，使用通用解析器", url);
                return getGenericParser();
            });
    }

    /**
     * 根据站点ID获取解析器
     *
     * @param siteId 站点ID
     * @return 解析器
     */
    public Optional<SiteParser> getParserById(String siteId) {
        return parsers.stream()
            .filter(parser -> parser.getSiteId().equals(siteId))
            .findFirst();
    }

    /**
     * 获取所有支持的站点
     */
    public List<String> getSupportedSites() {
        return parsers.stream()
            .filter(parser -> !"generic".equals(parser.getSiteId()))
            .map(SiteParser::getSiteName)
            .toList();
    }

    /**
     * 获取通用解析器
     */
    private SiteParser getGenericParser() {
        return parsers.stream()
            .filter(parser -> "generic".equals(parser.getSiteId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("没有找到通用解析器"));
    }
}
