async function api(ruta, metodo = "GET", cuerpo = null) {
  const token = localStorage.getItem("token");
  const opciones = { method: metodo, headers: { "Content-Type": "application/json" } };
  if (token) opciones.headers.Authorization = "Bearer " + token;
  if (cuerpo !== null) opciones.body = JSON.stringify(cuerpo);
  const res = await fetch(ruta, opciones);
  if (res.status === 401) { localStorage.clear(); location.href = "index.html"; return; }
  const datos = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) throw new Error((datos?.codigo || res.status) + ": " + (datos?.error || "Error"));
  return datos;
}
function usuarioActual(){return localStorage.getItem("usuario") || "usuario"}
function rolActual(){return localStorage.getItem("rol") || ""}
function escapar(valor){return String(valor ?? "-").replace(/[&<>\"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;", "'":"&#039;"}[c]))}
function mostrarToast(mensaje){const el=document.querySelector("#toast");if(!el)return;el.textContent=mensaje;el.classList.add("show");setTimeout(()=>el.classList.remove("show"),3000)}
