package org.example;

import org.graalvm.polyglot.Value;

public interface PGlite {
    Promise createDatabase(String name);
    Promise toString(String data);
}

