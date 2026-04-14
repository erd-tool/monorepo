const levels = ["debug", "info", "warn", "error"];
const service = process.env.SERVICE_NAME || "collaboration";

function log(level, event, meta = {}) {
  const safeLevel = levels.includes(level) ? level : "info";
  const {
    message,
    requestId = null,
    roomId = null,
    ...rest
  } = meta;
  const payload = {
    timestamp: new Date().toISOString(),
    level: safeLevel,
    service,
    requestId,
    roomId,
    event,
    message: message || event,
    ...rest
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
