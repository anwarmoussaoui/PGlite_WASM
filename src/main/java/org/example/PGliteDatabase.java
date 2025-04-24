package org.example;

import org.graalvm.polyglot.Value;

public interface PGliteDatabase {
    Promise query(String sql);
    Promise query(String sql, Value[] params);
    Promise exec(String sql);
    Promise exec(String sql, Value[] params);
}
