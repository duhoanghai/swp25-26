(function(){
  // Navigationslink basierend auf Dateinamen
  const path = (location.pathname.split("/").pop() || "dashboard.html").toLowerCase();
  const navLinks = document.querySelectorAll(".nav a[data-page]");
  navLinks.forEach(a => {
    const target = (a.getAttribute("href") || "").toLowerCase();
    if (target.endsWith(path)) a.setAttribute("aria-current","page");
    else a.removeAttribute("aria-current");
  });

  // Logout (zurück zu login)
  const logout = document.querySelector('[data-logout]');
  if (logout){
    logout.addEventListener("click", (e) => {
      e.preventDefault();
      localStorage.removeItem("auth_demo");
      location.href = "login.html";
    });
  }

  const isAppPage = document.body.dataset.page === "app";
  if (isAppPage){
    const ok = localStorage.getItem("auth_demo") === "1";
    if (!ok) location.href = "login.html";
  }

  // Patienten Demo Vorlage (nur wenn Elemente existieren)
  const sel = document.getElementById("p-select");
  const patientData = {
    anna: { g:"Weiblich", b:"23.10.2007", z:"aktiv (AC3-DP0)", r:"nicht verfügbar", t:"01.01.2026" },
    max:  { g:"Männlich",  b:"14.05.1999", z:"aktiv (MX1-KQ2)", r:"verfügbar",      t:"12.01.2026" },
    lea:  { g:"Divers",   b:"02.02.2003", z:"inaktiv",         r:"nicht verfügbar", t:"18.01.2026" }
  };

  function setPatient(key){
    const d = patientData[key];
    if (!d) return;
    const map = { "p-g": d.g, "p-b": d.b, "p-z": d.z, "p-r": d.r, "p-t": d.t };
    for (const id in map){
      const el = document.getElementById(id);
      if (el) el.textContent = map[id];
    }
  }

  if (sel){
    sel.addEventListener("change", (e) => setPatient(e.target.value));
    setPatient(sel.value);
  }
})();

