// Zentrale API-Konfiguration
const API_BASE = localStorage.getItem('api_base')
    || 'http://localhost:8080';
// API_BASE verfügbar für inline scripts (z.B. login.html)
window.API_BASE = String(API_BASE).replace(/\/+$/g, "");

// liest das JWT aus localStorage
function getAuthToken(){
  return localStorage.getItem('auth_token') || '';
}


//Fetch-Wrapper
async function apiFetch(path, options = {}){
  const base = (window.API_BASE || '').replace(/\/+$/g,'');
  const url = path.startsWith('http') ? path : base + path;
  const headers = new Headers(options.headers || {});
  const token = getAuthToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  // JSON automatisch, wenn body ein Objekt ist
  if (!headers.has('Content-Type') && options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    headers.set('Content-Type','application/json');
    options.body = JSON.stringify(options.body);
  }


//Request ausführen, bei Fehlern Exception
  const res = await fetch(url, { ...options, headers });
  if (!res.ok){
    const txt = await res.text().catch(()=> "");
    throw new Error(`API-Fehler (${res.status})${txt ? ": " + txt : ""}`);
  }
  return res;
}


//ISO-Datum/Uhrzeit
function fmtDateTime(iso){
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString('de-DE', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' });
}


(function(){
  // Navigationslink basierend auf Dateinamen
  const path = (location.pathname.split("/").pop() || "dashboard.html").toLowerCase();
  // sammelt alle Navigationslinks in der Sidebar
  const navLinks = document.querySelectorAll(".nav a[data-page]");
  // aktueller Menüpunkt wird farblich hervorgehoben
  navLinks.forEach(a => {
    const target = (a.getAttribute("href") || "").toLowerCase();
    if (target.endsWith(path)) a.setAttribute("aria-current","page");
  });


  // Logout (zurück zu login)
  const logout = document.querySelector('[data-logout]');
  if (logout){
    logout.addEventListener("click", (e) => {
      e.preventDefault();
      localStorage.removeItem("auth_token");
      localStorage.removeItem("auth_role");
      localStorage.removeItem("auth_subjectRef");
      localStorage.removeItem("auth_username");
      location.href = "login.html";
    });
  }


  // Zugriffschutz für Seiten (JWT muss vorhanden sein)
  const isAppPage = document.body.dataset.page === "app";
  if (isAppPage){
    const token = localStorage.getItem("auth_token");
    if (!token) location.href = "login.html";
  }


  // --- Angemeldeten Arzt im Header anzeigen ---
  (function showLoggedInDoctor(){
    const el = document.getElementById('loggedInDoctor');
    if (!el) return;
    const username = localStorage.getItem('auth_username');
    if (username) el.textContent = username;
  })();


  // --- Dashboard ---
  async function loadDashboardIfPresent(){
    const elPatients = document.getElementById("statPatients");
    const elReports  = document.getElementById("statReports");
    const elActWrap  = document.getElementById("recentActivities");
    if (!elPatients && !elReports && !elActWrap) return;


//Statistiken + letzte Aktivitäten
    try{
      const res = await apiFetch("/api/arzt/stats");
      const data = await res.json();

      if (elPatients) elPatients.textContent = String(data.patientsCount ?? "—");
      if (elReports)  elReports.textContent  = String(data.reportsCount ?? "—");

      if (elActWrap){
        elActWrap.innerHTML = "";
        const items = Array.isArray(data.recentReports) ? data.recentReports : [];
        if (items.length === 0){
          const d = document.createElement("div");
          d.textContent = "Keine Aktivitäten gefunden.";
          elActWrap.appendChild(d);
        } else {
          for (const r of items){
            const d = document.createElement("div");
            const who = r.patientDisplay || r.patientRef || "Unbekannt";
            d.textContent = `• ${fmtDateTime(r.createdAt)} – Bericht verfügbar (${who})`;
            elActWrap.appendChild(d);
          }
        }
      }
    } catch(err){
      console.error(err);
      if (elPatients) elPatients.textContent = "—";
      if (elReports)  elReports.textContent  = "—";
      if (elActWrap)  elActWrap.textContent  = "Dashboard-Daten konnten nicht geladen werden.";
    }
  }


  // --- Patient:innen-Seite ---
  async function loadPatientsIfPresent(){
    const sel = document.getElementById("p-select");
    const refreshBtn = document.getElementById("refresh-btn");
    const detailsDiv = document.getElementById("patient-details");
    if (!sel) return;

//UI-Elemente für Detailansicht
    const elements = {
      birth: document.getElementById("p-b"),
      code: document.getElementById("p-z"),
      appointment: document.getElementById("p-apt"),
      report: document.getElementById("p-r"),
      created: document.getElementById("p-c"),
      patientId: document.getElementById("p-id"),
      givenName: document.getElementById("p-given"),
      familyName: document.getElementById("p-family"),
      username: document.getElementById("p-username")
    };

    // Alle Felder auf "—" setzen
    function resetDetails() {
      Object.values(elements).forEach(el => {
        if (el) el.textContent = "—";
      });
      if (detailsDiv) detailsDiv.style.display = "none";
    }

    // Patienten laden
    async function loadPatientList() {
      try {
        sel.innerHTML = '<option value="">Lade Patient:innen…</option>';
        
        const res = await apiFetch("/api/arzt/patients");
        const patients = await res.json();
        
        sel.innerHTML = '<option value="">Bitte Patient:in auswählen…</option>';
        
        if (Array.isArray(patients) && patients.length > 0) {
          patients.forEach(patient => {
            const displayName = patient.displayName || 
                               `${patient.familyName || ''}, ${patient.givenName || ''}` || 
                               patient.username || 
                               patient.patientId;
            const option = new Option(
              displayName,
              patient.patientId
            );
            sel.appendChild(option);
          });
          console.log("Patienten geladen:", patients.length);
        } else {
          sel.innerHTML = '<option value="">Keine Patient:innen gefunden</option>';
          console.log("Keine Patienten in API Antwort");
        }
      } catch (error) {
        console.error("Fehler beim Laden der Patienten:", error);
        sel.innerHTML = '<option value="">Fehler beim Laden</option>';
      }
    }

    // Patientendetails laden
    async function loadPatientDetails(patientId) {
      if (!patientId) {
        resetDetails();
        return;
      }

      try {
        console.log("Lade Details für Patient:", patientId);
        
        try {
          const res = await apiFetch(`/api/arzt/patients/${patientId}/details`);
          const data = await res.json();
          
          console.log("Details API Antwort:", data);
          
          // Details anzeigen
          if (elements.birth) elements.birth.textContent = data.birthDate || "—";
          if (elements.appointment) elements.appointment.textContent = (data.latestAppointmentDate || data.appointmentDate || "—");
          if (elements.code) {
            elements.code.textContent = data.latestCodeStatus || "Kein Code";
            elements.code.className = "code-status " + getCodeStatusClass(data.latestCodeStatus);
          }
          if (elements.report) {
            elements.report.textContent = data.reportsCount > 0 
              ? `${data.reportsCount} Berichte verfügbar`
              : "Keine Berichte";
          }
          if (elements.created) elements.created.textContent = fmtDateTime(data.latestReportCreatedAt);
          if (elements.patientId) elements.patientId.textContent = patientId;
          if (elements.givenName) elements.givenName.textContent = data.givenName || "—";
          if (elements.familyName) elements.familyName.textContent = data.familyName || "—";
          if (elements.username) elements.username.textContent = data.username || "—";
          
        } catch (detailError) {
          console.log("Details-Endpoint fehlgeschlagen, versuche Patientenliste:", detailError);
          
          // Fallback: Patientenliste durchsuchen
          const res = await apiFetch("/api/arzt/patients");
          const patients = await res.json();
          const patient = patients.find(p => p.patientId === patientId);
          
          if (patient) {
            console.log("Patient in Liste gefunden:", patient);
            if (elements.birth) elements.birth.textContent = patient.birthDate || "—";
            if (elements.patientId) elements.patientId.textContent = patientId;
            if (elements.givenName) elements.givenName.textContent = patient.givenName || "—";
            if (elements.familyName) elements.familyName.textContent = patient.familyName || "—";
            if (elements.username) elements.username.textContent = patient.username || "—";
            
            elements.code.textContent = "Details nicht verfügbar";
            elements.report.textContent = "Details nicht verfügbar";
          }
        }
        
        if (detailsDiv) detailsDiv.style.display = "block";
        
      } catch (error) {
        console.error("Fehler beim Laden der Details:", error);
        resetDetails();
      }
    }
    

    // Hilfsfunktion für Status-Farben
    function getCodeStatusClass(status) {
      switch(status) {
        case "ACTIVE": return "status-active";
        case "USED": return "status-used";
        case "EXPIRED": return "status-expired";
        case "REVOKED": return "status-revoked";
        default: return "status-none";
      }
    }


    // Event Listener
    sel.addEventListener("change", (e) => {
      if (e.target.value) {
        loadPatientDetails(e.target.value);
      } else {
        resetDetails();
      }
    });

    if (refreshBtn) {
      refreshBtn.addEventListener("click", () => {
        loadPatientList();
        resetDetails();
      });
    }

    // Initial laden
    await loadPatientList();
  }

  // --- Bericht-Seite (Arzt): Tabelle ---
  function formatPatientName(p){
    if (!p) return "—";
    const dn = p.displayName || p.display || p.patientDisplay;
    if (dn && String(dn).trim()) return String(dn).trim();
    const family = (p.familyName || "").trim();
    const given  = (p.givenName || "").trim();
    const combo = [family, given].filter(Boolean).join(", ");
    if (combo) return combo;
    return (p.username || p.patientId || "—");
  }


//lädt Patient:innen und baut Map
  async function loadPatientNameMap(){
    const map = new Map();
    try{
      const res = await apiFetch("/api/arzt/patients");
      const patients = await res.json();
      for (const p of (patients || [])){
        const pid = p.patientId || "";
        if (!pid) continue;
        map.set(String(pid), formatPatientName(p));
        // optional: Patient/<id> key as well
        map.set(`Patient/${pid}`, formatPatientName(p));
      }
    } catch (e){
      // ignore
    }
    return map;
  }


//PDF-Report für Patient herunterladen
  async function downloadPatientReportPdf(patientId, days = 14) {
    const token = localStorage.getItem('auth_token');
    if (!token) {
      location.href = 'login.html';
      return;
    }


//PDF-Endpoint direkt mit fetch
    const res = await fetch(`${window.API_BASE}/api/reports/patient/${encodeURIComponent(patientId)}/pdf?days=${days}`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    if (!res.ok) {
      const txt = await res.text().catch(() => '');
      alert(`Backend-Fehler beim PDF-Download (${res.status}):\n${txt}`);
      return;
    }


//Browser-Download über ObjectURL
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `sleep-report-${patientId}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }


//Berichte-Tabelle rendern
  async function loadReportTableIfPresent(){
    const tbody = document.getElementById("reportTable");
    if (!tbody) return;

    const search = document.getElementById("reportSearch");
    const patientNameMap = await loadPatientNameMap();

    function render(rows){
      tbody.innerHTML = "";
      if (!rows || rows.length === 0){
        const tr = document.createElement("tr");
        tr.innerHTML = `<td colspan="3" style="color:var(--muted);">Keine Einträge.</td>`;
        tbody.appendChild(tr);
        return;
      }

      for (const r of rows){
        const tr = document.createElement("tr");
        const date = r.latestReportCreatedAt ? fmtDateTime(r.latestReportCreatedAt) : "—";
        const pid  = r.patientId;
        const pname = patientNameMap.get(String(pid || "")) 
          || patientNameMap.get(String(r.patientRef || "")) 
          || "";

        r._patientName = pname;

        tr.innerHTML = `
          <td>${r._patientName || r.patientDisplay || r.display || r.patientRef || pid || "—"}</td>
          <td>${date}</td>
          <td>
            <button class="btn btn-ghost js-download-pdf" type="button" ${r.reportsCount>0 ? "" : "disabled"} data-patient-id="${pid}">
              PDF herunterladen
            </button>
          </td>
        `;
        tbody.appendChild(tr);
      }

      tbody.querySelectorAll(".js-download-pdf").forEach(btn=>{
        btn.addEventListener("click", ()=>{
          const pid = btn.getAttribute("data-patient-id");
          if (pid) downloadPatientReportPdf(pid, 14);
        });
      });
    }

    try{
      const res = await apiFetch("/api/arzt/patient-reports");
      const rows = await res.json();
      let current = Array.isArray(rows) ? rows : [];
      render(current);

      if (search){
        search.addEventListener("input", ()=>{
          const q = (search.value || "").toLowerCase();
          const filtered = current.filter(r =>
            String(r._patientName || r.patientDisplay || r.display || r.patientRef || r.patientId || "").toLowerCase().includes(q)
          );
          render(filtered);
        });
      }
    } catch(err){
      console.error(err);
      tbody.innerHTML = `<tr><td colspan="3" style="color:var(--muted);">Fehler beim Laden der Berichte.</td></tr>`;
    }
  }

  // Start
  loadDashboardIfPresent();
  loadPatientsIfPresent();
  loadReportTableIfPresent();

})();