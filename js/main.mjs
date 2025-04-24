import 'fast-text-encoding';
import { PGlite } from '@electric-sql/pglite';

globalThis.runQuery = function() {
  const db = new PGlite();
  console.log(1);

  return db.query('SELECT "Hello world" AS message;')
    .then(result => {
    console.log(result)
      return db.close().then(() => {


        return result.rows;
      });
    })
    .catch(err => {
      console.error("Query failed:", err);
      throw err;
    });
};

// Test function using plain Promises
globalThis.test = function() {
  return runQuery()
    .then(result => {
      console.log("Result:", result);
      return result; // Return the result so Java can access it
    })
    .catch(err => {
      console.error("Error:", err);
      throw err;
    });
};


console.log(test());