import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./src/App";
import { Buffer } from "buffer";

// Shim Buffer (for base64 stuff) available on window
// https://github.com/vitejs/vite/discussions/3126#discussioncomment-936044
window.Buffer = Buffer;

const root = createRoot(document.getElementById("root")!);
root.render(<App />);
