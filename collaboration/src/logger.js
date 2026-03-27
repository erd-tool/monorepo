const levels = ["debug", "info", "warn", "error"];

function log(level, message, meta = {}) {
  const safeLevel = levels.includes(level) ? level : "info";
  const payload = {
    ts: new Date().toISOString(),
    level: safeLevel,
    message,
    ...meta
  };

  const line = JSON.stringify(payload);
  if (safeLevel === "error") {
    console.error(line);
    return;
  }
  if (safeLevel === "warn") {
    console.warn(line);
    return;
  }
  console.log(line);
}

module.exports = { log };

