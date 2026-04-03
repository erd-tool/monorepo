const http = require("http");
const express = require("express");
const { WebSocketServer } = require("ws");
const { v4: uuidv4 } = require("uuid");
const { setupWSConnection } = require("../node_modules/y-websocket/bin/utils.js");
const { log } = require("./logger");

const PORT = Number(process.env.PORT || 1234);
const ERD_SERVICE_URL = (process.env.ERD_SERVICE_URL || "http://localhost:8083").replace(/\/$/, "");

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
    websocket: "ws://host/ws/collaboration/<room-id>"
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

server.on("upgrade", async (req, socket, head) => {
  const { pathname } = new URL(req.url, `http://${req.headers.host}`);
  if (pathname === "/" || pathname === "/healthz" || pathname === "/favicon.ico") {
    socket.destroy();
    return;
  }

  const gatewayContext = extractGatewayContext(req);
  if (!gatewayContext) {
    log("warn", "ws_upgrade_rejected", { reason: "missing_gateway_headers" });
    rejectUpgrade(socket, 401, "Gateway 인증 헤더가 필요합니다.");
    return;
  }

  const roomId = pathname.replace(/^\/+/, "");
  if (!/^\d+$/.test(roomId)) {
    log("warn", "ws_upgrade_rejected", { reason: "invalid_room_id", path: pathname });
    rejectUpgrade(socket, 400, "유효한 ERD room id가 필요합니다.");
    return;
  }

  const access = await verifyRoomAccess(roomId, gatewayContext);
  if (!access.ok) {
    log("warn", "ws_upgrade_rejected", {
      reason: access.reason,
      roomId,
      userId: gatewayContext.userId,
      statusCode: access.statusCode
    });
    rejectUpgrade(socket, access.statusCode, access.message);
    return;
  }

  req.collaborationContext = { ...gatewayContext, roomId };

  wss.handleUpgrade(req, socket, head, (ws) => {
    wss.emit("connection", ws, req);
  });
});

wss.on("connection", (ws, req) => {
  const pathname = new URL(req.url, `http://${req.headers.host}`).pathname;
  const context = req.collaborationContext || {};
  log("info", "ws_connection", {
    path: pathname,
    remoteAddress: req.socket.remoteAddress,
    requestId: context.requestId,
    roomId: context.roomId,
    userId: context.userId,
    loginId: context.loginId
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

function extractGatewayContext(req) {
  const userId = getHeader(req, "x-user-id");
  const loginId = getHeader(req, "x-user-login-id");
  const requestId = getHeader(req, "x-request-id");
  if (!userId || !loginId || !requestId) {
    return null;
  }
  return {
    userId,
    loginId,
    role: getHeader(req, "x-user-role") || "",
    email: getHeader(req, "x-user-email") || "",
    requestId
  };
}

function getHeader(req, name) {
  const value = req.headers[name];
  return Array.isArray(value) ? value[0] : value;
}

async function verifyRoomAccess(roomId, context) {
  try {
    const response = await fetch(`${ERD_SERVICE_URL}/internal/erds/${roomId}/access`, {
      method: "GET",
      headers: {
        "X-USER-ID": context.userId,
        "X-USER-LOGIN-ID": context.loginId,
        "X-USER-ROLE": context.role,
        "X-USER-EMAIL": context.email,
        "X-REQUEST-ID": context.requestId
      },
      signal: AbortSignal.timeout(3000)
    });

    if (response.ok) {
      return { ok: true };
    }

    if (response.status === 401 || response.status === 403 || response.status === 404) {
      return {
        ok: false,
        statusCode: response.status,
        reason: "forbidden_room_access",
        message: "협업 room 접근 권한이 없습니다."
      };
    }

    return {
      ok: false,
      statusCode: 502,
      reason: "authorization_service_error",
      message: "협업 권한 검증에 실패했습니다."
    };
  } catch (error) {
    log("error", "room_access_verification_failed", {
      roomId,
      userId: context.userId,
      error: error?.message || String(error)
    });
    return {
      ok: false,
      statusCode: 502,
      reason: "authorization_unavailable",
      message: "협업 권한 검증 서비스에 연결할 수 없습니다."
    };
  }
}

function rejectUpgrade(socket, statusCode, message) {
  socket.write(
    `HTTP/1.1 ${statusCode} ${http.STATUS_CODES[statusCode] || "Error"}\r\n` +
      "Connection: close\r\n" +
      "Content-Type: application/json\r\n" +
      "\r\n" +
      JSON.stringify({ message })
  );
  socket.destroy();
}
