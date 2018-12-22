package com.github.urlv.utils;

import java.util.function.Predicate;

public class Check <T>{
    private Predicate<T> predicate;
    private T object;
    private boolean active = true;
    private Boolean result = null;

    private Check(Predicate<T> predicate, T object) {
        this.predicate = predicate;
        this.object = object;
    }

    public static <T>Check condition(Predicate<T> predicate, T object) {
        return new Check<>(predicate, object);
    }

    public static Check condition(boolean b) {
        return new Check<>(b2 -> b2, b);
    }

    public Check ifTrue(Action onTrue) {
        calculate(true, onTrue);
        return this;
    }

    public Check ifFalse(Action onFalse) {
        calculate(false, onFalse);
        return this;
    }

    private void calculate(boolean expected, Action action) {
        if (active) {
            result = this.result == null ? predicate.test(object) : result;

            if (result == expected) {
                active = false;
                action.invoke();
            }
        }
    }
}
