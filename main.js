document.addEventListener("DOMContentLoaded", () => {

    const API_BASE_URL = "http://localhost:8080/music";
    const CURRENT_USER_ID = 2;

    let CURRENT_SCORE_ID = 0;
    let isPlaying = false;

    const getBtn = document.getElementById('getBtn');
    const uploadBtn = document.getElementById('uploadBtn');
    const fileInput = document.getElementById('fileInput');
    const playPauseBtn = document.getElementById("playPauseBtn");

    const scoreTitle = document.getElementById('scoreTitle');
    const scoreAuthor = document.getElementById('scoreAuthor');
    const placeholder = document.getElementById('placeholder');

    // OSMD GLOBAL
    window.osmd = new opensheetmusicdisplay.OpenSheetMusicDisplay("paper", {
        autoResize: true,
        backend: "svg",
        drawTitle: false
    });

    // CACHE FOR PLAYBACK
    window.cachedEvents = [];

    const drawer = document.getElementById("scoreDrawer");
    const handle = document.getElementById("drawerHandle");

    handle.addEventListener("click", () => {
        drawer.classList.toggle("open");
    });

   
    async function loadUserScores() {

        const res = await fetch(`${API_BASE_URL}/users/${CURRENT_USER_ID}/scores`);
        const scores = await res.json();

        const list = document.getElementById("scoreList");
        list.innerHTML = "";

        scores.forEach(score => {

            const card = document.createElement("div");
            card.className = "score-card";

            card.innerHTML = `
                <strong>${score.id}. ${score.title || "Untitled"}</strong>
                <br>
                <small>${score.author || "Unknown composer"}</small>
            `;

            card.addEventListener("click", () => {
                CURRENT_SCORE_ID = score.id;
                loadScoreById(score.id);
            });

            list.appendChild(card);
        });
    }

   
    async function loadScoreById(scoreId) {

        window.playback?.stop?.();

        const response = await fetch(`${API_BASE_URL}/scores/${scoreId}`);
        const scoreDTO = await response.json();

        scoreTitle.innerText = scoreDTO.title || "Untitled";
        scoreAuthor.innerText = scoreDTO.author || "Unknown author";

        const bytes = Uint8Array.from(atob(scoreDTO.data), c => c.charCodeAt(0));
        const zip = await JSZip.loadAsync(bytes.buffer);

        let xmlText = null;

        for (const filename of Object.keys(zip.files)) {
            if (filename.endsWith(".xml") && !filename.includes("container")) {
                xmlText = await zip.files[filename].async("string");
                break;
            }
        }

        await window.osmd.load(xmlText);
        window.osmd.render();
        window.osmd.cursor.hide();

        // build cache only once
        window.cachedEvents = window.playback?.buildEvents(window.osmd) || [];
        console.log("events:", window.cachedEvents.length);
    }

    
    getBtn.addEventListener('click', () => {

        const inputId = document.getElementById("idScore");
        CURRENT_SCORE_ID = inputId.value.trim();

        loadScoreById(CURRENT_SCORE_ID);
    });

    
    uploadBtn.addEventListener('click', async () => {

        const file = fileInput.files[0];
        if (!file) return alert("Select a file first");

        const arrayBuffer = await file.arrayBuffer();
        const uint8Array = new Uint8Array(arrayBuffer);

        let binary = "";
        uint8Array.forEach(byte => binary += String.fromCharCode(byte));

        const base64Data = btoa(binary);

        const response = await fetch(`${API_BASE_URL}/users/${CURRENT_USER_ID}/scores`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ data: base64Data })
        });

        const scoreDTO = await response.json();

        scoreTitle.innerText = scoreDTO.title || "Untitled";
        scoreAuthor.innerText = scoreDTO.author || "Unknown author";

        await loadScoreById(scoreDTO.id);

        loadUserScores();
    });

   
    document.getElementById("deleteBtn").addEventListener("click", async () => {

        await fetch(`${API_BASE_URL}/users/${CURRENT_USER_ID}/scores/${CURRENT_SCORE_ID}`, {
            method: "DELETE"
        });

        scoreTitle.innerText = "No score selected";
        scoreAuthor.innerText = "";
        placeholder.style.display = "block";

        window.osmd.clear();
        loadUserScores();
    });

    
    playPauseBtn.addEventListener("click", async () => {
        
        if (!window.osmd?.IsReadyToRender()) {
            console.log("not good");
            return;
        }

        if (isPlaying) {
            
            window.playback.pause();
            isPlaying = false;
        } else {
            
            await window.playback.start();
            isPlaying = true;
        }
    });

    loadUserScores();
});