package com.novel.backend.common.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 测试
 */
class ResultTest {

    @Test
    void testSuccessWithData() {
        // Given
        String data = "测试数据";

        // When
        Result<String> result = Result.success(data);

        // Then
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void testSuccessWithMessageAndData() {
        // Given
        String message = "操作成功";
        String data = "测试数据";

        // When
        Result<String> result = Result.success(message, data);

        // Then
        assertEquals(200, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void testFailWithCodeAndMessage() {
        // Given
        int code = 404;
        String message = "资源未找到";

        // When
        Result<Void> result = Result.fail(code, message);

        // Then
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFailWithMessage() {
        // Given
        String message = "操作失败";

        // When
        Result<Void> result = Result.fail(message);

        // Then
        assertEquals(500, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        Result<String> result = new Result<>();

        // Then
        assertEquals(0, result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testAllArgsConstructor() {
        // When
        Result<String> result = new Result<>(200, "success", "data");

        // Then
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }
}
