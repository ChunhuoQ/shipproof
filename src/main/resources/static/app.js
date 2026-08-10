const $ = (selector) => document.querySelector(selector);
let latestReport = null;

const sample = {
  projectName: 'Spring PetClinic · public sample',
  repositoryUrl: 'https://github.com/spring-projects/spring-petclinic',
  demoUrl: 'https://spring-petclinic.github.io/',
  track: 'Autonomous Agents',
  socialUrl: 'https://x.com/springcentral',
  pitch: 'Spring PetClinic is a reference application for Java developers and framework teams who need a concrete, runnable example of Spring application architecture. The public repository documents local execution, tests, container workflows, application architecture, and licensing. This sample is used only to demonstrate how ShipProof verifies public evidence; ShipProof does not claim ownership of the project.'
};

$('#pitch').addEventListener('input', () => $('#pitch-count').textContent = $('#pitch').value.length);
$('#sample').addEventListener('click', () => {
  Object.entries(sample).forEach(([key, value]) => { const field = $('#' + key); if (field) field.value = value; });
  $('#pitch-count').textContent = $('#pitch').value.length;
});

$('#scan-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.currentTarget).entries());
  setLoading(true);
  try {
    const response = await fetch('/api/v1/scan', {
      method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload)
    });
    const body = await response.json();
    if (!response.ok) throw new Error(body.message || 'Scan failed');
    latestReport = body;
    renderReport(body);
    history.replaceState(null, '', '#' + body.reportId);
  } catch (error) {
    renderError(error.message);
  } finally {
    setLoading(false);
  }
});

function setLoading(active) {
  $('#run').disabled = active;
  $('#run span').textContent = active ? 'Collecting public evidence…' : 'Run seven-check audit';
  if (active) {
    $('#empty-state').hidden = true;
    $('#report-content').hidden = true;
    $('#loading-state').hidden = false;
    $('#report').classList.add('empty');
  }
}

function renderReport(report) {
  $('#report').classList.remove('empty');
  $('#empty-state').hidden = true;
  $('#loading-state').hidden = true;
  $('#report-content').hidden = false;
  const label = report.verdict === 'READY_TO_SUBMIT' ? 'READY TO SUBMIT'
    : report.verdict === 'NEEDS_WORK' ? 'NEEDS WORK' : 'BLOCKED BY EVIDENCE';
  $('#verdict').textContent = label;
  $('#verdict').className = report.verdict === 'BLOCKED' ? 'blocked' : report.verdict === 'NEEDS_WORK' ? 'needs-work' : '';
  $('#report-meta').textContent = `${report.reportId} · ${report.durationMs} ms · ${report.hardGateFailures} hard-gate failures`;
  $('#score').textContent = report.score;
  $('#score-ring').style.background = `conic-gradient(var(--signal) ${report.score * 3.6}deg,#303034 0deg)`;
  $('#check-count').textContent = `${report.passedChecks}/${report.totalChecks} passed`;
  $('#receipt-hash').textContent = report.proof.sha256;

  const max = {demo:20,repository:20,reproducibility:15,evidence:15,pitch:10,safety:10,submission:10};
  $('#score-grid').replaceChildren(...Object.entries(report.scorecard).map(([key, value]) => {
    const cell = document.createElement('div'); cell.className = 'score-cell';
    const name = document.createElement('span'); name.textContent = key;
    const score = document.createElement('b'); score.textContent = `${value}/${max[key]}`;
    cell.append(name, score); return cell;
  }));

  const ordered = [...report.findings].sort((a,b) => Number(a.passed) - Number(b.passed));
  $('#findings').replaceChildren(...ordered.map(finding => {
    const row = document.createElement('article'); row.className = `finding ${finding.passed ? 'pass' : 'fail'}`;
    const icon = document.createElement('span'); icon.className = 'finding-icon'; icon.textContent = finding.passed ? '✓' : '!';
    const copy = document.createElement('div');
    const title = document.createElement('h4'); title.textContent = finding.title;
    const evidence = document.createElement('p'); evidence.textContent = finding.evidence;
    copy.append(title, evidence);
    if (!finding.passed) { const fix = document.createElement('p'); fix.className = 'fix'; fix.textContent = 'FIX → ' + finding.remediation; copy.append(fix); }
    const points = document.createElement('span'); points.className = 'finding-score'; points.textContent = `${finding.points}/${finding.maxPoints}`;
    row.append(icon, copy, points); return row;
  }));
}

function renderError(message) {
  $('#report').classList.add('empty');
  $('#empty-state').hidden = false;
  $('#loading-state').hidden = true;
  $('#report-content').hidden = true;
  $('#empty-state h3').textContent = 'Scan stopped safely.';
  $('#empty-state p').textContent = message;
  $('#empty-state .crosshair').textContent = '!';
}

$('#download').addEventListener('click', () => {
  if (!latestReport) return;
  const blob = new Blob([JSON.stringify(latestReport, null, 2)], {type:'application/json'});
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob); link.download = `${latestReport.reportId}.json`; link.click();
  URL.revokeObjectURL(link.href);
});

(async function boot() {
  try {
    const response = await fetch('/api/v1/capabilities');
    if (!response.ok) throw new Error();
    const data = await response.json();
    $('#api-state').classList.add('live');
    $('#api-state').innerHTML = `<i></i> ${data.checks.length} checks online`;
  } catch (_) {
    $('#api-state').innerHTML = '<i></i> engine unavailable';
  }
  const reportId = location.hash.slice(1);
  if (/^spr_[a-f0-9]{16}$/.test(reportId)) {
    try {
      const response = await fetch('/api/v1/reports/' + reportId);
      if (response.ok) {
        latestReport = await response.json();
        renderReport(latestReport);
        document.querySelector('#scanner').scrollIntoView();
      }
    } catch (_) { /* Expired reports simply return to the scanner. */ }
  }
})();
