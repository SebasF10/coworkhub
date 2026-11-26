if(!localStorage.getItem("token")) location.href="index.html";
const rol=rolActual();document.querySelector("#user-role").textContent=rol;document.querySelector("#home-role").textContent=rol;document.querySelector("#welcome-user").textContent=usuarioActual();
if(rol!=="ADMIN"&&rol!=="RECEPCION") document.querySelectorAll(".staff-only").forEach(e=>e.style.display="none");
document.querySelectorAll(".nav-link").forEach(btn=>btn.addEventListener("click",()=>{document.querySelectorAll(".nav-link").forEach(x=>x.classList.remove("active"));btn.classList.add("active");document.querySelectorAll(".section").forEach(x=>x.classList.remove("active"));document.querySelector("#"+btn.dataset.section).classList.add("active");if(btn.dataset.section==="catalogos") cargarCatalogos();if(btn.dataset.section==="miembros") cargarMiembros();if(btn.dataset.section==="reservas") cargarReservas()}));
document.querySelectorAll("[data-go]").forEach(btn=>btn.addEventListener("click",()=>document.querySelector(`[data-section="${btn.dataset.go}"]`)?.click()));
document.querySelector("#logout")?.addEventListener("click",()=>{localStorage.clear();location.href="index.html"});
