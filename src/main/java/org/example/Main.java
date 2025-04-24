package org.example;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static Map<String, String> getEngineOptions() {
        Map<String, String> engineOptions = new HashMap<>();
        engineOptions.put("engine.CompilerThreads", "1");
        engineOptions.put("engine.WarnInterpreterOnly", "false");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");
        return engineOptions;
    }

    private static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.mle-mode", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.unhandled-rejections", "throw");
        options.put("js.commonjs-require-cwd", Paths.get("./").toAbsolutePath().toString());
        return options;
    }

    public static void main(String[] args) {
        Engine engine = Engine.newBuilder()
                .allowExperimentalOptions(true)
                .options(getEngineOptions())

                .build();

        try (Context context = Context.newBuilder("js", "wasm")
                //.engine(engine)
                .allowHostAccess(HostAccess.ALL)
                .allowIO(true)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.esm-eval-returns-exports", "true")
                .option("js.unhandled-rejections", "throw")
                .allowAllAccess(true)
                .allowHostClassLookup(s -> true)
                .options(getLanguageOptions())
                .build()) {

            context.eval("js", "globalThis.self = globalThis;"); // 'globalThis' est l'équivalent moderne de 'global'
            context.eval("js", "globalThis.window = globalThis;");
            context.eval("js", "globalThis.document = { body: {} };");
            context.eval("js", "globalThis.window.location = { href: '' };");

            context.eval(Source.newBuilder("js",Main.class.getResource("/node_modules_electric-sql_pglite_dist_fs_nodefs_js.bundle.mjs")).build());

            context.eval(Source.newBuilder("js",Main.class.getResource("/vendors-node_modules_browserify-zlib_lib_index_js.bundle.mjs")).build());
            context.eval(Source.newBuilder("js",Main.class.getResource("/vendors-node_modules_electric-sql_pglite_dist_fs_opfs-ahp_js.bundle.mjs")).build());
            context.eval(Source.newBuilder("js",Main.class.getResource("/vendors-node_modules_util_util_js.bundle.mjs")).build());
            context.eval(Source.newBuilder("js",Main.class.getResource("/_c4ad.bundle.mjs")).build());
            context.eval(Source.newBuilder("js",Main.class.getResource("/_8414.bundle.mjs")).build());
            context.eval(Source.newBuilder("js", Main.class.getResource("/encoding-indexes.js")).build());
            context.eval(Source.newBuilder("js", Main.class.getResource("/encoding.js")).build());

            byte[] wasmfile = Files.readAllBytes(Paths.get("./src/main/resources/882a4fc4178bf2b803a6.wasm"));
            context.getBindings("js").putMember("wasmfile", wasmfile);
            byte[] datafile = Files.readAllBytes(Paths.get("./src/main/resources/6a6af95f67e70830cb39.data"));
            Value uint8ArrayConstructor = context.eval("js", "Uint8Array");
            Value jsArray = uint8ArrayConstructor.newInstance(datafile.length);
            for (int i = 0; i < datafile.length; i++) {
                jsArray.setArrayElement(i, datafile[i] & 0xFF); // Ensure unsigned
            }

            context.getBindings("js").putMember("datafile", jsArray);
            context.eval("js", """
                               (() => {
                                console.log("datafile length in JS:", globalThis.datafile.length);  // should log 5401749
                              const NativeURL = globalThis.URL;
                    
                              class FakeURL {
                                constructor(input, base) {
                                  this.href = input;
                                }
                    
                                toString() {
                                  return this.href;
                                }
                              }
                    
                              globalThis.URL = FakeURL;
                    
                              globalThis.fetch = async function (url) {
                                const wasmUrl = 'file:/Users/anwarmoussaoui/Desktop/Demos/pglite/Pglite/target/classes/882a4fc4178bf2b803a6.wasm';
                                const dataUrl = 'file:/Users/anwarmoussaoui/Desktop/Demos/pglite/Pglite/target/classes/6a6af95f67e70830cb39.data';
                                const fsBundlePath = '/Users/anwarmoussaoui/Desktop/Demos/pglite/Pglite/release/6a6af95f67e70830cb39.data';
                    
                                const target = (typeof url === 'object' && 'href' in url) ? url.href : url;
                    
                                if (target === wasmUrl) {
                                  return {
                                    async arrayBuffer() {
                                      return globalThis.wasmfile;
                                    },
                                    ok: true,
                                    status: 200,
                                  };
                                } else if (target === dataUrl) {
                                  return {
                                    async arrayBuffer() {
                                      return globalThis.datafile instanceof Uint8Array
                                        ? globalThis.datafile.buffer
                                        : globalThis.datafile;
                                    },
                                    ok: true,
                                    status: 200,
                                  };
                                } else if (target.endsWith('pglite.data')) {
                                  // Emulate getFsBundle behavior
                                  if (typeof process !== 'undefined' && process.versions && process.versions.node) {
                                    // In Node.js (via GraalVM)
                                    const fs = await import('fs/promises');
                                    const fileData = await fs.readFile(fsBundlePath);
                                    return {
                                      async arrayBuffer() {
                                        return fileData.buffer;
                                      },
                                      ok: true,
                                      status: 200,
                                    };
                                  } else {
                                    throw new Error('Browser fetch not supported in this environment');
                                  }
                                } else {
                                  throw new Error(`Unhandled fetch to: ${target}`);
                                }
                              };
                            })();
                    if (typeof WebAssembly.instantiateStreaming !== "function") {
                      WebAssembly.instantiateStreaming = async (sourcePromise, importObject) => {
                        // Assume `globalThis.wasmfile` is already a Uint8Array or ArrayBuffer
                        const buffer = globalThis.wasmfile instanceof Uint8Array
                          ? globalThis.wasmfile.buffer
                          : globalThis.wasmfile;
                    
                        return WebAssembly.instantiate(new Uint8Array(buffer), importObject);
                      };
                    }
                    
                    
                    
                            """
            );
            Source bundleSrc = Source.newBuilder("js",
                            Main.class.getResource("/bundle.mjs")).build();
             context.eval(bundleSrc);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
