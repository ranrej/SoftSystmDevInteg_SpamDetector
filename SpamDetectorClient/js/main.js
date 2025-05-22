const API_URL = "http://localhost:8080/spamDetector-1.0/api";
const DATA_FIELDS = ["file", "spamProbability", "actualClass"];
const results = [];

async function load_results() {
  // GET list of model results
  const response = await fetch(`${API_URL}/spam`);
  return await response.json();
}

async function load_stats() {
  // GET precision and accuracy values
  const precision_result = await fetch(`${API_URL}/spam/precision`);
  const accuracy_result = await fetch(`${API_URL}/spam/accuracy`);
  const precision = await precision_result.json();
  const accuracy = await accuracy_result.json();

  // Update values
  const precision_header = document.getElementById("precision-value");
  precision_header.textContent = JSON.parse(precision);
  const accuracy_header = document.getElementById("accuracy-value");
  accuracy_header.textContent = JSON.parse(accuracy);
}

function display_results(results) {
  const table = document.querySelector("#results-table tbody");

  // Create a new table row for each entry in the list
  for (const result of results) {
    const row = document.createElement("tr");

    // Populate the row by iterating through the data fields
    for (const field of DATA_FIELDS) {
      const entry = document.createElement("td");
      entry.innerText = result[field];
      row.appendChild(entry);
    }
    row.classList.add("hidden");

    // Add the new row to the main table
    table.appendChild(row);
    window.setTimeout(() => {
      row.classList.remove("hidden");
    }, 10);
  }
}

// Load statistics and display results once the site is loaded
async function initialize() {
  load_stats();
  display_results(await load_results());
}

// Load result dataset and statistic values for display once the site is loaded
window.onload = initialize;
