/**
 * Lanka Threads - Global Client-Side Validation Engine
 * Validates Email Addresses and Mobile Phone Numbers across the entire web application
 */
(function () {
    'use strict';

    // RFC 5322 standard compliant email regex pattern
    const EMAIL_REGEX = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,10}$/;

    // Sri Lankan & General International Mobile Phone Number Pattern:
    // Supports: +94 77 123 4567, 077 123 4567, 011 234 5678, +94112345678, or 8-15 digit phone formats
    const PHONE_CLEANED_REGEX = /^(?:(?:\+94|0094|0)?(?:7[0-9]|1[1-9]|2[1-8]|3[1-8]|4[1-7]|5[1-7]|6[3-7]|8[1-9]|9[1-2])[0-9]{7}|\+?[1-9][0-9]{7,14})$/;

    function cleanPhoneNumber(phone) {
        if (!phone) return '';
        return phone.replace(/[\s()\-\.]/g, '');
    }

    function isValidEmail(email) {
        if (!email) return false;
        return EMAIL_REGEX.test(email.trim());
    }

    function isValidPhone(phone) {
        if (!phone) return false;
        const cleaned = cleanPhoneNumber(phone.trim());
        return PHONE_CLEANED_REGEX.test(cleaned);
    }

    function isValidContactInfo(contactInfo) {
        if (!contactInfo) return false;
        const parts = contactInfo.split(/[/,;|]+/);
        let hasValidPart = false;
        for (let i = 0; i < parts.length; i++) {
            const p = parts[i].trim();
            if (isValidEmail(p) || isValidPhone(p)) {
                hasValidPart = true;
                break;
            }
        }
        return hasValidPart || isValidEmail(contactInfo.trim()) || isValidPhone(contactInfo.trim());
    }

    function getOrCreateErrorElement(input) {
        // Look for existing error container
        let errorEl = input.parentElement.querySelector('.global-field-error') ||
                      document.getElementById(input.name + 'Error') ||
                      document.getElementById(input.id + 'Error');

        if (!errorEl) {
            errorEl = document.createElement('div');
            errorEl.className = 'global-field-error text-[11px] text-rose-500 dark:text-rose-400 mt-1.5 flex items-center gap-1 font-medium transition-all duration-200 hidden';
            if (input.nextSibling) {
                input.parentNode.insertBefore(errorEl, input.nextSibling);
            } else {
                input.parentNode.appendChild(errorEl);
            }
        }
        return errorEl;
    }

    function setFieldState(input, isValid, message) {
        const errorEl = getOrCreateErrorElement(input);
        
        if (isValid) {
            input.classList.remove('border-rose-500', 'focus:border-rose-500', 'ring-rose-500/20');
            input.classList.add('border-emerald-500/80');
            if (errorEl) {
                errorEl.classList.add('hidden');
                errorEl.innerHTML = '';
            }
            input.setCustomValidity('');
        } else {
            input.classList.remove('border-emerald-500/80');
            input.classList.add('border-rose-500', 'focus:border-rose-500');
            if (errorEl) {
                errorEl.innerHTML = `<i class="bi bi-exclamation-circle-fill text-xs text-rose-500 shrink-0"></i> <span>${message}</span>`;
                errorEl.classList.remove('hidden');
            }
            input.setCustomValidity(message);
        }
    }

    function clearFieldState(input) {
        input.classList.remove('border-rose-500', 'focus:border-rose-500', 'border-emerald-500/80');
        const errorEl = input.parentElement.querySelector('.global-field-error');
        if (errorEl) {
            errorEl.classList.add('hidden');
            errorEl.innerHTML = '';
        }
        input.setCustomValidity('');
    }

    function validateInputField(input, isSubmitting = false) {
        const val = input.value.trim();
        const isRequired = input.hasAttribute('required') || input.required;
        const name = (input.getAttribute('name') || '').toLowerCase();
        const id = (input.getAttribute('id') || '').toLowerCase();
        const type = (input.getAttribute('type') || '').toLowerCase();

        // If empty and not required, clear unless submitting
        if (!val) {
            if (isRequired && isSubmitting) {
                if (type === 'email' || name.includes('email') || id.includes('email')) {
                    setFieldState(input, false, 'Email address is required.');
                    return false;
                } else if (type === 'tel' || name.includes('phone') || name.includes('mobile') || id.includes('phone')) {
                    setFieldState(input, false, 'Mobile phone number is required.');
                    return false;
                } else if (name.includes('contact')) {
                    setFieldState(input, false, 'Contact information is required.');
                    return false;
                }
            }
            if (!isRequired && !val) {
                clearFieldState(input);
                return true;
            }
            return !isRequired;
        }

        // 1. Email Field Validation
        if (type === 'email' || name.includes('email') || id.includes('email')) {
            const valid = isValidEmail(val);
            setFieldState(input, valid, valid ? '' : 'Please enter a valid email address (e.g. name@example.com)');
            return valid;
        }

        // 2. Mobile / Phone Field Validation
        if (type === 'tel' || name.includes('phone') || name.includes('mobile') || id.includes('phone') || id.includes('mobile')) {
            const valid = isValidPhone(val);
            setFieldState(input, valid, valid ? '' : 'Please enter a valid mobile number (e.g. +94 77 123 4567 or 077 123 4567)');
            return valid;
        }

        // 3. Supplier Contact Info (Phone / Email combination)
        if (name.includes('contactinfo') || name.includes('contact')) {
            const valid = isValidContactInfo(val);
            setFieldState(input, valid, valid ? '' : 'Please enter a valid phone number or email (e.g. 0112345678 or vendor@example.com)');
            return valid;
        }

        return true;
    }

    function attachValidationToInput(input) {
        if (input.dataset.validationBound) return;
        input.dataset.validationBound = 'true';

        let touched = false;

        input.addEventListener('blur', function () {
            touched = true;
            validateInputField(input, false);
        });

        input.addEventListener('input', function () {
            if (touched || input.value.length > 5) {
                validateInputField(input, false);
            }
        });
    }

    function initFormValidation() {
        // Find all relevant inputs across the DOM
        const selector = 'input[type="email"], input[type="tel"], input[name*="email" i], input[name*="phone" i], input[name*="mobile" i], input[name*="contact" i], input[id*="email" i], input[id*="phone" i]';
        const inputs = document.querySelectorAll(selector);

        inputs.forEach(attachValidationToInput);

        // Intercept form submissions to guarantee validation before network dispatch
        document.querySelectorAll('form').forEach(function (form) {
            if (form.dataset.formValidationBound) return;
            form.dataset.formValidationBound = 'true';

            form.addEventListener('submit', function (e) {
                const formInputs = form.querySelectorAll(selector);
                let firstInvalid = null;

                formInputs.forEach(function (inp) {
                    const isValid = validateInputField(inp, true);
                    if (!isValid && !firstInvalid) {
                        firstInvalid = inp;
                    }
                });

                if (firstInvalid) {
                    e.preventDefault();
                    e.stopImmediatePropagation();
                    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstInvalid.focus();
                }
            });
        });
    }

    // Initialize on DOM load and watch for dynamic modals/elements
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initFormValidation);
    } else {
        initFormValidation();
    }

    // Observe DOM mutations to auto-bind dynamic modals (Add Customer, Add Supplier, etc.)
    const observer = new MutationObserver(function () {
        initFormValidation();
    });

    if (document.body) {
        observer.observe(document.body, { childList: true, subtree: true });
    }

    // Expose helpers globally
    window.LankaValidation = {
        isValidEmail: isValidEmail,
        isValidPhone: isValidPhone,
        isValidContactInfo: isValidContactInfo,
        reinit: initFormValidation
    };

})();
