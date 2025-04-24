package org.example;

import org.graalvm.polyglot.Value;

@FunctionalInterface
public interface ValueConsumer extends java.util.function.Consumer<Value> {
    @Override
    void accept(Value value);
}

