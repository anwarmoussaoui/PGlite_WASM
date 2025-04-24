package org.example;

public interface Promise {
    Promise then(ValueConsumer onResolve);
    Promise then(ValueConsumer onResolve, ValueConsumer onReject);
    Promise catch_(ValueConsumer onReject);
}
