const http = require("http");
const express = require("express");
const { WebSocketServer } = require("ws");
const { v4: uuidv4 } = require("uuid");
const { setupWSConnection } = require("../node_modules/y-websocket/bin/utils.js");
const { log } = require("./logger");

const PORT = Number(process.env.PORT || 1234);

const app = express();
app.disable("x-powered-by");
app.use(express.json({ limit: "1mb" }));
app.use((req, _res, next) => {
  log("info", "http_request", {
    method: req.method,
    path: req.path
  });
  next();
});

app.get("/", (_req, res) => {
  res.status(200).json({
    service: "erdtool-collaboration",
    status: "ok",
    websocket: "ws://host:1234/<room-id>"
  });
});

app.get("/healthz", (_req, res) => {
  res.status(200).json({
    status: "ok",
    service: "collaboration",
    requestId: uuidv4()
  });
});

app.use((err, _req, res, _next) => {
  log("error", "unexpected_error", { error: err?.message || String(err) });
  res.status(500).json({ message: "internal server error" });
});

const server = http.createServer(app);
const wss = new WebSocketServer({ noServer: true });

server.on("upgrade", (req, socket, head) => {
  const { pathname } = new URL(req.url, `http://${req.headers.host}`);
  if (pathname === "/" || pathname === "/healthz" || pathname === "/favicon.ico") {
    socket.destroy();
    return;
  }

  wss.handleUpgrade(req, socket, head, (ws) => {
    wss.emit("connection", ws, req);
  });
});

wss.on("connection", (ws, req) => {
  const pathname = new URL(req.url, `http://${req.headers.host}`).pathname;
  log("info", "ws_connection", {
    path: pathname,
    remoteAddress: req.socket.remoteAddress
  });

  setupWSConnection(ws, req, {
    gc: true
  });

  ws.on("close", (code, reason) => {
    log("info", "ws_close", {
      code,
      reason: reason?.toString?.() || ""
    });
  });

  ws.on("error", (error) => {
    log("error", "ws_error", { error: error.message });
  });
});

server.listen(PORT, "0.0.0.0", () => {
  log("info", "server_started", { port: PORT });
});
