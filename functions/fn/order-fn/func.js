const fdk = require("@fnproject/fdk");
const { Pool } = require("pg");

const pool = new Pool({
  host: process.env.PGHOST || "host.docker.internal",
  port: Number(process.env.PGPORT || 5433),
  user: process.env.PGUSER || "app",
  password: process.env.PGPASSWORD || "app",
  database: process.env.PGDATABASE || "app"
});

let initPromise;

async function ensureSchema() {
  if (!initPromise) {
    initPromise = pool
      .query(
        "CREATE TABLE IF NOT EXISTS order_faas_audit (" +
          "id SERIAL PRIMARY KEY, " +
          "order_id BIGINT, " +
          "user_id BIGINT, " +
          "table_number BIGINT, " +
          "item TEXT, " +
          "received_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
        ")"
      )
      .then(() => pool.query(
        "ALTER TABLE order_faas_audit " +
          "ADD COLUMN IF NOT EXISTS table_number BIGINT"
      ))
      .catch((error) => {
        initPromise = undefined;
        throw error;
      });
  }
  return initPromise;
}

fdk.handle(async function (input) {
  const payload = input || {};
  try {
    await ensureSchema();

    await pool.query(
      "INSERT INTO order_faas_audit (order_id, user_id, table_number, item) " +
        "VALUES ($1, $2, $3, $4)",
      [
        payload.orderId ?? null,
        payload.userId ?? null,
        payload.tableNumber ?? null,
        payload.item ?? null
      ]
    );

    return {
      status: "stored",
      orderId: payload.orderId ?? null,
      userId: payload.userId ?? null,
      item: payload.item ?? null,
      tableNumber: payload.tableNumber ?? null
    };
  } catch (error) {
    console.error("order-fn failed:", error);
    return {
      status: "error",
      message: error instanceof Error ? error.message : "unknown error"
    };
  }
});
