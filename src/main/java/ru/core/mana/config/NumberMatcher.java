package ru.core.mana.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбирает числовые выражения из config.yml: точное значение, сравнение и диапазон.
 * Используется бонусами full-food и health-range; новый числовой бонус может
 * переиспользовать этот класс вместо собственного парсера.
 */
public final class NumberMatcher {
    private static final Pattern RANGE = Pattern.compile("^\\s*(-?\\d+(?:\\.\\d+)?)\\.\\.(-?\\d+(?:\\.\\d+)?)\\s*$");
    private static final Pattern COMPARE = Pattern.compile("^\\s*(>=|<=|>|<|=)?\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");
    private final Double lower;
    private final Double upper;
    private final String operator;

    private NumberMatcher(Double lower, Double upper, String operator) {
        this.lower = lower;
        this.upper = upper;
        this.operator = operator;
    }

    public static NumberMatcher parse(String expression) {
        Matcher range = RANGE.matcher(expression);
        if (range.matches()) {
            return new NumberMatcher(Double.parseDouble(range.group(1)),
                    Double.parseDouble(range.group(2)), "range");
        }
        Matcher compare = COMPARE.matcher(expression);
        if (!compare.matches()) throw new IllegalArgumentException("Invalid numeric matcher: " + expression);
        return new NumberMatcher(Double.parseDouble(compare.group(2)), null,
                compare.group(1) == null ? "=" : compare.group(1));
    }

    public boolean matches(double value) {
        if ("range".equals(operator)) return value >= lower && value <= upper;
        return switch (operator) {
            case ">=" -> value >= lower;
            case "<=" -> value <= lower;
            case ">" -> value > lower;
            case "<" -> value < lower;
            default -> Math.abs(value - lower) < 0.000001;
        };
    }
}