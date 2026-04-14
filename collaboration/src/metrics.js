const client = require("prom-client");

const register = new client.Registry();

client.collectDefaultMetrics({
  register,
  prefix: "collaboration_"
});

const activeConnectionsGauge = new client.Gauge({
  name: "collaboration_ws_active_connections",
  help: "Current number of active websocket connections"
});

const activeRoomsGauge = new client.Gauge({
  name: "collaboration_ws_active_rooms",
  help: "Current number of rooms with at least one active websocket connection"
});

const connectionOpenedCounter = new client.Counter({
  name: "collaboration_ws_connections_opened_total",
  help: "Total websocket connections opened"
});

const connectionClosedCounter = new client.Counter({
  name: "collaboration_ws_connections_closed_total",
  help: "Total websocket connections closed"
});

const wsErrorCounter = new client.Counter({
  name: "collaboration_ws_errors_total",
  help: "Total websocket errors observed"
});

const wsMessageCounter = new client.Counter({
  name: "collaboration_ws_messages_total",
  help: "Total inbound websocket messages classified by protocol kind",
  labelNames: ["kind"]
});

register.registerMetric(activeConnectionsGauge);
register.registerMetric(activeRoomsGauge);
register.registerMetric(connectionOpenedCounter);
register.registerMetric(connectionClosedCounter);
register.registerMetric(wsErrorCounter);
register.registerMetric(wsMessageCounter);

const roomConnectionCounts = new Map();

function connectionOpened(roomId) {
  activeConnectionsGauge.inc();
  connectionOpenedCounter.inc();
  bumpRoomCount(roomId, 1);
}

function connectionClosed(roomId) {
  activeConnectionsGauge.dec();
  connectionClosedCounter.inc();
  bumpRoomCount(roomId, -1);
}

function wsError() {
  wsErrorCounter.inc();
}

function inboundMessage(kind = "unknown") {
  wsMessageCounter.inc({ kind });
}

function bumpRoomCount(roomId, delta) {
  if (!roomId) {
    return;
  }

  const nextCount = (roomConnectionCounts.get(roomId) || 0) + delta;
  if (nextCount <= 0) {
    roomConnectionCounts.delete(roomId);
  } else {
    roomConnectionCounts.set(roomId, nextCount);
  }
  activeRoomsGauge.set(roomConnectionCounts.size);
}

module.exports = {
  metricsContentType: register.contentType,
  register,
  connectionOpened,
  connectionClosed,
  wsError,
  inboundMessage
};
