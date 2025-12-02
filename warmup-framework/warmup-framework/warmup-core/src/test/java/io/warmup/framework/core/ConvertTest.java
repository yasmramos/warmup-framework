package io.warmup.framework.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

/**
 * Unit tests for {@link Convert}.
 */
class ConvertTest {

    /* ============================
     *  PRIMITIVES & WRAPPERS
     * ============================ */
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "42, int",
        "true, boolean",
        "1234567890123, long",
        "1234567890123L, long",
        "3.14, double",
        "2.5, float",
        "2.5f, float",
        "A, char",
        "127, byte",
        "32000, short"
    })
    void testValidPrimitiveConversions(String input, Class<?> primitiveType) {
        Object result = Convert.convertStringToType(input, primitiveType);
        Object expected = createExpectedValue(input, primitiveType);

        assertThat(result).isEqualTo(expected);
        assertThat(result).isInstanceOf(getWrapperType(primitiveType));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "99, java.lang.Integer",
        "false, java.lang.Boolean",
        "999, java.lang.Long",
        "999L, java.lang.Long",
        "1.1, java.lang.Double",
        "9.9, java.lang.Float",
        "9.9f, java.lang.Float",
        "Z, java.lang.Character",
        "100, java.lang.Byte",
        "1111, java.lang.Short"
    })
    void testValidWrapperConversions(String input, String wrapperName) throws Exception {
        Class<?> wrapperType = Class.forName(wrapperName);
        Object result = Convert.convertStringToType(input, wrapperType);
        Object expected = createExpectedValue(input, wrapperType);

        assertThat(result).isEqualTo(expected);
        assertThat(result).isInstanceOf(getWrapperType(wrapperType));
    }

    /**
     * Helper method to create expected values based on input and target type
     */
    private Object createExpectedValue(String input, Class<?> targetType) {
        // Remove suffixes for parsing
        String cleanInput = input;
        if (targetType == long.class || targetType == Long.class) {
            cleanInput = input.endsWith("L") || input.endsWith("l")
                    ? input.substring(0, input.length() - 1) : input;
        } else if (targetType == float.class || targetType == Float.class) {
            cleanInput = input.endsWith("F") || input.endsWith("f")
                    ? input.substring(0, input.length() - 1) : input;
        }

        // Create expected value based on type
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(cleanInput);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.valueOf(cleanInput);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.valueOf(cleanInput);
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.valueOf(cleanInput);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.valueOf(cleanInput);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.valueOf(cleanInput);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.valueOf(cleanInput);
        } else if (targetType == char.class || targetType == Character.class) {
            return cleanInput.charAt(0);
        }

        throw new IllegalArgumentException("Unsupported type: " + targetType);
    }

    /* ============================
     *  NULL -> DEFAULT VALUES
     * ============================ */
    @ParameterizedTest
    @NullSource
    void testNullToPrimitives(String ignored) {
        assertThat(Convert.convertStringToType(null, int.class)).isEqualTo(0);
        assertThat(Convert.convertStringToType(null, boolean.class)).isEqualTo(false);
        assertThat(Convert.convertStringToType(null, long.class)).isEqualTo(0L);
        assertThat(Convert.convertStringToType(null, double.class)).isEqualTo(0.0);
        assertThat(Convert.convertStringToType(null, float.class)).isEqualTo(0.0f);
        assertThat(Convert.convertStringToType(null, byte.class)).isEqualTo((byte) 0);
        assertThat(Convert.convertStringToType(null, short.class)).isEqualTo((short) 0);
        assertThat(Convert.convertStringToType(null, char.class)).isEqualTo('\0');
    }

    @ParameterizedTest
    @NullSource
    void testNullToWrappers(String ignored) {
        assertThat(Convert.convertStringToType(null, Integer.class)).isNull();
        assertThat(Convert.convertStringToType(null, Boolean.class)).isNull();
        assertThat(Convert.convertStringToType(null, String.class)).isNull();
        assertThat(Convert.convertStringToType(null, Long.class)).isNull();
        assertThat(Convert.convertStringToType(null, Double.class)).isNull();
        assertThat(Convert.convertStringToType(null, Float.class)).isNull();
        assertThat(Convert.convertStringToType(null, Byte.class)).isNull();
        assertThat(Convert.convertStringToType(null, Short.class)).isNull();
        assertThat(Convert.convertStringToType(null, Character.class)).isNull();
    }

    /* ============================
     *  STRING IDENTITY
     * ============================ */
    @Test
    void testStringIdentity() {
        String value = "hello";
        String result = Convert.convertStringToType(value, String.class);
        assertThat(result).isSameAs(value);
    }

    /* ============================
     *  INVALID CONVERSIONS
     * ============================ */
    @Test
    void testInvalidIntegerThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> Convert.convertStringToType("abc", int.class));
        assertThat(ex).hasMessageContaining("For input string: \"abc\"");
    }

    @Test
    void testCharTooLongThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> Convert.convertStringToType("toolong", char.class));
        assertThat(ex).hasMessageContaining("Cannot convert 'toolong' to char/Character");
    }

    /* ============================
     *  CUSTOM TYPE WITH String CTOR
     * ============================ */
    @Test
    void testCustomTypeViaStringConstructor() {
        CustomValue cv = Convert.convertStringToType("555", CustomValue.class);
        assertThat(cv.value).isEqualTo(555);
    }

    @Test
    void testCustomTypeInvalidThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Convert.convertStringToType("xyz", CustomValue.class));
    }

    /* ============================
     *  HELPER: convertValueForType
     * ============================ */
    @Test
    void testConvertValueForTypeDelegatesCorrectly() {
        Object prim = Convert.convertValueForType("77", int.class);
        Object wrap = Convert.convertValueForType("77", Integer.class);

        assertThat(prim).isEqualTo(77);
        assertThat(wrap).isEqualTo(Integer.valueOf(77));
    }

    @Test
    void testConvertValueForTypeNullDefault() {
        assertThat(Convert.convertValueForType(null, double.class)).isEqualTo(0.0);
        assertThat(Convert.convertValueForType(null, String.class)).isNull();
    }

    /* ============================
     *  EDGE CASES
     * ============================ */
    @Test
    void testFloatWithFSuffix() {
        Float result = Convert.convertStringToType("2.5f", Float.class);
        assertThat(result).isEqualTo(2.5f);
    }

    @Test
    void testLongWithLSuffix() {
        Long result = Convert.convertStringToType("123L", Long.class);
        assertThat(result).isEqualTo(123L);
    }

    @Test
    void testCharacterConversion() {
        Character result = Convert.convertStringToType("X", Character.class);
        assertThat(result).isEqualTo('X');
    }

    private Class<?> getWrapperType(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return Boolean.class;
            }
            if (type == byte.class) {
                return Byte.class;
            }
            if (type == short.class) {
                return Short.class;
            }
            if (type == int.class) {
                return Integer.class;
            }
            if (type == long.class) {
                return Long.class;
            }
            if (type == float.class) {
                return Float.class;
            }
            if (type == double.class) {
                return Double.class;
            }
            if (type == char.class) {
                return Character.class;
            }
            // void.class is primitive but not relevant here
        }
        return type;
    }

    /* ============================
     *  DUMMY CUSTOM TYPE
     * ============================ */
    public static class CustomValue {

        public final int value;

        public CustomValue(String s) {
            this.value = Integer.parseInt(s);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CustomValue that = (CustomValue) obj;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(value);
        }
    }
}
