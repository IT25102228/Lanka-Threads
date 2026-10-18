/**
 * Lanka Threads - Kumo Interactive Motion & Smooth Scroll Animation Engine
 * Inspired by Kumo Interactive UI/UX - Features 3D cursor tilt tracking, floating badge levitation,
 * custom magnetic ambient cursor glow, smooth scroll reveals, and HTMX dynamic DOM re-binding.
 */

document.addEventListener('DOMContentLoaded', function () {
    initKumoMotionEngine();
});

function initKumoMotionEngine() {
    const existingBar = document.getElementById('scroll-progress-bar');
    if (existingBar) existingBar.remove();
    const existingCursor = document.getElementById('kumo-cursor-ring');
    if (existingCursor) existingCursor.remove();
    createScrollToTopButton();
    setupIntersectionObserver();
    setup3DTiltPhysics();
    setupSmoothAnchorScrolling();
    setupHTMXRebinding();
}

/**
 * 3D Interactive Card & Image Tilt Mechanics (Kumo Parallax Effect)
 */
function setup3DTiltPhysics() {
    // Attach 3D cursor tilt logic to cards, product images, and hero banners
    const tiltTargets = document.querySelectorAll('.card, .dashboard-card, .glass-card, .product-img-wrapper, .kumo-tilt');

    tiltTargets.forEach(function (card) {
        if (card.classList.contains('tilt-init-done')) return;
        card.classList.add('tilt-init-done');

        let bounds;

        function onMouseEnter() {
            bounds = card.getBoundingClientRect();
            card.style.transition = 'transform 0.15s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.35s ease';
        }

        function onMouseMove(e) {
            if (!bounds) bounds = card.getBoundingClientRect();
            const mouseX = e.clientX - bounds.left;
            const mouseY = e.clientY - bounds.top;

            const xPct = (mouseX / bounds.width - 0.5) * 2; // -1 to 1
            const yPct = (mouseY / bounds.height - 0.5) * 2; // -1 to 1

            const tiltX = -yPct * 12; // Max 12deg tilt
            const tiltY = xPct * 12;

            card.style.transform = `perspective(1000px) rotateX(${tiltX}deg) rotateY(${tiltY}deg) translateZ(15px) scale(1.02)`;
        }

        function onMouseLeave() {
            card.style.transition = 'transform 0.5s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.35s ease';
            card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateZ(0px) scale(1)';
        }

        card.addEventListener('mouseenter', onMouseEnter);
        card.addEventListener('mousemove', onMouseMove);
        card.addEventListener('mouseleave', onMouseLeave);
    });
}

/**
 * Sets up IntersectionObserver for scroll-triggered reveals
 */
let globalScrollObserver = null;

function setupIntersectionObserver() {
    const observerOptions = {
        root: null,
        rootMargin: '0px 0px -50px 0px',
        threshold: 0.08
    };

    if ('IntersectionObserver' in window) {
        globalScrollObserver = new IntersectionObserver(function (entries, observer) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        observeElements();
    } else {
        const allTargeted = document.querySelectorAll('.reveal-on-scroll, .reveal-slide-left, .reveal-slide-right, .reveal-zoom, .reveal-hyper');
        allTargeted.forEach(function (el) {
            el.classList.add('is-visible');
        });
    }
}

/**
 * Finds elements and applies staggered entrance delays
 */
function observeElements() {
    if (!globalScrollObserver) return;

    const selectors = [
        '.reveal-on-scroll',
        '.reveal-slide-left',
        '.reveal-slide-right',
        '.reveal-zoom',
        '.reveal-hyper',
        'main > div',
        'main section',
        '.card',
        '.dashboard-card',
        '.glass-card',
        '.table-responsive',
        'form',
        '.grid > div',
        '.grid > a'
    ];

    const elements = document.querySelectorAll(selectors.join(', '));

    elements.forEach(function (el) {
        if (el.closest('nav') || el.closest('.profile-dropdown-menu') || el.classList.contains('reveal-init-done')) {
            return;
        }

        if (!el.classList.contains('reveal-on-scroll') &&
            !el.classList.contains('reveal-slide-left') &&
            !el.classList.contains('reveal-slide-right') &&
            !el.classList.contains('reveal-zoom') &&
            !el.classList.contains('reveal-hyper')) {
            el.classList.add('reveal-hyper');
        }

        const parent = el.parentElement;
        if (parent && (parent.classList.contains('grid') || parent.classList.contains('row') || parent.classList.contains('flex-wrap'))) {
            const siblings = Array.from(parent.children);
            const index = siblings.indexOf(el);
            if (index >= 0) {
                const delay = Math.min((index % 6) * 0.08, 0.4);
                el.style.transitionDelay = `${delay}s`;
            }
        }

        el.classList.add('reveal-init-done');
        globalScrollObserver.observe(el);
    });
}

/**
 * Enables smooth scrolling for anchor links
 */
function setupSmoothAnchorScrolling() {
    document.addEventListener('click', function (e) {
        const anchor = e.target.closest('a[href^="#"]');
        if (!anchor) return;

        const href = anchor.getAttribute('href');
        if (href === '#' || href === '') return;

        const targetEl = document.querySelector(href);
        if (targetEl) {
            e.preventDefault();
            const navHeight = 75;
            const targetPosition = targetEl.getBoundingClientRect().top + window.pageYOffset - navHeight;

            window.scrollTo({
                top: targetPosition,
                behavior: 'smooth'
            });
        }
    });
}

/**
 * Re-binds observer & 3D tilt when HTMX or AJAX updates the DOM
 */
function setupHTMXRebinding() {
    document.addEventListener('htmx:afterSettle', function () {
        observeElements();
        setup3DTiltPhysics();
    });
    document.addEventListener('htmx:afterSwap', function () {
        observeElements();
        setup3DTiltPhysics();
    });
}

/**
 * Creates and handles the floating scroll-to-top button
 */
function createScrollToTopButton() {
    let btn = document.getElementById('scroll-to-top-btn');
    if (!btn) {
        btn = document.createElement('button');
        btn.id = 'scroll-to-top-btn';
        btn.className = 'scroll-to-top-btn';
        btn.setAttribute('aria-label', 'Scroll to top');
        btn.innerHTML = '<i class="bi bi-arrow-up"></i>';
        document.body.appendChild(btn);

        btn.addEventListener('click', function () {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    // Scroll listener to toggle visibility
    window.addEventListener('scroll', function () {
        if (window.scrollY > 300) {
            btn.classList.add('is-visible');
        } else {
            btn.classList.remove('is-visible');
        }
    });
}
