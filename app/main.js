import electron from "electron";
import * as childProcess from "child_process";
import * as path from "path";

let { app, BrowserWindow } = electron;

const isDevelopment = false;

/* global process */

let server;

let binDir = path.join(app.getAppPath(), "bin");
let resourcesDir = path
  .join(app.getAppPath(), "public")
  .replace("app.asar", "app.asar.unpacked");

function createWindow() {
  let win = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: false,
    },
    title: "Tales",
  });
  win.maximize();

  if (isDevelopment) {
    win.webContents.openDevTools();
  }

  win.loadURL("http://localhost:3000/");

  win.webContents.on("devtools-opened", () => {
    win.focus();
    setTimeout(() => win.focus(), 0);
  });

  win.removeMenu();
}

function start() {
  runServer();
  createWindow();
}

function stop() {
  killServer();
}

function runServer() {
  server = childProcess.execFile(path.join(binDir, "tales-server"), [
    "-bind",
    "127.0.0.1:3000",
    "-resources",
    resourcesDir,
  ]);
  server.on("error", err => {
    console.error("failed to start process", err);
    server = null;
  });
  server.on("exit", (code, signal) => {
    console.log("server exited", code, signal);
    server = null;
  });
  server.unref();
}

function killServer() {
  if (server) {
    console.log("killing server...");
    server.kill();
  }
}

app.whenReady().then(start);

app.on("will-quit", stop);

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
