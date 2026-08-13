import { useEffect, useState, type MouseEventHandler, type ReactNode } from "react";
import {
  Aperture, ArrowDown, ChevronDown, Flashlight, Info, Layers3,
  RotateCcw, Settings, Sparkles, Sun, Timer, X,
} from "lucide-react";

type Sheet = "controls" | "gallery" | "config" | "settings" | null;

const modes = ["PHOTO", "PORTRAIT", "VIDEO", "NIGHT", "ASTRO"];
const lenses = ["0.6", "1×", "2×", "5×"];

function IconButton({ label, children, onClick, active = false }: {
  label: string; children: ReactNode; onClick?: () => void; active?: boolean;
}) {
  return (
    <button aria-label={label} title={label} onClick={onClick}
      className={`group flex h-9 w-9 items-center justify-center rounded-full border transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#d7ae60] ${active ? "border-[#d7ae60]/70 bg-[#d7ae60]/20 text-[#f5cc7c]" : "border-white/10 bg-black/20 text-white/80 hover:border-white/30 hover:bg-white/10"}`}>
      {children}
    </button>
  );
}

function GlassPanel({ children, className = "", onClick }: { children: ReactNode; className?: string; onClick?: MouseEventHandler<HTMLDivElement> }) {
  return <div onClick={onClick} className={`rounded-[28px] border border-white/10 bg-[#111313]/90 shadow-2xl shadow-black/40 backdrop-blur-2xl ${className}`}>{children}</div>;
}

export function NovaCam() {
  const [intro, setIntro] = useState(true);
  const [sheet, setSheet] = useState<Sheet>(null);
  const [flash, setFlash] = useState(false);
  const [hdr, setHdr] = useState(true);
  const [ai, setAi] = useState(true);
  const [live, setLive] = useState(true);
  const [lens, setLens] = useState("1×");
  const [mode, setMode] = useState("PHOTO");
  const [focus, setFocus] = useState({ x: 50, y: 43 });
  const [processing, setProcessing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [toast, setToast] = useState("");
  const [filter, setFilter] = useState("Photos");
  const [config, setConfig] = useState("Vivid HD");
  const [timer, setTimer] = useState(false);

  useEffect(() => { const t = setTimeout(() => setIntro(false), 1900); return () => clearTimeout(t); }, []);
  useEffect(() => {
    if (!processing) return undefined;
    const t = setInterval(() => setProgress((p) => p >= 100 ? 100 : p + 8), 110);
    return () => clearInterval(t);
  }, [processing]);
  useEffect(() => {
    if (progress === 100) {
      const t = setTimeout(() => { setProcessing(false); setProgress(0); setToast("Saved to Nova Gallery"); }, 500);
      return () => clearTimeout(t);
    }
    return undefined;
  }, [progress]);
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(""), 2200);
    return () => clearTimeout(t);
  }, [toast]);

  const notify = (message: string) => setToast(message);
  const shutter = () => {
    if (mode === "ASTRO") { notify("Astro mode needs a tripod and clear sky"); return; }
    setProcessing(true); setProgress(4);
  };
  const unsupported = (name: string) => notify(`${name} is not available on this device`);

  if (intro) return (
    <main className="flex min-h-[100dvh] items-center justify-center overflow-hidden bg-[#090b0b] text-[#f1eadc]" style={{ fontFamily: "ui-sans-serif, system-ui" }}>
      <style>{css}</style>
      <div className="relative text-center animate-[fadeIn_1.1s_ease-out]">
        <div className="mx-auto mb-7 flex h-20 w-20 items-center justify-center rounded-[26px] border border-[#d7ae60]/40 bg-[#d7ae60]/10 shadow-[0_0_80px_rgba(215,174,96,.16)]"><Aperture size={35} strokeWidth={1.2} className="text-[#d7ae60]" /></div>
        <p className="text-[10px] uppercase tracking-[.38em] text-[#d7ae60]">NOVA SYSTEMS</p>
        <h1 className="mt-3 font-['Georgia'] text-4xl tracking-tight">NovaCam</h1>
        <p className="mt-3 text-xs tracking-[.16em] text-white/40">COMPUTATIONAL CAMERA</p>
        <div className="mx-auto mt-10 h-px w-24 bg-gradient-to-r from-transparent via-[#d7ae60] to-transparent" />
      </div>
    </main>
  );

  return (
    <main className="relative mx-auto min-h-[100dvh] w-full max-w-[520px] overflow-hidden bg-[#070909] text-[#f1eadc] selection:bg-[#d7ae60]/30" style={{ fontFamily: "ui-sans-serif, system-ui" }}>
      <style>{css}</style>
      <section className="camera-scene absolute inset-0" onClick={(e) => {
        const r = e.currentTarget.getBoundingClientRect();
        setFocus({ x: ((e.clientX - r.left) / r.width) * 100, y: ((e.clientY - r.top) / r.height) * 100 });
      }}>
        <div className="scene-glow" />
        <div className="scene-mountain" />
        <div className="scene-lake" />
      </section>
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-b from-black/55 via-transparent to-[#070909]/95" />

      <header className="relative z-10 flex items-center justify-between px-5 pb-3 pt-5">
        <div className="flex items-center gap-2"><Aperture size={16} className="text-[#d7ae60]" /><span className="text-[11px] font-medium tracking-[.24em]">NOVACAM</span></div>
        <div className="flex items-center gap-2 text-[9px] tracking-[.14em] text-white/60"><span className="h-1.5 w-1.5 rounded-full bg-[#bfe6be]" /> 48 MP · 24°C</div>
        <div className="flex gap-2">
          <IconButton label="Flash" active={flash} onClick={() => setFlash(!flash)}><Flashlight size={16} /></IconButton>
          <IconButton label="HDR Auto" active={hdr} onClick={() => setHdr(!hdr)}><Layers3 size={16} /></IconButton>
          <IconButton label="AI scene detection" active={ai} onClick={() => setAi(!ai)}><Sparkles size={16} /></IconButton>
        </div>
      </header>

      <div className="relative z-10 flex items-center justify-between px-5 pt-4">
        <button className="flex items-center gap-1.5 rounded-full border border-white/10 bg-black/20 px-3 py-2 text-[10px] tracking-[.12em] text-white/80" onClick={() => setSheet("config")}><span className="h-1.5 w-1.5 rounded-full bg-[#d7ae60]" /> {config}<ChevronDown size={12} /></button>
        <div className="flex gap-2"><IconButton label="Camera settings" onClick={() => setSheet("settings")}><Settings size={16} /></IconButton><IconButton label="Timer" active={timer} onClick={() => { setTimer(!timer); notify(timer ? "Timer off" : "3 second timer"); }}><Timer size={16} /></IconButton></div>
      </div>

      <div className="focus-ring pointer-events-none absolute z-10 h-14 w-14 -translate-x-1/2 -translate-y-1/2 rounded-full border border-[#d7ae60]/80" style={{ left: `${focus.x}%`, top: `${focus.y}%` }}>
        <span className="absolute -right-2 -top-2 flex h-5 w-5 items-center justify-center rounded-full bg-[#d7ae60] text-[#17130c]"><Sun size={11} /></span>
      </div>
      <div className="absolute left-5 top-[31%] z-10 rounded-full border border-white/10 bg-black/25 px-2.5 py-1 text-[9px] tracking-[.13em] text-white/60">AF · AE LOCK</div>
      <div className="absolute right-5 top-[31%] z-10 flex flex-col items-end gap-1 text-[9px] tracking-[.12em] text-white/55"><span>ISO 50</span><span>1/120 s</span><span>EV 0.0</span></div>

      <div className="absolute bottom-0 left-0 right-0 z-20">
        <div className="mb-5 flex justify-center gap-2">
          {lenses.map((l) => <button key={l} onClick={() => setLens(l)} className={`h-9 min-w-9 rounded-full px-2 text-[11px] font-medium transition-all ${lens === l ? "bg-[#d7ae60] text-[#16120b] shadow-lg shadow-[#d7ae60]/20" : "border border-white/10 bg-black/30 text-white/70"}`}>{l}</button>)}
        </div>
        <div className="mb-5 flex items-center justify-between px-6">
          <button aria-label="Open gallery" onClick={() => setSheet("gallery")} className="h-12 w-12 overflow-hidden rounded-2xl border border-white/20 bg-gradient-to-br from-[#c08c55] via-[#514a40] to-[#1c393c] text-left shadow-lg"><span className="block h-full w-full bg-[radial-gradient(circle_at_70%_30%,#e8b769,transparent_22%),linear-gradient(145deg,transparent_45%,#192e31_46%)]" /></button>
          <button aria-label="Capture photo" onClick={shutter} className={`shutter-button relative flex h-[76px] w-[76px] items-center justify-center rounded-full border-[3px] border-[#f5ead5] transition-transform active:scale-90 ${processing ? "scale-95" : ""}`}><span className="h-[62px] w-[62px] rounded-full bg-[#f1eadc]" /></button>
          <IconButton label="Switch camera" onClick={() => notify("Front camera")}><RotateCcw size={20} /></IconButton>
        </div>
        <div className="border-t border-white/10 bg-black/35 px-5 pb-5 pt-3 backdrop-blur-xl">
          <div className="mb-3 flex items-center justify-between"><button onClick={() => setLive(!live)} className={`flex items-center gap-2 text-[10px] tracking-[.14em] ${live ? "text-[#f2c977]" : "text-white/45"}`}><span className={`h-2 w-2 rounded-full ${live ? "bg-[#f2c977]" : "bg-white/30"}`} /> LIVE PHOTO {live ? "ON" : "OFF"}</button><button onClick={() => setSheet("controls")} className="flex items-center gap-1 text-[10px] tracking-[.14em] text-white/65"><ArrowDown size={14} /> CONTROLS</button></div>
          <div className="flex gap-7 overflow-x-auto pb-1 no-scrollbar">{modes.map((m) => <button key={m} onClick={() => m === "ASTRO" ? setMode(m) : setMode(m)} className={`shrink-0 text-[11px] tracking-[.18em] transition-colors ${mode === m ? "font-semibold text-[#f2c977]" : "text-white/45"}`}>{m}</button>)}</div>
        </div>
      </div>

      {processing && <div className="absolute inset-0 z-40 flex items-center justify-center bg-[#080a0a]/70 backdrop-blur-md"><GlassPanel className="w-[82%] p-6"><div className="mb-5 flex items-center justify-between"><div><p className="text-[10px] tracking-[.2em] text-[#d7ae60]">NOVA ENGINE</p><h2 className="mt-1 text-xl">Processing image</h2></div><Aperture className="animate-[spin_2s_linear_infinite] text-[#d7ae60]" size={25} /></div><div className="space-y-3 text-xs text-white/60"><Progress label="Aligning frames" value={progress > 32 ? 100 : progress * 3} /><Progress label="Recovering highlights" value={progress > 58 ? 100 : Math.max(0, progress - 20) * 2.6} /><Progress label="Finishing Vivid HD" value={progress > 80 ? progress : 0} /></div><p className="mt-5 text-right font-mono text-[10px] text-[#d7ae60]">{Math.min(progress, 100)}%</p></GlassPanel></div>}
      {toast && <div className="absolute left-1/2 top-24 z-50 -translate-x-1/2 rounded-full border border-[#d7ae60]/30 bg-[#191711]/90 px-4 py-2 text-[11px] text-[#f2d08a] shadow-xl">{toast}</div>}
      {sheet && <Overlay sheet={sheet} close={() => setSheet(null)} filter={filter} setFilter={setFilter} config={config} setConfig={setConfig} notify={notify} unsupported={unsupported} />}
    </main>
  );
}

function Progress({ label, value }: { label: string; value: number }) { return <div><div className="mb-1 flex justify-between"><span>{label}</span><span className="text-[#d7ae60]">{value >= 100 ? "Ready" : "Working"}</span></div><div className="h-1 overflow-hidden rounded-full bg-white/10"><div className="h-full rounded-full bg-[#d7ae60] transition-all duration-300" style={{ width: `${Math.min(100, value)}%` }} /></div></div>; }

function Overlay({ sheet, close, filter, setFilter, config, setConfig, notify, unsupported }: { sheet: Sheet; close: () => void; filter: string; setFilter: (v: string) => void; config: string; setConfig: (v: string) => void; notify: (v: string) => void; unsupported: (v: string) => void }) {
  return <div className="absolute inset-0 z-30 flex items-end bg-black/35 backdrop-blur-[2px]" onClick={close}><GlassPanel className="animate-[slideUp_.35s_ease-out] w-full rounded-b-none rounded-t-[30px] p-5" onClick={(e) => e.stopPropagation()}>
    <div className="mx-auto mb-5 h-1 w-10 rounded-full bg-white/25" /><div className="mb-5 flex items-center justify-between"><div><p className="text-[10px] tracking-[.2em] text-[#d7ae60]">NOVACAM</p><h2 className="mt-1 text-xl">{sheet === "controls" ? "Manual controls" : sheet === "gallery" ? "Nova Gallery" : sheet === "config" ? "Camera Config" : "Settings"}</h2></div><IconButton label="Close" onClick={close}><X size={17} /></IconButton></div>
    {sheet === "controls" && <div className="space-y-4">{[["ISO","50"],["SHUTTER","1/120 s"],["EV","0.0"],["WHITE BALANCE","Auto"]].map(([a,b]) => <div key={a} className="flex items-center justify-between border-b border-white/10 pb-3"><span className="text-[10px] tracking-[.18em] text-white/50">{a}</span><button onClick={() => notify(`${a} control adjusted`)} className="flex items-center gap-2 text-sm text-[#f2d08a]">{b}<ChevronDown size={14} /></button></div>)}<div className="flex items-center justify-between rounded-2xl bg-white/5 p-3"><div><p className="text-sm">RAW capture</p><p className="mt-1 text-[10px] text-white/40">Pro DNG · preserves full sensor data</p></div><button onClick={() => unsupported("RAW capture")} className="rounded-full border border-white/10 px-3 py-1 text-[10px] text-white/50">Unavailable</button></div></div>}
    {sheet === "gallery" && <><div className="mb-5 flex gap-2 overflow-x-auto no-scrollbar">{["Photos","Live Photos","RAW","Favorites"].map((x) => <button key={x} onClick={() => x === "RAW" ? unsupported("RAW") : setFilter(x)} className={`shrink-0 rounded-full px-3 py-2 text-[10px] ${filter === x ? "bg-[#d7ae60] text-[#17130c]" : "bg-white/7 text-white/55"}`}>{x}</button>)}</div><div className="grid grid-cols-3 gap-2">{["#b77b4c,#293d3d","#d09d58,#26343a","#49625c,#d5ad69","#4c4e48,#a87248","#c5a577,#384c4c","#273537,#d5b06b"].map((g,i) => <div key={i} className="aspect-square rounded-xl" style={{ background: `linear-gradient(140deg,${g.split(",")[0]},${g.split(",")[1]})` }} />)}</div><p className="mt-4 text-center text-[10px] text-white/40">{filter} · 6 items</p></>}
    {sheet === "config" && <div className="space-y-3">{[["Vivid HD","Balanced detail with a warm highlight rolloff"],["Natural","Faithful tones, neutral sharpening"],["Night Detail","Longer stack for low light"]].map(([x,d]) => <button key={x} onClick={() => { setConfig(x); notify(`${x} activated`); close(); }} className={`flex w-full items-center justify-between rounded-2xl border p-4 text-left ${config === x ? "border-[#d7ae60]/60 bg-[#d7ae60]/10" : "border-white/10 bg-white/5"}`}><div><p className="text-sm">{x}</p><p className="mt-1 text-[10px] text-white/45">{d}</p></div>{config === x && <span className="h-2 w-2 rounded-full bg-[#d7ae60]" />}</button>)}</div>}
    {sheet === "settings" && <div className="space-y-1">{[["Save location","Nova Gallery"],["60 FPS video","Hardware dependent"],["Grid lines","Off"],["Haptics","On"]].map(([a,b]) => <button key={a} onClick={() => a === "60 FPS video" ? unsupported("60 FPS video") : notify(`${a} · ${b}`)} className="flex w-full items-center justify-between border-b border-white/10 py-4 text-left"><span className="text-sm">{a}</span><span className="flex items-center gap-1 text-[11px] text-white/45">{b}<ChevronDown size={13} /></span></button>)}<div className="mt-5 flex items-center gap-2 text-[10px] text-white/35"><Info size={13} /> NovaCam 1.4 · Computational Camera</div></div>}
  </GlassPanel></div>;
}

const css = `
@keyframes fadeIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
@keyframes slideUp{from{opacity:0;transform:translateY(35px)}to{opacity:1;transform:none}}
@keyframes spin{to{transform:rotate(360deg)}}
.camera-scene{background:linear-gradient(180deg,#1a282b 0%,#4e5d56 36%,#aa9a72 62%,#1b2727 100%);filter:saturate(.8)}
.scene-glow{position:absolute;inset:0;background:radial-gradient(ellipse at 70% 25%,rgba(246,198,111,.64),transparent 18%),radial-gradient(ellipse at 25% 48%,rgba(176,205,187,.42),transparent 30%)}
.scene-mountain{position:absolute;bottom:29%;left:-10%;width:120%;height:35%;background:linear-gradient(140deg,transparent 17%,#364946 18% 36%,transparent 37%),linear-gradient(32deg,transparent 24%,#627365 25% 47%,transparent 48%);opacity:.8}
.scene-lake{position:absolute;bottom:0;left:0;right:0;height:35%;background:linear-gradient(180deg,rgba(45,72,68,.72),rgba(13,29,30,.95));clip-path:polygon(0 17%,15% 11%,33% 16%,51% 8%,73% 18%,100% 8%,100% 100%,0 100%)}
.shutter-button{background:rgba(255,255,255,.12);box-shadow:0 0 0 1px rgba(0,0,0,.25),0 8px 30px rgba(0,0,0,.3)}
.no-scrollbar::-webkit-scrollbar{display:none}.no-scrollbar{scrollbar-width:none}
`;