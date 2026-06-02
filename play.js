const synth = new Tone.Sampler({
    urls: {
        "A0": "A0.mp3",
        "C1": "C1.mp3",
        "D#1": "Ds1.mp3",
        "F#1": "Fs1.mp3",
        "A1": "A1.mp3",
        "C2": "C2.mp3",
        "D#2": "Ds2.mp3",
        "F#2": "Fs2.mp3",
        "A2": "A2.mp3",
        "C3": "C3.mp3",
        "D#3": "Ds3.mp3",
        "F#3": "Fs3.mp3",
        "A3": "A3.mp3",
        "C4": "C4.mp3",
        "D#4": "Ds4.mp3",
        "F#4": "Fs4.mp3",
        "A4": "A4.mp3",
        "C5": "C5.mp3",
        "D#5": "Ds5.mp3",
        "F#5": "Fs5.mp3",
        "A5": "A5.mp3",
        "C6": "C6.mp3",
        "D#6": "Ds6.mp3",
        "F#6": "Fs6.mp3",
        "A6": "A6.mp3",
        "C7": "C7.mp3",
        "D#7": "Ds7.mp3",
        "F#7": "Fs7.mp3",
        "A7": "A7.mp3",
        "C8": "C8.mp3"
    },
    release: 1,
    baseUrl: "https://tonejs.github.io/audio/salamander/"
}).toDestination();

let part = null;


function buildEvents(osmd) {
    const events = [];
    const cursor = osmd.cursor;
    cursor.reset();

    let currentBeat = 0;

    let safety = 0;

    while (!cursor.Iterator.EndReached && safety < 10000) {

        const voices = cursor.Iterator.CurrentVoiceEntries;

        
        const duration = cursor.Iterator.CurrentSourceTimestamp?.Duration?.RealValue || 1;

        for (const voice of voices || []) {
            for (const note of voice.Notes || []) {

                if (!note?.Pitch) continue;

                events.push({
                    time: currentBeat,     
                    note: safeNote(note.Pitch.ToString())
                });
            }
        }
        //maybe modify this to work dynamically
        currentBeat += duration /4; 
        

        cursor.next();
        safety++;
    }

    cursor.reset();

    return events;
}


function safeNote(value) {
    if (!value) return null;

    const raw = value;
    const keyMatch = raw.match(/Key:\s*([A-G]#?)/);
    const octaveMatch = raw.match(/octave:\s*(-?\d+)/);

    if (!keyMatch || !octaveMatch) {
        return null;
    }

   
    return `${keyMatch[1]}${Number(octaveMatch[1]) + 3}`;
}


async function start() {

    await Tone.start();
    await synth.loaded;

    stop();

    Tone.Transport.bpm.value = 120;

    part = new Tone.Part((time, value) => {

        const note = value.note;
        if (note) {
            synth.triggerAttackRelease(note, "8n", time);
        }

    }, window.cachedEvents);

    part.start(0);
    Tone.Transport.start();
}

function pause() {
    Tone.Transport.pause();
}

function stop() {

    if (part) {
        part.stop();
        part.dispose();
        part = null;
    }

    Tone.Transport.stop();
    Tone.Transport.cancel();

    window.osmd?.cursor?.reset();
    window.osmd?.cursor?.hide();
}

// expose globally
window.playback = {
    start,
    pause,
    stop,
    buildEvents
};