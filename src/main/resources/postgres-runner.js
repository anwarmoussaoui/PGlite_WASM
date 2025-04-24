// postgres-runner.js
import { WASI } from '@wasmer/wasi';
import { WasmFs } from '@wasmer/wasmfs';
import { readFile } from 'fs/promises';
import { exec } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));

const wasmBytes = await readFile(join(__dirname, 'postgres.wasm'));

// Setup WASI
const wasmFs = new WasmFs();

const wasi = new WASI({
  args: ['postgres'],
  env: {},
  bindings: {
    ...WASI.defaultBindings,
    fs: wasmFs.fs,
  },
});

// Compile & instantiate
const module = await WebAssembly.compile(wasmBytes);
const instance = await WebAssembly.instantiate(module, {
  ...wasi.getImports(module),
});

// Start WASI (Postgres)
wasi.start(instance);

// At this point, the instance is running Postgres!
