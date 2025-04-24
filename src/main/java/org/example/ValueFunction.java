package org.example;

import org.graalvm.polyglot.Value;

@FunctionalInterface
public interface ValueFunction {
    Value apply(Value value);
}
