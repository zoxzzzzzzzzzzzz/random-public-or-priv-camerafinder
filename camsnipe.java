// ==UserScript==
// @name         zoxz cam sniper - direct ip cam / webcam feed sniper w/ troll mode 2026
// @namespace    https://zoxz.shadow
// @version      1.3.0
// @description  hunts direct live camera streams (video/audio) + troll mode for mic-enabled cams + location picker (restaurants/rooms/all) + multi-engine blast
// @author       zoxz
// @match        https://*.google.com/*
// @match        https://*.google.*/*
// @match        https://www.bing.com/*
// @match        https://duckduckgo.com/*
// @match        https://yandex.com/*
// @match        https://search.brave.com/*
// @match        https://searx.be/*
// @grant        GM_registerMenuCommand
// @grant        GM_addStyle
// @run-at       document-end
// ==/UserScript==

(function() {
    'use strict';

    
    GM_addStyle(`
        #zoxz-panel {
            position: fixed;
            top: 12px;
            right: 12px;
            z-index: 999999;
            background: #0a0e14;
            color: #d0d8e0;
            border: 1px solid #1f2a38;
            border-radius: 6px;
            padding: 12px;
            width: 440px;
            font-family: monospace;
            box-shadow: 0 12px 40px rgba(0,0,0,0.9);
            user-select: none;
        }
        #zoxz-panel h3 {
            margin: 0 0 12px 0;
            font-size: 17px;
            color: #ff4444;
            text-align: center;
        }
        #zoxz-dork {
            width: 100%;
            padding: 9px;
            background: #11171f;
            border: 1px solid #1f2a38;
            color: #d0d8e0;
            font-family: monospace;
            border-radius: 4px;
            margin-bottom: 12px;
            resize: vertical;
            min-height: 80px;
        }
        .zoxz-btn {
            background: #aa0000;
            color: #fff;
            border: none;
            padding: 7px 14px;
            margin: 5px 3px 0 0;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
        }
        .zoxz-btn:hover { background: #dd0000; }
        .zoxz-btn-copy { background: #3a444f; }
        .zoxz-btn-copy:hover { background: #4a545f; }
        .zoxz-category {
            margin: 14px 0 7px;
            font-weight: bold;
            color: #ff8888;
            cursor: pointer;
        }
        .zoxz-dorks, .zoxz-troll-dorks {
            display: none;
            margin-left: 10px;
        }
        .zoxz-dorks.show, .zoxz-troll-dorks.show { display: block; }
        .zoxz-dork-item {
            margin: 6px 0;
            padding: 6px 10px;
            background: #181f28;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
        }
        .zoxz-dork-item:hover { background: #252e3a; }
        #zoxz-multi, #zoxz-troll {
            margin: 12px 0;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
        }
        #zoxz-multi input, #zoxz-troll input { accent-color: #ff4444; }
        #zoxz-location {
            background: #11171f;
            color: #d0d8e0;
            border: 1px solid #1f2a38;
            border-radius: 4px;
            padding: 4px;
        }
        #zoxz-status {
            margin-top: 10px;
            font-size: 12px;
            color: #8899aa;
            text-align: center;
        }
    `);

    const panel = document.createElement('div');
    panel.id = 'zoxz-panel';
    panel.innerHTML = `
        <h3>zoxz cam sniper + troll mode</h3>
        <textarea id="zoxz-dork" placeholder="direct feed dork... video/audio/mic streams"></textarea>

        <div id="zoxz-troll">
            <input type="checkbox" id="zoxz-trollmode">
            <label for="zoxz-trollmode">troll mode (mic/audio cams)</label>
            <select id="zoxz-location">
                <option value="all">pick troll: all</option>
                <option value="restaurants">restaurants</option>
                <option value="rooms">rooms</option>
            </select>
        </div>

        <div id="zoxz-multi">
            <input type="checkbox" id="zoxz-allengines" checked>
            <label for="zoxz-allengines">blast all engines → direct feeds</label>
        </div>

        <div id="zoxz-buttons"></div>
        <div id="zoxz-status"></div>

        <!-- video-only categories (default) -->
        <div class="zoxz-category video-cat">↴ direct axis mjpg streams</div>
        <div class="zoxz-dorks video-dorks" id="cat-axis">
            <div class="zoxz-dork-item">inurl:axis-cgi/mjpg/video.cgi</div>
            <div class="zoxz-dork-item">inurl:axis-cgi/jpg/image.cgi</div>
            <div class="zoxz-dork-item">inurl:axis-cgi/mjpg/video swf</div>
            <div class="zoxz-dork-item">intitle:"live view" inurl:axis-cgi/mjpg</div>
        </div>

        <div class="zoxz-category video-cat">↴ viewerframe direct refresh/motion</div>
        <div class="zoxz-dorks video-dorks" id="cat-viewer">
            <div class="zoxz-dork-item">inurl:ViewerFrame?Mode=Refresh</div>
            <div class="zoxz-dork-item">inurl:ViewerFrame?Mode=Motion</div>
            <div class="zoxz-dork-item">inurl:viewerframe?mode=refresh</div>
            <div class="zoxz-dork-item">inurl:ViewerFrame?Mode=Refresh intitle:"Network Camera"</div>
        </div>

        <div class="zoxz-category video-cat">↴ generic direct jpg/mjpg streams</div>
        <div class="zoxz-dorks video-dorks" id="cat-generic">
            <div class="zoxz-dork-item">inurl:/video.mjpg</div>
            <div class="zoxz-dork-item">inurl:/mjpg/video.mjpg</div>
            <div class="zoxz-dork-item">inurl:/jpg/image.jpg</div>
            <div class="zoxz-dork-item">inurl:/cam.jpg OR inurl:/image.jpg "live"</div>
            <div class="zoxz-dork-item">inurl:/live.jpg OR inurl:/snapshot.jpg</div>
        </div>

        <div class="zoxz-category video-cat">↴ webcamxp / webcam7 direct</div>
        <div class="zoxz-dorks video-dorks" id="cat-webcamxp">
            <div class="zoxz-dork-item">intitle:"webcamXP" inurl:video.mjpg</div>
            <div class="zoxz-dork-item">intitle:"webcam 7" inurl:video.mjpg</div>
            <div class="zoxz-dork-item">inurl:/video.mjpg intitle:"webcam"</div>
        </div>

        <div class="zoxz-category video-cat">↴ android ip webcam direct</div>
        <div class="zoxz-dorks video-dorks" id="cat-android">
            <div class="zoxz-dork-item">intitle:"IP Webcam" inurl:/video</div>
            <div class="zoxz-dork-item">inurl:/video.mjpg intitle:"IP Webcam"</div>
            <div class="zoxz-dork-item">inurl:/shot.jpg intitle:"IP Webcam"</div>
        </div>

        <!-- troll mode categories (audio/mic) -->
        <div class="zoxz-category troll-cat" style="display:none;">↴ foscam direct audio/two-way</div>
        <div class="zoxz-troll-dorks troll-dorks" id="cat-foscam" style="display:none;">
            <div class="zoxz-dork-item">inurl:"/cgi-bin/CGIProxy.fcgi?cmd=" "Foscam"</div>
            <div class="zoxz-dork-item">intitle:"Foscam IP Camera" inurl:videostream.cgi</div>
            <div class="zoxz-dork-item">inurl:"/cgi-bin/CGIStream.cgi?cmd=GetMJPGStream" "Foscam"</div>
            <div class="zoxz-dork-item">intitle:"Foscam" inurl:"/audio.cgi"</div>
        </div>

        <div class="zoxz-category troll-cat" style="display:none;">↴ android ip webcam audio streams</div>
        <div class="zoxz-troll-dorks troll-dorks" id="cat-android-audio" style="display:none;">
            <div class="zoxz-dork-item">intitle:"IP Webcam" inurl:/audio.wav</div>
            <div class="zoxz-dork-item">intitle:"IP Webcam" inurl:/audio.opus</div>
            <div class="zoxz-dork-item">inurl:/audio.mjpg intitle:"IP Webcam"</div>
        </div>

        <div class="zoxz-category troll-cat" style="display:none;">↴ axis with audio cgi</div>
        <div class="zoxz-troll-dorks troll-dorks" id="cat-axis-audio" style="display:none;">
            <div class="zoxz-dork-item">inurl:axis-cgi/audio/recv.cgi</div>
            <div class="zoxz-dork-item">inurl:axis-cgi/audio/send.cgi</div>
            <div class="zoxz-dork-item">intitle:"live view" inurl:axis-cgi/audio</div>
        </div>

        <div class="zoxz-category troll-cat" style="display:none;">↴ generic audio-inclusive streams</div>
        <div class="zoxz-troll-dorks troll-dorks" id="cat-generic-audio" style="display:none;">
            <div class="zoxz-dork-item">inurl:/audio.cgi intitle:"network camera"</div>
            <div class="zoxz-dork-item">inurl:/mic/audio.wav "live"</div>
            <div class="zoxz-dork-item">inurl:/audio.mjpg OR inurl:/videoaudio.mjpg</div>
        </div>
    `;

    document.body.appendChild(panel);

    const textarea = document.getElementById('zoxz-dork');
    const status = document.getElementById('zoxz-status');
    const trollCheckbox = document.getElementById('zoxz-trollmode');
    const locationSelect = document.getElementById('zoxz-location');

    
    document.querySelectorAll('.zoxz-category').forEach(cat => {
        cat.addEventListener('click', () => {
            const next = cat.nextElementSibling;
            if (next && (next.classList.contains('zoxz-dorks') || next.classList.contains('zoxz-troll-dorks'))) {
                next.classList.toggle('show');
            }
        });
    });

    
    document.querySelectorAll('.zoxz-dork-item').forEach(item => {
        item.addEventListener('click', () => {
            textarea.value = item.textContent.trim();
            textarea.focus();
        });
    });

    
    trollCheckbox.addEventListener('change', () => {
        const isTroll = trollCheckbox.checked;
        document.querySelectorAll('.video-cat, .video-dorks').forEach(el => {
            el.style.display = isTroll ? 'none' : 'block';
        });
        document.querySelectorAll('.troll-cat, .troll-dorks').forEach(el => {
            el.style.display = isTroll ? 'block' : 'none';
        });
    });

    const engines = {
        google: { url: 'https://www.google.com/search?q=' },
        bing:   { url: 'https://www.bing.com/search?q=' },
        duck:   { url: 'https://duckduckgo.com/?q=' },
        yandex: { url: 'https://yandex.com/search/?text=' },
        brave:  { url: 'https://search.brave.com/search?q=' },
        searx:  { url: 'https://searx.be/search?q=' }
    };

    
    const locationMods = {
        all: '',
        restaurants: ' +restaurant +cafe +diner',
        rooms: ' +bedroom +livingroom +home +office'
    };

    function snipeDork(dork) {
        if (!dork.trim()) {
            status.textContent = 'need a dork';
            return;
        }

      
        const loc = locationSelect.value;
        if (loc !== 'all') {
            dork += locationMods[loc];
        }

        const all = document.getElementById('zoxz-allengines').checked;
        status.textContent = all
            ? `sniping "${dork}" → direct feeds across engines...`
            : `sniping google direct...`;

        const encoded = encodeURIComponent(dork);

        if (all) {
            Object.values(engines).forEach(e => {
                window.open(e.url + encoded, '_blank');
            });
        } else {
            window.open(engines.google.url + encoded, '_blank');
        }

        setTimeout(() => { status.textContent = ''; }, 3000);
    }

    const btns = document.getElementById('zoxz-buttons');

    const btnSnipe = document.createElement('button');
    btnSnipe.className = 'zoxz-btn';
    btnSnipe.textContent = 'snipe/troll feeds →';
    btnSnipe.onclick = () => snipeDork(textarea.value);

    const btnCopy = document.createElement('button');
    btnCopy.className = 'zoxz-btn zoxz-btn-copy';
    btnCopy.textContent = 'copy';
    btnCopy.onclick = () => {
        navigator.clipboard.writeText(textarea.value).then(() => {
            status.textContent = 'copied';
            setTimeout(() => status.textContent = '', 1500);
        });
    };

    btns.appendChild(btnSnipe);
    btns.appendChild(btnCopy);

    document.addEventListener('keydown', e => {
        if (e.ctrlKey && e.altKey && e.key.toLowerCase() === 's') {
            textarea.focus();
            e.preventDefault();
        }
    });

    let drag = false, x, y, ix, iy;
    panel.addEventListener('mousedown', e => {
        if (e.target.tagName === 'TEXTAREA' || e.target.tagName === 'BUTTON' || e.target.tagName === 'SELECT' || e.target.tagName === 'INPUT') return;
        ix = e.clientX - x;
        iy = e.clientY - y;
        drag = true;
    });
    document.addEventListener('mousemove', e => {
        if (!drag) return;
        e.preventDefault();
        x = e.clientX - ix;
        y = e.clientY - iy;
        panel.style.left = x + 'px';
        panel.style.top = y + 'px';
        panel.style.right = 'auto';
    });
    document.addEventListener('mouseup', () => drag = false);

    x = window.innerWidth - 480;
    y = 40;
    panel.style.left = x + 'px';
    panel.style.top = y + 'px';

})();
