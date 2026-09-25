/**
 * ResolveDesk - Complaint Management Portal
 * Frontend State & Interaction Controller
 */

// ==========================================
// 1. INITIAL SYSTEM STATE & SEED DATA
// ==========================================

const SystemState = {
    currentUser: null, // { id, name, email, role, department?, isSenior? }
    simulatedDate: new Date(),
    nextComplaintSeq: 5,
    nextStudentSeq: 104,
    studentFilter: 'ALL', // 'ALL' | 'PENDING' | 'IN_PROGRESS' | 'RESOLVED' | 'ESCALATED'
    
    // Seeded Students
    students: [
        { id: "USR-101", name: "Alice Smith", email: "alice@example.com", phone: "+1-555-0101", password: "demo123" },
        { id: "USR-102", name: "David Miller", email: "david@example.com", phone: "+1-555-0102", password: "demo123" },
        { id: "USR-103", name: "Emma Watson", email: "emma@example.com", phone: "+1-555-0103", password: "demo123" }
    ],

    // Seeded Administrators
    admins: [
        { id: "ADM-201", name: "Bob Johnson", email: "bob@support.com", department: "Customer Support", isSenior: false, password: "adminPass2026" },
        { id: "ADM-S01", name: "Diana Prince", email: "diana@support.com", department: "Executive Escalations", isSenior: true, password: "adminPass2026" }
    ],

    // SLA Thresholds
    thresholds: {
        HIGH: 2,
        MEDIUM: 5,
        LOW: 10
    },

    // Seeded Complaints
    complaints: [
        {
            complaintId: "CMP-0001",
            type: "Billing",
            description: "Double charge on semester subscription fee",
            priority: "HIGH",
            daysAgo: 4,
            dateFiled: formatDate(daysAgo(4)),
            status: "OPEN",
            filedBy: { id: "USR-101", name: "Alice Smith" },
            assignedAdmin: null,
            statusHistory: [
                { status: "OPEN", timestamp: formatDateTime(daysAgo(4)), note: "Complaint filed by Alice Smith" }
            ]
        },
        {
            complaintId: "CMP-0002",
            type: "Facilities",
            description: "Request dark mode option in library computer stations",
            priority: "LOW",
            daysAgo: 4,
            dateFiled: formatDate(daysAgo(4)),
            status: "OPEN",
            filedBy: { id: "USR-102", name: "David Miller" },
            assignedAdmin: null,
            statusHistory: [
                { status: "OPEN", timestamp: formatDateTime(daysAgo(4)), note: "Complaint filed by David Miller" }
            ]
        },
        {
            complaintId: "CMP-0003",
            type: "Technical",
            description: "Password reset link from campus portal not arriving",
            priority: "MEDIUM",
            daysAgo: 1,
            dateFiled: formatDate(daysAgo(1)),
            status: "OPEN",
            filedBy: { id: "USR-103", name: "Emma Watson" },
            assignedAdmin: null,
            statusHistory: [
                { status: "OPEN", timestamp: formatDateTime(daysAgo(1)), note: "Complaint filed by Emma Watson" }
            ]
        },
        {
            complaintId: "CMP-0004",
            type: "Academic",
            description: "Exam portal timed out before final submission",
            priority: "HIGH",
            daysAgo: 3,
            dateFiled: formatDate(daysAgo(3)),
            status: "IN_PROGRESS",
            filedBy: { id: "USR-101", name: "Alice Smith" },
            assignedAdmin: { id: "ADM-201", name: "Bob Johnson" },
            statusHistory: [
                { status: "OPEN", timestamp: formatDateTime(daysAgo(3)), note: "Complaint filed by Alice Smith" },
                { status: "IN_PROGRESS", timestamp: formatDateTime(daysAgo(2)), note: "Reviewing examination server access logs [By: Bob Johnson]" }
            ]
        }
    ]
};

// Helpers for dates
function daysAgo(n) {
    const d = new Date();
    d.setDate(d.getDate() - n);
    return d;
}

function formatDate(date) {
    const d = new Date(date);
    return d.toISOString().split('T')[0];
}

function formatDateTime(date) {
    const d = new Date(date);
    return d.toISOString().replace('T', ' ').substring(0, 19);
}

// ==========================================
// 2. TAB SWITCHING & AUTH UI
// ==========================================

function switchRole(role) {
    clearFeedback();
    const studentTab = document.getElementById('studentRoleTab');
    const adminTab = document.getElementById('adminRoleTab');
    const studentSignIn = document.getElementById('studentSignInSection');
    const studentRegister = document.getElementById('studentRegisterSection');
    const adminLogin = document.getElementById('adminLoginSection');

    if (role === 'student') {
        studentTab.classList.add('active');
        adminTab.classList.remove('active');
        studentSignIn.style.display = 'block';
        studentRegister.style.display = 'none';
        adminLogin.style.display = 'none';
    } else {
        adminTab.classList.add('active');
        studentTab.classList.remove('active');
        studentSignIn.style.display = 'none';
        studentRegister.style.display = 'none';
        adminLogin.style.display = 'block';
    }
}

function showStudentForm(formType) {
    clearFeedback();
    const studentSignIn = document.getElementById('studentSignInSection');
    const studentRegister = document.getElementById('studentRegisterSection');

    if (formType === 'register') {
        studentSignIn.style.display = 'none';
        studentRegister.style.display = 'block';
    } else {
        studentRegister.style.display = 'none';
        studentSignIn.style.display = 'block';
    }
}

function showFeedback(message, type = 'error') {
    const alertBox = document.getElementById('feedbackAlert');
    alertBox.className = `feedback-alert ${type}`;
    alertBox.textContent = message;
    alertBox.style.display = 'flex';
}

function clearFeedback() {
    const alertBox = document.getElementById('feedbackAlert');
    if (alertBox) {
        alertBox.style.display = 'none';
    }
}

// Quick Fill Helpers
function quickFillStudent(studentId) {
    switchRole('student');
    showStudentForm('signin');
    const student = SystemState.students.find(s => s.id === studentId);
    if (student) {
        document.getElementById('studentIdInput').value = student.id;
        document.getElementById('studentPassword').value = student.password;
        showFeedback(`Quick-filled credentials for ${student.name} (${student.id})`, 'success');
    }
}

function quickFillAdmin(adminId) {
    switchRole('admin');
    const admin = SystemState.admins.find(a => a.id === adminId);
    if (admin) {
        document.getElementById('adminIdInput').value = admin.id;
        document.getElementById('adminPassword').value = admin.password;
        const tier = admin.isSenior ? 'Senior Administrator' : 'Regular Administrator';
        showFeedback(`Quick-filled credentials for ${admin.name} (${tier})`, 'success');
    }
}

// ==========================================
// 3. AUTHENTICATION HANDLERS
// ==========================================

function handleStudentSignIn(e) {
    e.preventDefault();
    clearFeedback();
    const inputVal = document.getElementById('studentIdInput').value.trim();
    const password = document.getElementById('studentPassword').value;

    const student = SystemState.students.find(s => 
        s.id.toLowerCase() === inputVal.toLowerCase() || 
        s.email.toLowerCase() === inputVal.toLowerCase()
    );

    if (!student) {
        showFeedback(`Student account '${inputVal}' not found. Please register or use demo accounts.`, 'error');
        return;
    }

    if (password !== student.password) {
        showFeedback('Incorrect password. Try using demo credentials.', 'error');
        return;
    }

    // Success
    SystemState.currentUser = {
        id: student.id,
        name: student.name,
        email: student.email,
        phone: student.phone,
        role: 'STUDENT'
    };

    renderDashboard();
}

function handleStudentRegister(e) {
    e.preventDefault();
    clearFeedback();
    const name = document.getElementById('regFullName').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const phone = document.getElementById('regPhone').value.trim();
    const password = document.getElementById('regPassword').value;

    // Check duplicate
    if (SystemState.students.some(s => s.email.toLowerCase() === email.toLowerCase())) {
        showFeedback('A student with this email address is already registered.', 'error');
        return;
    }

    const newId = `USR-${SystemState.nextStudentSeq++}`;
    const newStudent = { id: newId, name, email, phone, password };
    SystemState.students.push(newStudent);

    // Auto login
    SystemState.currentUser = {
        id: newStudent.id,
        name: newStudent.name,
        email: newStudent.email,
        phone: newStudent.phone,
        role: 'STUDENT',
        welcomeMsg: `Account successfully created! Your official Student ID is ${newId}.`
    };

    renderDashboard();
}

function handleAdminSignIn(e) {
    e.preventDefault();
    clearFeedback();
    const inputVal = document.getElementById('adminIdInput').value.trim();
    const password = document.getElementById('adminPassword').value;

    const admin = SystemState.admins.find(a => 
        a.id.toLowerCase() === inputVal.toLowerCase() || 
        a.email.toLowerCase() === inputVal.toLowerCase()
    );

    if (!admin) {
        showFeedback(`Administrator '${inputVal}' is not authorized. Valid IDs: ADM-201, ADM-S01.`, 'error');
        return;
    }

    if (password !== admin.password) {
        showFeedback('Administrative authentication failed. Check credentials.', 'error');
        return;
    }

    // Success
    SystemState.currentUser = {
        id: admin.id,
        name: admin.name,
        email: admin.email,
        department: admin.department,
        isSenior: admin.isSenior,
        role: admin.isSenior ? 'SENIOR_ADMIN' : 'ADMIN'
    };

    renderDashboard();
}

function handleLogout() {
    SystemState.currentUser = null;
    SystemState.studentFilter = 'ALL';
    document.getElementById('authSection').style.display = 'flex';
    document.getElementById('portalView').style.display = 'none';
    switchRole('student');
    showStudentForm('signin');
    showFeedback('You have been logged out successfully. Sign in again below.', 'success');
}

// ==========================================
// 4. PORTAL DASHBOARDS (POST-LOGIN)
// ==========================================

function renderDashboard() {
    document.getElementById('authSection').style.display = 'none';
    const portal = document.getElementById('portalView');
    portal.style.display = 'block';

    const user = SystemState.currentUser;
    const isStudent = user.role === 'STUDENT';
    const roleBadgeClass = isStudent ? '' : (user.isSenior ? 'senior' : 'admin');
    const roleLabel = isStudent ? 'Student' : (user.isSenior ? 'Senior Admin' : 'Staff Admin');

    portal.innerHTML = `
        <div class="portal-topbar">
            <div class="user-identity">
                <div class="avatar ${!isStudent ? 'admin-avatar' : ''}">
                    ${user.name.charAt(0)}
                </div>
                <div class="identity-details">
                    <h3>${user.name} <span class="role-tag ${roleBadgeClass}">${roleLabel}</span></h3>
                    <p>ID: <strong>${user.id}</strong> &bull; ${user.email} ${user.department ? `&bull; Dept: ${user.department}` : ''}</p>
                </div>
            </div>
            <div style="display:flex; align-items:center; gap: 12px;">
                <button type="button" class="chip-btn" onclick="triggerEscalationScan()" title="Evaluate unresolved SLA breaches">
                    ⚡ Run SLA Escalation Scan
                </button>
                <button type="button" class="btn-logout" onclick="handleLogout()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    <span>Sign Out</span>
                </button>
            </div>
        </div>
        ${user.welcomeMsg ? `
            <div class="feedback-alert success" style="margin-bottom: 20px; display: flex; align-items: center; gap: 10px;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                <span>${user.welcomeMsg}</span>
            </div>
        ` : ''}

        ${isStudent ? renderStudentDashboard() : renderAdminDashboard()}
    `;

    attachDashboardEvents();
}

// Helper to select quick complaint category
function setComplaintCategory(cat) {
    const input = document.getElementById('complaintType');
    if (input) {
        input.value = cat;
        input.focus();
    }
}

// Helper to filter student complaints by status
function setStudentFilter(filter) {
    SystemState.studentFilter = filter;
    renderDashboard();
}

// Student Dashboard View
function renderStudentDashboard() {
    const allStudentComplaints = SystemState.complaints.filter(c => c.filedBy.id === SystemState.currentUser.id);

    const pendingComplaints = allStudentComplaints.filter(c => c.status === 'OPEN');
    const inProgressComplaints = allStudentComplaints.filter(c => c.status === 'IN_PROGRESS');
    const resolvedComplaints = allStudentComplaints.filter(c => c.status === 'RESOLVED' || c.status === 'CLOSED');
    const escalatedComplaints = allStudentComplaints.filter(c => c.status === 'ESCALATED');

    const filter = SystemState.studentFilter || 'ALL';
    let filteredList = allStudentComplaints;
    if (filter === 'PENDING') filteredList = pendingComplaints;
    else if (filter === 'IN_PROGRESS') filteredList = inProgressComplaints;
    else if (filter === 'RESOLVED') filteredList = resolvedComplaints;
    else if (filter === 'ESCALATED') filteredList = escalatedComplaints;

    return `
        <!-- Top Metrics Cards: All, Pending, In Progress, Resolved, Escalated -->
        <div class="metrics-grid">
            <div class="metric-card ${filter === 'ALL' ? 'active' : ''}" onclick="setStudentFilter('ALL')" title="View all your complaints">
                <div class="metric-header">
                    <span>All Complaints</span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>
                </div>
                <div class="metric-num">${allStudentComplaints.length}</div>
            </div>

            <div class="metric-card metric-pending ${filter === 'PENDING' ? 'active' : ''}" onclick="setStudentFilter('PENDING')" title="Filter: Pending complaints">
                <div class="metric-header">
                    <span>Pending</span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 14 14"/></svg>
                </div>
                <div class="metric-num">${pendingComplaints.length}</div>
            </div>

            <div class="metric-card metric-progress ${filter === 'IN_PROGRESS' ? 'active' : ''}" onclick="setStudentFilter('IN_PROGRESS')" title="Filter: In Progress complaints">
                <div class="metric-header">
                    <span>In Progress</span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                </div>
                <div class="metric-num">${inProgressComplaints.length}</div>
            </div>

            <div class="metric-card metric-resolved ${filter === 'RESOLVED' ? 'active' : ''}" onclick="setStudentFilter('RESOLVED')" title="Filter: Resolved & Closed complaints">
                <div class="metric-header">
                    <span>Resolved</span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                </div>
                <div class="metric-num">${resolvedComplaints.length}</div>
            </div>

            <div class="metric-card metric-escalated ${filter === 'ESCALATED' ? 'active' : ''}" onclick="setStudentFilter('ESCALATED')" title="Filter: Escalated complaints">
                <div class="metric-header">
                    <span>Escalated</span>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></svg>
                </div>
                <div class="metric-num">${escalatedComplaints.length}</div>
            </div>
        </div>

        <div class="dashboard-grid">
            <!-- Left: Commit a New Complaint Form -->
            <div class="panel-card">
                <h3>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    Commit New Complaint
                </h3>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 16px;">
                    Submit a grievance with automatic SLA auto-escalation protection.
                </p>

                <form id="fileComplaintForm" onsubmit="submitStudentComplaint(event)">
                    <div class="form-group">
                        <label for="complaintType">Category / Subject</label>
                        <div class="input-wrapper">
                            <input type="text" id="complaintType" placeholder="e.g. Billing, Academic, Facilities" required>
                        </div>
                        <div class="cat-pills">
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Billing')">Billing</button>
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Academic')">Academic</button>
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Facilities')">Facilities</button>
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Technical')">Technical</button>
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Hostel')">Hostel</button>
                            <button type="button" class="cat-pill" onclick="setComplaintCategory('Library')">Library</button>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="complaintPriority">Priority Level (SLA Resolution Target)</label>
                        <select id="complaintPriority" style="width:100%; padding: 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: 12px; color: #fff; font-family: inherit;">
                            <option value="HIGH">HIGH (Auto-Escalates after 2 Days)</option>
                            <option value="MEDIUM" selected>MEDIUM (Auto-Escalates after 5 Days)</option>
                            <option value="LOW">LOW (Auto-Escalates after 10 Days)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="complaintDesc">Detailed Grievance Statement</label>
                        <textarea id="complaintDesc" rows="4" style="width:100%; padding: 12px; background: var(--bg-input); border: 1px solid var(--border-subtle); border-radius: 12px; color: #fff; font-family: inherit; resize: vertical;" placeholder="Describe what occurred, dates, and relevant details..." required></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary" id="btnCommitComplaint">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
                        <span>Commit Complaint</span>
                    </button>
                </form>
            </div>

            <!-- Right: Complaints Listing with Filter Tabs -->
            <div class="panel-card">
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom: 14px;">
                    <h3>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                        <span>Complaints Tracker</span>
                    </h3>
                    <span style="font-size:0.82rem; color:var(--text-muted);">
                        Filter: <strong>${filter}</strong> (${filteredList.length} shown)
                    </span>
                </div>

                <!-- Filter Tabs: All, Pending, In Progress, Resolved, Escalated -->
                <div class="filter-tabs-bar">
                    <button type="button" class="filter-tab ${filter === 'ALL' ? 'active' : ''}" onclick="setStudentFilter('ALL')">
                        All <span class="filter-count">${allStudentComplaints.length}</span>
                    </button>
                    <button type="button" class="filter-tab ${filter === 'PENDING' ? 'active' : ''}" onclick="setStudentFilter('PENDING')">
                        Pending <span class="filter-count">${pendingComplaints.length}</span>
                    </button>
                    <button type="button" class="filter-tab ${filter === 'IN_PROGRESS' ? 'active' : ''}" onclick="setStudentFilter('IN_PROGRESS')">
                        In Progress <span class="filter-count">${inProgressComplaints.length}</span>
                    </button>
                    <button type="button" class="filter-tab ${filter === 'RESOLVED' ? 'active' : ''}" onclick="setStudentFilter('RESOLVED')">
                        Resolved <span class="filter-count">${resolvedComplaints.length}</span>
                    </button>
                    ${escalatedComplaints.length > 0 ? `
                        <button type="button" class="filter-tab ${filter === 'ESCALATED' ? 'active' : ''}" onclick="setStudentFilter('ESCALATED')">
                            Escalated <span class="filter-count">${escalatedComplaints.length}</span>
                        </button>
                    ` : ''}
                </div>

                ${filteredList.length === 0 ? `
                    <div class="empty-state">
                        <div class="empty-state-icon">📂</div>
                        <h4>No ${filter === 'ALL' ? '' : filter.toLowerCase()} complaints found</h4>
                        <p>${filter === 'ALL' ? 'Use the form on the left to commit your first complaint.' : `You have 0 complaints currently matching '${filter}'. Click 'All' to see all your complaints.`}</p>
                    </div>
                ` : `
                    <div class="table-responsive">
                        <table class="complaints-table">
                            <thead>
                                <tr>
                                    <th>Ticket ID</th>
                                    <th>Category</th>
                                    <th>Priority</th>
                                    <th>Filed On</th>
                                    <th>Status</th>
                                    <th>Handler</th>
                                    <th>Audit</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${filteredList.map(c => `
                                    <tr>
                                        <td>
                                            <strong style="color:#f8fafc; font-family:var(--font-heading);">${c.complaintId}</strong>
                                        </td>
                                        <td>
                                            <div style="font-weight:600; color:#e2e8f0;">${c.type}</div>
                                            <div style="font-size:0.75rem; color:var(--text-muted); max-width: 180px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${c.description}">
                                                ${c.description}
                                            </div>
                                        </td>
                                        <td><span class="priority-tag priority-${c.priority.toLowerCase()}">${c.priority}</span></td>
                                        <td><span style="font-size:0.82rem; color:var(--text-muted);">${c.dateFiled}</span></td>
                                        <td>${getStatusBadge(c.status)}</td>
                                        <td>
                                            <span style="font-size:0.82rem; color:${c.assignedAdmin ? '#cbd5e1' : 'var(--text-dim)'};">
                                                ${c.assignedAdmin ? c.assignedAdmin.name : '<em>In Queue</em>'}
                                            </span>
                                        </td>
                                        <td>
                                            <button type="button" class="chip-btn" onclick="viewAuditTrail('${c.complaintId}')" title="View complete history log">Audit Trail</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `}
            </div>
        </div>
    `;
}

// Admin Dashboard View
function renderAdminDashboard() {
    const all = SystemState.complaints;

    return `
        <div class="panel-card" style="margin-bottom: 24px;">
            <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
                <h3>Administrator Command Queue (${all.length} Total Tickets)</h3>
                <div style="display:flex; gap: 8px; flex-wrap:wrap;">
                    <button type="button" class="chip-btn" onclick="sortComplaints('priority')">Sort by Priority (Urgent First)</button>
                    <button type="button" class="chip-btn" onclick="sortComplaints('date')">Sort by Date Filed</button>
                </div>
            </div>

            <div class="table-responsive">
                <table class="complaints-table">
                    <thead>
                        <tr>
                            <th>Ticket ID</th>
                            <th>Student</th>
                            <th>Priority</th>
                            <th>Category</th>
                            <th>Status</th>
                            <th>Assigned Handler</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody id="adminTableBody">
                        ${all.map(c => `
                            <tr>
                                <td><strong>${c.complaintId}</strong></td>
                                <td>${c.filedBy.name} (${c.filedBy.id})</td>
                                <td><span class="priority-tag priority-${c.priority.toLowerCase()}">${c.priority}</span></td>
                                <td>${c.type}</td>
                                <td>${getStatusBadge(c.status)}</td>
                                <td>${c.assignedAdmin ? c.assignedAdmin.name : '<em style="color:var(--text-dim)">Unassigned</em>'}</td>
                                <td>
                                    <button type="button" class="chip-btn" onclick="adminTriageTicket('${c.complaintId}')">Manage</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

// ==========================================
// 5. COMPLAINT ACTIONS & LOGIC
// ==========================================

function getStatusBadge(status) {
    switch (status) {
        case 'OPEN': return `<span class="badge badge-open">● PENDING</span>`;
        case 'IN_PROGRESS': return `<span class="badge badge-progress">⚡ IN PROGRESS</span>`;
        case 'ESCALATED': return `<span class="badge badge-escalated">🔥 ESCALATED</span>`;
        case 'RESOLVED': return `<span class="badge badge-resolved">✓ RESOLVED</span>`;
        case 'CLOSED': return `<span class="badge badge-closed">🔒 CLOSED</span>`;
        default: return status;
    }
}

function submitStudentComplaint(e) {
    e.preventDefault();
    const type = document.getElementById('complaintType').value.trim();
    const priority = document.getElementById('complaintPriority').value;
    const description = document.getElementById('complaintDesc').value.trim();

    if (!type || !description) return;

    const newId = `CMP-${String(SystemState.nextComplaintSeq++).padStart(4, '0')}`;
    const nowStr = formatDate(new Date());

    const newComplaint = {
        complaintId: newId,
        type,
        description,
        priority,
        daysAgo: 0,
        dateFiled: nowStr,
        status: "OPEN",
        filedBy: {
            id: SystemState.currentUser.id,
            name: SystemState.currentUser.name
        },
        assignedAdmin: null,
        statusHistory: [
            { status: "OPEN", timestamp: formatDateTime(new Date()), note: `Complaint filed by ${SystemState.currentUser.name}` }
        ]
    };

    SystemState.complaints.unshift(newComplaint);
    SystemState.studentFilter = 'ALL';
    SystemState.currentUser.welcomeMsg = `Grievance committed successfully as ticket ${newId} with Priority ${priority}! Real-time SLA tracking initialized.`;
    renderDashboard();
}

function viewAuditTrail(complaintId) {
    const comp = SystemState.complaints.find(c => c.complaintId === complaintId);
    if (!comp) return;

    let historyHtml = comp.statusHistory.map(h => `
        <div style="padding: 10px; background: rgba(255,255,255,0.03); border-radius: 8px; margin-bottom: 8px; font-size: 0.85rem;">
            <div style="color:var(--text-muted); font-size: 0.76rem;">${h.timestamp} &bull; <strong>${h.status}</strong></div>
            <div style="color:#f8fafc; margin-top: 4px;">${h.note}</div>
        </div>
    `).join('');

    const modal = document.createElement('div');
    modal.style.position = 'fixed';
    modal.style.inset = '0';
    modal.style.background = 'rgba(0,0,0,0.75)';
    modal.style.backdropFilter = 'blur(8px)';
    modal.style.zIndex = '999';
    modal.style.display = 'flex';
    modal.style.alignItems = 'center';
    modal.style.justifyContent = 'center';
    modal.style.padding = '20px';

    modal.innerHTML = `
        <div style="background: var(--bg-card); border: 1px solid var(--border-subtle); border-radius: 20px; padding: 28px; width: 100%; max-width: 520px; max-height: 80vh; overflow-y:auto;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;">
                <h3 style="font-family:var(--font-heading); font-size:1.3rem;">Audit Trail: ${comp.complaintId}</h3>
                <button type="button" class="chip-btn" id="closeAuditModal">✕ Close</button>
            </div>
            <p style="font-size:0.9rem; color:var(--text-muted); margin-bottom: 16px;">${comp.description}</p>
            <div style="margin-top: 12px;">
                ${historyHtml}
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    modal.querySelector('#closeAuditModal').onclick = () => document.body.removeChild(modal);
}

function adminTriageTicket(complaintId) {
    const comp = SystemState.complaints.find(c => c.complaintId === complaintId);
    if (!comp) return;

    const action = prompt(
        `Manage Ticket: ${comp.complaintId} (${comp.status})\n` +
        `1. Assign to me (${SystemState.currentUser.name})\n` +
        `2. Mark IN PROGRESS\n` +
        `3. Mark RESOLVED\n` +
        `4. Mark CLOSED\n\n` +
        `Enter option (1-4):`
    );

    if (!action) return;

    const now = formatDateTime(new Date());
    const admin = SystemState.currentUser;

    if (action === "1") {
        comp.assignedAdmin = { id: admin.id, name: admin.name };
        comp.statusHistory.push({ status: comp.status, timestamp: now, note: `Assigned to handler ${admin.name}` });
        alert(`Assigned ticket ${comp.complaintId} to ${admin.name}.`);
    } else if (action === "2") {
        comp.status = "IN_PROGRESS";
        comp.assignedAdmin = { id: admin.id, name: admin.name };
        comp.statusHistory.push({ status: "IN_PROGRESS", timestamp: now, note: `Triage in progress [By: ${admin.name}]` });
    } else if (action === "3") {
        const note = prompt("Enter resolution notes:") || "Issue solved successfully.";
        comp.status = "RESOLVED";
        comp.statusHistory.push({ status: "RESOLVED", timestamp: now, note: `${note} [Resolved by: ${admin.name}]` });
    } else if (action === "4") {
        comp.status = "CLOSED";
        comp.statusHistory.push({ status: "CLOSED", timestamp: now, note: `Ticket closed by ${admin.name}` });
    }

    renderDashboard();
}

function triggerEscalationScan() {
    let escalatedCount = 0;
    const seniorAdmin = SystemState.admins.find(a => a.isSenior);

    SystemState.complaints.forEach(c => {
        if (c.status === "OPEN" || c.status === "IN_PROGRESS") {
            const threshold = SystemState.thresholds[c.priority] || 5;
            if (c.daysAgo > threshold) {
                c.status = "ESCALATED";
                c.assignedAdmin = seniorAdmin ? { id: seniorAdmin.id, name: seniorAdmin.name } : c.assignedAdmin;
                c.statusHistory.push({
                    status: "ESCALATED",
                    timestamp: formatDateTime(new Date()),
                    note: `Escalated: overdue by ${c.daysAgo - threshold} day(s) beyond ${threshold}d SLA threshold. Routed to Senior Admin.`
                });
                escalatedCount++;
            }
        }
    });

    if (escalatedCount > 0) {
        alert(`SLA Scan Complete: ${escalatedCount} overdue ticket(s) breached SLA and were ESCALATED to Senior Administration.`);
    } else {
        alert("SLA Scan Complete: All active tickets are currently within permitted resolution windows.");
    }

    if (SystemState.currentUser) {
        renderDashboard();
    }
}

function sortComplaints(mode) {
    if (mode === 'priority') {
        const order = { HIGH: 3, MEDIUM: 2, LOW: 1 };
        SystemState.complaints.sort((a, b) => order[b.priority] - order[a.priority]);
    } else if (mode === 'date') {
        SystemState.complaints.sort((a, b) => new Date(a.dateFiled) - new Date(b.dateFiled));
    }
    renderDashboard();
}

function attachDashboardEvents() {
    // Optional additional event listeners
}
