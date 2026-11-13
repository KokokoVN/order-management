// Authentication & API helper for frontend
const Auth = {
    TOKEN_KEY: 'auth_token',
    USER_KEY: 'auth_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const u = localStorage.getItem(this.USER_KEY);
        try {
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    setSession(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    clearSession() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    isAdmin() {
        const user = this.getUser();
        if (!user || !user.role) return false;
        const r = String(user.role).toUpperCase();
        return r === 'ROLE_ADMIN' || r === 'ADMIN';
    },

    logout() {
        this.clearSession();
        window.location.href = '/login.html';
    },

    // Guard functions for pages
    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    },

    requireAdmin() {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        if (!this.isAdmin()) {
            showToast('Bạn không có quyền truy cập trang Quản Trị (ADMIN)!', 'warning');
            setTimeout(() => {
                window.location.href = '/user.html';
            }, 800);
            return false;
        }
        return true;
    },

    async fetch(url, options = {}) {
        options.headers = options.headers || {};
        const token = this.getToken();
        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }
        if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
        }

        try {
            const res = await fetch(url, options);
            if (res.status === 401) {
                this.clearSession();
                window.location.href = '/login.html?expired=1';
                throw new Error('Phiên đăng nhập đã hết hạn');
            }
            return res;
        } catch (err) {
            throw err;
        }
    }
};

// Clipboard copy helper
function copyToClipboard(text, message = 'Đã sao chép vào bộ nhớ tạm!') {
    if (!text) return;
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showToast(message, 'success');
        }).catch(() => {
            fallbackCopy(text, message);
        });
    } else {
        fallbackCopy(text, message);
    }
}

function fallbackCopy(text, message) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "fixed";
    textArea.style.left = "-9999px";
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    try {
        document.execCommand('copy');
        showToast(message, 'success');
    } catch (err) {
        showToast('Không thể sao chép tự động', 'error');
    }
    document.body.removeChild(textArea);
}

// Consistent Date Formatter
function formatDateTime(dateStr) {
    if (!dateStr) return '--';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return String(dateStr);
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes} ${day}/${month}/${year}`;
    } catch (e) {
        return String(dateStr);
    }
}

// Modern Toast Notification helper
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'fixed bottom-5 inset-x-4 sm:inset-x-auto sm:right-5 sm:max-w-sm z-[100] flex flex-col gap-2.5 pointer-events-none';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    const configs = {
        success: {
            bg: 'bg-emerald-950/90 border-emerald-500/40 text-emerald-200',
            icon: 'fa-solid fa-circle-check text-emerald-400'
        },
        error: {
            bg: 'bg-rose-950/90 border-rose-500/40 text-rose-200',
            icon: 'fa-solid fa-triangle-exclamation text-rose-400'
        },
        info: {
            bg: 'bg-sky-950/90 border-sky-500/40 text-sky-200',
            icon: 'fa-solid fa-circle-info text-sky-400'
        },
        warning: {
            bg: 'bg-amber-950/90 border-amber-500/40 text-amber-200',
            icon: 'fa-solid fa-circle-exclamation text-amber-400'
        }
    };

    const cfg = configs[type] || configs.info;

    toast.className = `${cfg.bg} border backdrop-blur-md px-4 py-3 rounded-2xl shadow-2xl flex items-center gap-3 transition-all transform duration-300 translate-y-4 opacity-0 pointer-events-auto text-xs font-medium`;
    toast.innerHTML = `
        <i class="${cfg.icon} text-base flex-shrink-0"></i>
        <span class="flex-1 leading-snug break-words">${message}</span>
        <button class="text-slate-400 hover:text-white focus:outline-none p-1 transition" onclick="this.parentElement.remove()">
            <i class="fa-solid fa-xmark text-xs"></i>
        </button>
    `;

    container.appendChild(toast);
    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-4', 'opacity-0');
        toast.classList.add('translate-y-0', 'opacity-100');
    });

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Sleek Custom Confirm Modal Component (Replaces browser confirm())
const ConfirmModal = {
    modalEl: null,
    resolvePromise: null,

    init() {
        if (this.modalEl) return;
        const div = document.createElement('div');
        div.id = 'custom-confirm-modal';
        div.className = 'fixed inset-0 z-[110] bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4 transition-all duration-200';
        div.innerHTML = `
            <div class="bg-slate-800 border border-slate-700/80 rounded-2xl shadow-2xl max-w-sm w-full p-6 space-y-4 transform transition-all scale-95 opacity-0 duration-200" id="custom-confirm-card">
                <div class="w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/30 flex items-center justify-center text-xl mx-auto shadow-inner" id="confirmIconBox">
                    <i class="fa-solid fa-triangle-exclamation" id="confirmIcon"></i>
                </div>
                <div class="text-center space-y-1.5">
                    <h3 class="text-base font-bold text-white" id="confirmTitle">Xác nhận</h3>
                    <p class="text-xs text-slate-300 leading-relaxed" id="confirmMessage">Bạn có chắc chắn muốn thực hiện?</p>
                </div>
                <div class="grid grid-cols-2 gap-3 pt-2">
                    <button type="button" id="confirmCancelBtn" class="px-4 py-2.5 rounded-xl bg-slate-700 hover:bg-slate-600 text-slate-200 font-semibold text-xs transition">
                        Hủy
                    </button>
                    <button type="button" id="confirmOkBtn" class="px-4 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-semibold text-xs shadow-lg shadow-rose-600/30 transition">
                        Đồng ý
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(div);
        this.modalEl = div;

        document.getElementById('confirmCancelBtn').addEventListener('click', () => this.close(false));
        document.getElementById('confirmOkBtn').addEventListener('click', () => this.close(true));
    },

    show({ title = 'Xác nhận', message = '', confirmText = 'Đồng ý', cancelText = 'Hủy', isDanger = true }) {
        this.init();
        document.getElementById('confirmTitle').textContent = title;
        document.getElementById('confirmMessage').textContent = message;
        document.getElementById('confirmOkBtn').textContent = confirmText;
        document.getElementById('confirmCancelBtn').textContent = cancelText;

        const btnOk = document.getElementById('confirmOkBtn');
        const iconBox = document.getElementById('confirmIconBox');
        const icon = document.getElementById('confirmIcon');

        if (isDanger) {
            btnOk.className = 'px-4 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-semibold text-xs shadow-lg shadow-rose-600/30 transition';
            iconBox.className = 'w-12 h-12 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/30 flex items-center justify-center text-xl mx-auto shadow-inner';
            icon.className = 'fa-solid fa-trash-can';
        } else {
            btnOk.className = 'px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs shadow-lg shadow-indigo-600/30 transition';
            iconBox.className = 'w-12 h-12 rounded-2xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/30 flex items-center justify-center text-xl mx-auto shadow-inner';
            icon.className = 'fa-solid fa-circle-question';
        }

        this.modalEl.classList.remove('hidden');
        this.modalEl.classList.add('flex');
        
        requestAnimationFrame(() => {
            const card = document.getElementById('custom-confirm-card');
            card.classList.remove('scale-95', 'opacity-0');
            card.classList.add('scale-100', 'opacity-100');
        });

        return new Promise((resolve) => {
            this.resolvePromise = resolve;
        });
    },

    close(result) {
        if (!this.modalEl) return;
        const card = document.getElementById('custom-confirm-card');
        card.classList.remove('scale-100', 'opacity-100');
        card.classList.add('scale-95', 'opacity-0');
        setTimeout(() => {
            this.modalEl.classList.add('hidden');
            this.modalEl.classList.remove('flex');
            if (this.resolvePromise) {
                this.resolvePromise(result);
                this.resolvePromise = null;
            }
        }, 150);
    }
};
