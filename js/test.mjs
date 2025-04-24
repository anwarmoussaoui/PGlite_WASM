import 'fast-text-encoding';
import { PGlite } from '@electric-sql/pglite';

globalThis.runQuery = function () {
  return new Promise((resolve, reject) => {
    try {
      const db = new PGlite();
      console.log(1);

      db.query("SELECT 'Hello world' AS message;")
        .then(result => {
          console.log("Query result:", result);
          resolve(result);
        })
        .catch(error => {
          console.error("Query error:", error);
          reject(error);
        });
    } catch (err) {
      reject(err);
    }
  });
};

// Optional: immediately run it for testing in JS
function test (){runQuery().then(res => console.log("Resolved:", res));}

test()