import { PGlite } from '@electric-sql/pglite';

export async function runQuery() {
  try {
    // If wasmPath is wrong or file is missing, it will throw here
    const db = new PGlite();

    const result = await db.query("SELECT 'Hello world' AS message;");
    await db.close();
    return result.rows;
  } catch (err) {
    console.error('Query failed:', err);
    throw err; // Make sure the error is still thrown back to Java
  }
}
