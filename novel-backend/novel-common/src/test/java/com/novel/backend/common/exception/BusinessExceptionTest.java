package com.novel.backend.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 测试
 */
class BusinessExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "测试异常";

        // When
        BusinessException exception = new BusinessException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(500, exception.getCode());
    }

    @Test
    void testConstructorWithCodeAndMessage() {
        // Given
        int code = 400;
        String message = "参数错误";

        // When
        BusinessException exception = new BusinessException(code, message);

        // Then
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "测试异常";
        Throwable cause = new RuntimeException("原始异常");

        // When
        BusinessException exception = new BusinessException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(500, exception.getCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithCodeMessageAndCause() {
        // Given
        int code = 404;
        String message = "资源未找到";
        Throwable cause = new RuntimeException("原始异常");

        // When
        BusinessException exception = new BusinessException(code, message, cause);

        // Then
        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
