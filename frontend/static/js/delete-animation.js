/**
 * Lanka Threads - Animated Trash Can & Flying Letters Delete Engine
 * Provides dynamic interactive SVG trash can with animated opening lid,
 * progressive liquid/debris fill, micro-impact shake physics, flying letter arcs,
 * and Web Audio synthesized sound drops.
 */

(function () {
    'use strict';

    // Audio Context instance singleton
    let trashAudioCtx = null;

    /**
     * Synthesize crisp, satisfying mechanical/liquid drop audio via Web Audio API
     * @param {number} index - Index of the letter currently dropping
     */
    function playTrashDropSound(index) {
        try {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (!AudioCtx) return;

            if (!trashAudioCtx) {
                trashAudioCtx = new AudioCtx();
            }

            if (trashAudioCtx.state === 'suspended') {
                trashAudioCtx.resume();
            }

            const ctx = trashAudioCtx;
            const now = ctx.currentTime;

            // Pitch climbs gently with each letter filling up the bin
            const baseFreq = 340 + (index * 42);

            const osc = ctx.createOscillator();
            const gain = ctx.createGain();

            osc.type = 'sine';
            osc.frequency.setValueAtTime(baseFreq, now);
            osc.frequency.exponentialRampToValueAtTime(baseFreq * 0.35, now + 0.08);

            gain.gain.setValueAtTime(0.08, now);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.08);

            osc.connect(gain);
            gain.connect(ctx.destination);

            osc.start(now);
            osc.stop(now + 0.08);
        } catch (e) {
            // Audio context not allowed without prior user interaction or disabled
        }
    }

    /**
     * Async sleep helper
     * @param {number} ms 
     */
    const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

    /**
     * Generate standard SVG Trash Can markup
     * @param {string} clipId 
     * @returns {string}
     */
    function createTrashSvgMarkup(clipId) {
        return `
        <svg class="trash-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <!-- Trash Lid Group -->
            <g class="trash-lid">
                <path d="M9 4.5C9 4.22386 9.22386 4 9.5 4H14.5C14.7761 4 15 4.22386 15 4.5V5.5H9V4.5Z" fill="currentColor"/>
                <path d="M4 5.75C4 5.33579 4.33579 5 4.75 5H19.25C19.6642 5 20 5.33579 20 5.75C20 6.16421 19.6642 6.5 19.25 6.5H4.75C4.33579 6.5 4 6.16421 4 5.75Z" fill="currentColor"/>
            </g>
            <defs>
                <clipPath id="${clipId}">
                    <path d="M5.8 7.5L7 20.2C7.08 21.1 7.84 21.8 8.74 21.8H15.26C16.16 21.8 16.92 21.1 17 20.2L18.2 7.5H5.8Z"/>
                </clipPath>
            </defs>
            <!-- Rising Liquid Fill Container -->
            <g clip-path="url(#${clipId})">
                <rect class="trash-liquid" x="4" y="7" width="16" height="16" />
            </g>
            <!-- Trash Can Body Lines -->
            <path class="trash-body" d="M5.8 7.5L7 20.2C7.08 21.1 7.84 21.8 8.74 21.8H15.26C16.16 21.8 16.92 21.1 17 20.2L18.2 7.5H5.8Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            <path class="trash-line" d="M9.5 10.5V18" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.6"/>
            <path class="trash-line" d="M12 10.5V18" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.6"/>
            <path class="trash-line" d="M14.5 10.5V18" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" opacity="0.6"/>
        </svg>`;
    }

    /**
     * Execute the letter flying + liquid rising + impact shake animation
     * @param {HTMLElement} btn 
     * @param {Function} [onComplete] 
     */
    async function animateDeleteButton(btn, onComplete) {
        if (btn.classList.contains('deleting') || btn.dataset.isAnimating === 'true') {
            return;
        }

        btn.dataset.isAnimating = 'true';
        btn.classList.add('deleting');

        const trashWrapper = btn.querySelector('.trash-wrapper');
        const liquidGroup = btn.querySelector('.trash-liquid');
        const letters = btn.querySelectorAll('.letter');

        if (trashWrapper) {
            trashWrapper.classList.add('open');
        }

        const totalLetters = letters.length > 0 ? letters.length : 6;
        
        // Compute discrete liquid levels so it fills up to 0px (top of can)
        const liquidLevels = [];
        for (let i = 0; i < totalLetters; i++) {
            const progress = (i + 1) / totalLetters;
            liquidLevels.push(Math.round(16 * (1 - progress)));
        }

        // Secuencia de vertido por letra
        for (let i = 0; i < letters.length; i++) {
            letters[i].classList.add('flying');

            setTimeout(() => {
                if (liquidGroup) {
                    liquidGroup.style.transform = `translateY(${liquidLevels[i]}px)`; // Eleva líquido
                }
                playTrashDropSound(i); // Dispara sonido sincronizado
                if (trashWrapper) {
                    trashWrapper.classList.remove('impact-shake');
                    void trashWrapper.offsetWidth; // Force reflow
                    trashWrapper.classList.add('impact-shake'); // Micro-impacto
                }
            }, 200);

            await wait(120); // Intervalo entre letras
        }

        // Wait for last letter to settle
        await wait(360);

        if (trashWrapper) {
            trashWrapper.classList.remove('open');
        }

        await wait(120);

        btn.dataset.isAnimating = 'false';

        if (typeof onComplete === 'function') {
            onComplete();
        }
    }

    /**
     * Prepare a button element with full SVG trash can and letter spans
     * @param {HTMLElement} btn 
     */
    function setupDeleteButtonStructure(btn) {
        if (btn.dataset.deleteBtnInitialized === 'true') return;
        btn.dataset.deleteBtnInitialized = 'true';

        let trashWrapper = btn.querySelector('.trash-wrapper');
        let textWrapper = btn.querySelector('.text-wrapper');

        const uniqueId = 'trashClip_' + Math.random().toString(36).substring(2, 9);

        // If trash-wrapper doesn't exist, create it
        if (!trashWrapper) {
            trashWrapper = document.createElement('div');
            trashWrapper.className = 'trash-wrapper';
            trashWrapper.innerHTML = createTrashSvgMarkup(uniqueId);
        } else if (!trashWrapper.querySelector('.trash-svg')) {
            trashWrapper.innerHTML = createTrashSvgMarkup(uniqueId);
        }

        // If text-wrapper already exists with .letter elements, ensure styles are valid
        if (textWrapper) {
            const existingLetters = textWrapper.querySelectorAll('.letter');
            existingLetters.forEach((span, idx) => {
                if (!span.style.getPropertyValue('--fly-dist')) {
                    const dist = 38 + (idx * 12);
                    span.style.setProperty('--fly-dist', `${dist}px`);
                }
            });
        } else {
            // Extract raw text or default to "Delete"
            let rawText = btn.getAttribute('data-text') || btn.textContent.trim() || 'Delete';
            rawText = rawText.replace(/\s+/g, ' ');

            textWrapper = document.createElement('div');
            textWrapper.className = 'text-wrapper';

            let charIdx = 0;
            for (let i = 0; i < rawText.length; i++) {
                const char = rawText[i];
                const span = document.createElement('span');
                span.className = 'letter';
                
                if (char === ' ') {
                    span.innerHTML = '&nbsp;';
                    span.style.setProperty('--fly-dist', `${38 + (charIdx * 12)}px`);
                } else {
                    span.textContent = char;
                    span.style.setProperty('--fly-dist', `${38 + (charIdx * 12)}px`);
                }
                textWrapper.appendChild(span);
                charIdx++;
            }

            // Clear original content and assemble
            btn.innerHTML = '';
            btn.appendChild(trashWrapper);
            btn.appendChild(textWrapper);
        }

        // Ensure trash wrapper is first child
        if (btn.firstChild !== trashWrapper) {
            btn.insertBefore(trashWrapper, btn.firstChild);
        }

        // Event listener
        btn.addEventListener('click', function (e) {
            if (btn.dataset.isAnimating === 'true' || btn.classList.contains('deleting')) {
                e.preventDefault();
                e.stopPropagation();
                return;
            }

            const confirmMsg = btn.getAttribute('data-confirm') || btn.getAttribute('data-delete-confirm');
            if (confirmMsg) {
                const ok = window.confirm(confirmMsg);
                if (!ok) {
                    e.preventDefault();
                    e.stopPropagation();
                    return;
                }
            }

            // Intercept standard click to play animation first
            e.preventDefault();
            e.stopPropagation();

            animateDeleteButton(btn, () => {
                // If it's inside a form or a submit button
                const form = btn.closest('form');
                if (btn.type === 'submit' && form) {
                    // Check if form is valid and submit
                    form.submit();
                    return;
                }

                // If it's an anchor link
                if (btn.tagName.toLowerCase() === 'a' && btn.href) {
                    window.location.href = btn.href;
                    return;
                }

                // If there's an action attribute or onclick logic
                const customAction = btn.getAttribute('data-action');
                if (customAction && typeof window[customAction] === 'function') {
                    window[customAction](btn);
                }
            });
        });
    }

    /**
     * Find and initialize all delete buttons on the page
     */
    function initAllDeleteButtons() {
        const buttons = document.querySelectorAll('.delete-btn, [data-delete-btn]');
        buttons.forEach((btn) => {
            setupDeleteButtonStructure(btn);
        });
    }

    // Expose globals for manual script invocations
    window.initDeleteButtons = initAllDeleteButtons;
    window.animateDeleteButton = animateDeleteButton;
    window.playTrashDropSound = playTrashDropSound;

    // Auto-run on DOM ready and HTMX swaps
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAllDeleteButtons);
    } else {
        initAllDeleteButtons();
    }

    document.addEventListener('htmx:afterSwap', function () {
        initAllDeleteButtons();
    });
})();
