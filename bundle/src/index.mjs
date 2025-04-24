import { PGlite } from "@electric-sql/pglite";

const db = new PGlite();
var x = await db.query("select 'Hello world' as message;");
console.log(x)
// -> { rows: [ { message: "Hello world" } ] }