import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  ArrowLeft, Check, ChevronRight, Download, Film, Heart, Info, MoreHorizontal,
  Play, Share2, Sparkles, Volume2, X,
} from "lucide-react";

type MediaKind = "Live Photo" | "Photo" | "Cinematic";

type Capture = {
  id: number;
  title: string;
  place: string;
  time: string;
  kind: MediaKind;
  tone: string;
  accent: string;
  duration?: string;
  favorite?: boolean;
};

const captures: Capture[] = [
  { id: 1, title: "Last light over Tahoe", place: "South Lake Tahoe · California", time: "Today, 18:42", kind: "Live Photo", tone: "linear-gradient(145deg,#dda66b 0%,#77575b 35%,#17363a 100%)", accent: "#f4c67b", duration: "2.9 s", favorite: true },
  { id: 2, title: "Quiet water", place: "Emerald Bay · California", time: "Today, 18:37", kind: "Live Photo", tone: "linear-gradient(145deg,#9cbdac 0%,#31545a 48%,#111f2b 100%)", accent: "#9bc6b7", duration: "3.0 s" },
  { id: 3, title: "Pine & fog", place: "Fallen Leaf Lake · California", time: "Today, 17:16", kind: "Photo", tone: "linear-gradient(145deg,#c7b58e 0%,#65756d 39%,#263a3d 100%)", accent: "#d6c99d" },
  { id: 4, title: "The long way home", place: "Highway 50 · Nevada", time: "Yesterday, 20:11", kind: "Cinematic", tone: "linear-gradient(145deg,#c17758 0%,#402e40 45%,#101c2a 100%)", accent: "#eaa77f", duration: "6.4 s" },
  { id: 5, title: "Morning cabin", place: "Meyers · California", time: "Yesterday, 08:02", kind: "Live Photo", tone: "linear-gradient(145deg,#ead0a2 0%,#8a7665 45%,#33494a 100%)", accent: "#f0d6a4", duration: "3.0 s" },
  { id: 6, title: "Blue hour", place: "Kiva Beach · California", time: "Aug 18, 21:03", kind: "Photo", tone: "linear-gradient(145deg,#5e8291 0%,#394b67 46%,#171f35 100%)", accent: "#94b9c6" },
];

function IconButton({ label, children, onClick }: { label: string; children: ReactNode; onClick: () => void }) {
  return <button aria-label={label} title={label} onClick={onClick} className="gallery-icon">{children}</button>;
}

function Thumb({ item, selected, onClick }: { item: Capture; selected: boolean; onClick: () => void }) {
  return (
    <button onClick={onClick} className={`thumb ${selected ? "thumb-selected" : ""}`} style={{ background: item.tone }}>
      <span className="thumb-shine" />
      {item.kind === "Live Photo" && <span className="live-mark"><span /> LIVE</span>}
      {item.kind === "Cinematic" && <Film size={13} />}
      {item.favorite && <Heart size={12} fill="currentColor" className="thumb-heart" />}
    </button>
  );
}

export function NovaCamGallery() {
  const [selectedId, setSelectedId] = useState(1);
  const [tab, setTab] = useState<"All" | "Live Photos" | "Favorites">("All");
  const [playing, setPlaying] = useState(false);
  const [favoriteIds, setFavoriteIds] = useState<number[]>([1]);
  const [toast, setToast] = useState("");
  const [exported, setExported] = useState(false);

  const selected = captures.find((capture) => capture.id === selectedId) ?? captures[0];
  const visibleCaptures = useMemo(() => captures.filter((item) => {
    if (tab === "Live Photos") return item.kind === "Live Photo";
    if (tab === "Favorites") return favoriteIds.includes(item.id);
    return true;
  }), [favoriteIds, tab]);

  useEffect(() => {
    if (!toast) return;
    const timeout = setTimeout(() => setToast(""), 2400);
    return () => clearTimeout(timeout);
  }, [toast]);

  useEffect(() => {
    if (!playing || selected.kind === "Photo") return undefined;
    const timeout = setTimeout(() => setPlaying(false), 2900);
    return () => clearTimeout(timeout);
  }, [playing, selected.kind]);

  const toggleFavorite = () => {
    setFavoriteIds((current) => current.includes(selected.id) ? current.filter((id) => id !== selected.id) : [...current, selected.id]);
    setToast(favoriteIds.includes(selected.id) ? "Removed from Favorites" : "Added to Favorites");
  };

  const exportLive = () => {
    setExported(true);
    setToast(selected.kind === "Live Photo" ? "Live Photo exported as .HEIC + .MOV" : "Photo exported to device");
  };

  return (
    <main className="gallery-app">
      <style>{css}</style>
      <header className="gallery-header">
        <div className="header-row">
          <IconButton label="Back to camera" onClick={() => setToast("Camera ready")}><ArrowLeft size={19} /></IconButton>
          <div className="header-title">
            <span className="eyebrow"><span className="gold-dot" /> NOVA GALLERY</span>
            <h1>Moments</h1>
          </div>
          <IconButton label="Gallery information" onClick={() => setToast("6 captures · 3.8 GB available")}><Info size={18} /></IconButton>
        </div>
        <div className="subhead-row">
          <p>Lake Tahoe <ChevronRight size={13} /> <span>August 19, 2024</span></p>
          <button className="sort-button" onClick={() => setToast("Sorted by newest")}>Newest <ChevronRight size={13} /></button>
        </div>
      </header>

      <section className="preview-section">
        <div className={`hero-preview ${playing ? "hero-playing" : ""}`} style={{ background: selected.tone }}>
          <div className="preview-glow" />
          <div className="preview-horizon" />
          <div className="preview-mountains" />
          <div className="preview-meta"><span>{selected.kind.toUpperCase()}</span><span>{selected.duration ?? "12 MP"}</span></div>
          {selected.kind !== "Photo" && (
            <button className="play-button" onClick={() => setPlaying((value) => !value)} aria-label={playing ? "Pause Live Photo" : "Play Live Photo"}>
              {playing ? <span className="pause-bars"><i /><i /></span> : <Play size={22} fill="currentColor" />}
            </button>
          )}
          <div className="preview-bottom">
            <div><h2>{selected.title}</h2><p>{selected.place}</p></div>
            <span className="capture-time">{selected.time}</span>
          </div>
          {playing && <div className="scrub-line"><span /></div>}
        </div>
        <div className="preview-actions">
          <button onClick={toggleFavorite} className={`action-button ${favoriteIds.includes(selected.id) ? "is-favorite" : ""}`}><Heart size={17} fill={favoriteIds.includes(selected.id) ? "currentColor" : "none"} /><span>{favoriteIds.includes(selected.id) ? "Favorited" : "Favorite"}</span></button>
          <button onClick={() => setToast("Share sheet opened")} className="action-button"><Share2 size={17} /><span>Share</span></button>
          <button onClick={exportLive} className="export-button"><Download size={17} /><span>{exported ? "Exported" : "Export"}</span></button>
        </div>
      </section>

      <section className="library-section">
        <div className="section-heading"><div><span className="eyebrow">YOUR CAPTURES</span><h2>All moments <span>{visibleCaptures.length}</span></h2></div><button className="more-button" onClick={() => setToast("Selection tools opened")}><MoreHorizontal size={19} /></button></div>
        <div className="filter-tabs">
          {(["All", "Live Photos", "Favorites"] as const).map((filter) => <button key={filter} className={tab === filter ? "filter-active" : ""} onClick={() => setTab(filter)}>{filter}{filter === "Live Photos" && <span>3</span>}</button>)}
        </div>
        <div className="thumbnail-grid">
          {visibleCaptures.map((item) => <Thumb key={item.id} item={{ ...item, favorite: favoriteIds.includes(item.id) }} selected={selected.id === item.id} onClick={() => { setSelectedId(item.id); setPlaying(false); setExported(false); }} />)}
        </div>
        <p className="library-footer"><Sparkles size={13} /> NovaCam automatically preserves the decisive moment before and after each Live Photo.</p>
      </section>
      {toast && <div className="toast"><Check size={14} /> {toast}</div>}
    </main>
  );
}

const css = `
@import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Manrope:wght@400;500;600;700&display=swap');
*{box-sizing:border-box}.gallery-app{min-height:100dvh;background:#101718;color:#f3eee2;font-family:'Manrope',ui-sans-serif,sans-serif;overflow:hidden;position:relative;padding-bottom:24px}
.gallery-app:before{content:"";position:fixed;inset:0;pointer-events:none;opacity:.035;background-image:url("data:image/svg+xml,%3Csvg viewBox='0 0 180 180' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.8' numOctaves='3'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.7'/%3E%3C/svg%3E")}
.gallery-header{padding:22px 22px 17px;border-bottom:1px solid rgba(255,255,255,.08);background:rgba(13,19,19,.88);backdrop-filter:blur(18px)}.header-row,.subhead-row,.section-heading{display:flex;align-items:center;justify-content:space-between}.header-title{text-align:center}.eyebrow{font:500 9px 'DM Mono',monospace;letter-spacing:.17em;color:#c8a66c}.gold-dot{display:inline-block;width:5px;height:5px;background:#e4b86e;border-radius:50%;margin:0 5px 1px 0}.header-title h1{font-size:23px;letter-spacing:-.04em;margin:3px 0 0;font-weight:600}.gallery-icon{width:37px;height:37px;border:1px solid rgba(255,255,255,.11);border-radius:50%;display:grid;place-items:center;background:rgba(255,255,255,.045);color:#ddd8cb;cursor:pointer;transition:transform .2s,background .2s}.gallery-icon:hover{background:rgba(228,184,110,.13);transform:translateY(-1px)}.subhead-row{margin-top:20px;font-size:11px;color:#e7dfcf}.subhead-row p{display:flex;align-items:center;gap:3px;margin:0}.subhead-row p span{color:rgba(255,255,255,.4)}.sort-button,.more-button{color:rgba(255,255,255,.55);font-size:10px;background:none;border:0;display:flex;align-items:center;gap:4px;cursor:pointer}.preview-section{padding:18px 18px 0}.hero-preview{height:285px;border-radius:25px;position:relative;overflow:hidden;box-shadow:0 18px 42px rgba(0,0,0,.27);transition:transform .3s}.hero-preview.hero-playing{transform:scale(.988)}.preview-glow{position:absolute;inset:0;background:radial-gradient(ellipse at 74% 30%,rgba(255,222,156,.72),transparent 23%),radial-gradient(ellipse at 18% 57%,rgba(195,220,201,.3),transparent 31%)}.preview-horizon{position:absolute;bottom:0;left:0;right:0;height:42%;background:linear-gradient(180deg,rgba(33,73,72,.4),#0f2a31);clip-path:polygon(0 19%,16% 7%,33% 17%,54% 3%,71% 15%,100% 0,100% 100%,0 100%)}.preview-mountains{position:absolute;bottom:31%;left:-12%;width:125%;height:38%;opacity:.7;background:linear-gradient(150deg,transparent 18%,#345253 19% 39%,transparent 40%),linear-gradient(28deg,transparent 24%,#66716b 25% 45%,transparent 46%)}.preview-meta{position:absolute;top:17px;left:18px;right:18px;display:flex;justify-content:space-between;font:500 9px 'DM Mono',monospace;letter-spacing:.13em;color:rgba(255,255,255,.72)}.play-button{position:absolute;top:50%;left:50%;transform:translate(-50%,-54%);width:58px;height:58px;border-radius:50%;border:1px solid rgba(255,255,255,.62);background:rgba(14,23,24,.32);backdrop-filter:blur(8px);display:grid;place-items:center;color:#f7f0df;cursor:pointer;padding-left:3px;transition:transform .25s,background .25s}.play-button:hover{transform:translate(-50%,-54%) scale(1.07);background:rgba(14,23,24,.54)}.pause-bars{display:flex;gap:4px}.pause-bars i{height:16px;width:3px;border-radius:2px;background:currentColor}.preview-bottom{position:absolute;bottom:17px;left:18px;right:18px;display:flex;align-items:end;justify-content:space-between}.preview-bottom h2{margin:0;font-size:18px;letter-spacing:-.035em;font-weight:600}.preview-bottom p{margin:5px 0 0;font-size:10px;color:rgba(255,255,255,.62)}.capture-time{font:400 9px 'DM Mono',monospace;color:rgba(255,255,255,.58);white-space:nowrap}.scrub-line{position:absolute;bottom:0;left:0;right:0;height:3px;background:rgba(255,255,255,.17)}.scrub-line span{display:block;width:68%;height:100%;background:#e4b86e;animation:scrub 2.9s linear}.preview-actions{display:flex;gap:8px;padding:13px 1px 3px}.action-button,.export-button{height:39px;flex:1;border:1px solid rgba(255,255,255,.1);border-radius:12px;background:#182121;color:rgba(255,255,255,.62);font-size:10px;display:flex;align-items:center;justify-content:center;gap:7px;cursor:pointer;transition:background .2s,color .2s,transform .2s}.action-button:hover,.export-button:hover{transform:translateY(-1px);background:#202c2b;color:#f5eedf}.action-button.is-favorite{color:#e9b18f}.export-button{flex:1.16;background:#d7ae60;border-color:#d7ae60;color:#17130c;font-weight:700}.export-button:hover{background:#ecc477;color:#17130c}.library-section{padding:25px 18px 0}.section-heading h2{margin:5px 0 0;font-size:18px;font-weight:600;letter-spacing:-.035em}.section-heading h2 span{font:400 10px 'DM Mono',monospace;color:rgba(255,255,255,.38);vertical-align:middle;margin-left:5px}.more-button{height:32px;width:32px;border:1px solid rgba(255,255,255,.1);border-radius:50%;justify-content:center}.filter-tabs{display:flex;gap:21px;margin:18px 0 12px;border-bottom:1px solid rgba(255,255,255,.08)}.filter-tabs button{position:relative;border:0;background:none;color:rgba(255,255,255,.4);font-size:10px;padding:0 0 11px;cursor:pointer}.filter-tabs button.filter-active{color:#e7d9bb}.filter-tabs button.filter-active:after{content:"";position:absolute;bottom:-1px;left:0;right:0;height:2px;background:#d7ae60}.filter-tabs button span{font:400 9px 'DM Mono',monospace;background:rgba(215,174,96,.15);color:#d7ae60;border-radius:7px;margin-left:5px;padding:2px 4px}.thumbnail-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:8px}.thumb{aspect-ratio:1;border:0;border-radius:14px;position:relative;overflow:hidden;color:rgba(255,255,255,.8);display:flex;align-items:flex-start;justify-content:center;padding-top:9px;cursor:pointer;transition:transform .2s,box-shadow .2s}.thumb:hover{transform:translateY(-2px)}.thumb-selected{box-shadow:0 0 0 2px #d7ae60}.thumb-shine{position:absolute;inset:0;background:radial-gradient(circle at 72% 22%,rgba(255,226,154,.48),transparent 22%),linear-gradient(135deg,transparent 45%,rgba(15,40,42,.46) 46%)}.live-mark{position:relative;display:flex;align-items:center;gap:4px;font:500 7px 'DM Mono',monospace;letter-spacing:.08em;text-shadow:0 1px 4px #172525}.live-mark span{width:5px;height:5px;background:#e9b86d;border-radius:50%}.thumb svg{position:relative}.thumb-heart{position:absolute;right:9px;bottom:9px;color:#f0c3a1}.library-footer{display:flex;align-items:flex-start;gap:7px;margin:16px 2px 0;color:rgba(255,255,255,.34);font-size:9px;line-height:1.5}.library-footer svg{color:#d5aa64;flex:none;margin-top:1px}.toast{position:fixed;bottom:20px;left:50%;transform:translateX(-50%);z-index:3;background:#e8d4ae;color:#1a1914;border-radius:100px;padding:10px 15px;display:flex;align-items:center;gap:7px;font-size:10px;font-weight:600;box-shadow:0 8px 24px rgba(0,0,0,.3);white-space:nowrap;animation:toast-in .3s ease-out}@keyframes scrub{from{width:0}to{width:68%}}@keyframes toast-in{from{opacity:0;transform:translate(-50%,8px)}to{opacity:1;transform:translate(-50%,0)}}@media(min-width:560px){.gallery-app{max-width:470px;margin:0 auto;box-shadow:0 0 80px rgba(0,0,0,.35)}}
`;