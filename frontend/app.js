const app = document.getElementById('app');
const API_BASE = window.API_BASE_URL || 'http://localhost:8081/api/v1';
const state = { token: localStorage.getItem('jwt') || '', user: null, links: {} };
if (new URLSearchParams(location.search).get('logout') === '1') {
  localStorage.removeItem('jwt');
  sessionStorage.clear();
  state.token = '';
}
const route = location.pathname.replace(/^\/+|\/+$/g, '').split('/').filter(Boolean);
const queryPage = new URLSearchParams(location.search).get('page');
const page = queryPage || (route[0] === 'admin' ? (route[1] || 'dashboard') : (route[0] || 'home'));
const resourceId = route[1] && route[0] !== 'admin' ? route[1] : null;

const routes = {
  dashboard: '/admin/dashboard',
  students: '/admin/students',
  teachers: '/admin/teachers',
  courses: '/admin/courses',
  enrollments: '/admin/enrollments'
};

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${state.token}`, ...(options.headers || {}) }
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`);
  return data;
}

function field(label, name, type = 'text', required = true) {
  return `<label>${label}<input name="${name}" type="${type}" ${required ? 'required' : ''}></label>`;
}

function tableRows(items, columns, actions = false, editable = false) {
  if (!items.length) return `<tr><td colspan="${columns.length + (actions ? 1 : 0)}">No records found.</td></tr>`;
  return items.map(item => `<tr>${columns.map(column => `<td>${column(item) ?? ''}</td>`).join('')}${actions ? `<td>${editable ? `<button class="secondary edit" data-id="${item.id}" data-user-id="${item.user?.id}">Edit</button> ` : ''}<button class="danger delete" data-id="${item.id}">Delete</button></td>` : ''}</tr>`).join('');
}

async function renderResource(resource, id) {
  const item = await request(`/${resource}/${encodeURIComponent(id)}`);
  const title = resource.charAt(0).toUpperCase() + resource.slice(1, -1);
  const profile = item.user || item;
  const resourceFields = resource === 'students' ? {
    'Student number': item.studentNumber, Department: item.department, Program: item.program,
    Semester: item.semester, 'Enrollment year': item.enrollmentYear
  } : resource === 'teachers' ? {
    'Employee number': item.employeeNumber, Department: item.department,
    Designation: item.designation, Specialization: item.specialization
  } : {
    'Course code': item.courseCode, Title: item.title, Description: item.description,
    'Credit hours': item.creditHours, Department: item.department,
    Teacher: item.teacher?.user?.name || item.teacher?.id
  };
  const fields = Object.entries({ Name: profile.name, Email: profile.email, ...resourceFields })
    .filter(([, value]) => value !== null && value !== undefined);

  app.innerHTML += `<section class="card profile-card">
    <a class="back-link" href="${routes[resource]}">← Back to ${title}s</a>
    <div class="profile-header"><div class="profile-avatar">${(profile.name || title).charAt(0).toUpperCase()}</div>
      <div><p class="eyebrow">${title.toUpperCase()} PROFILE</p><h2>${profile.name || `${title} #${id}`}</h2><p class="muted">${profile.email || ''}</p></div>
    </div>
    <div class="profile-grid">${fields.map(([label, value]) => `<div class="profile-field"><span>${label}</span><strong>${value}</strong></div>`).join('')}</div>
  </section>`;
}

async function createNavigationLinks() {
  state.links = routes;
}

function nav() {
  const isAdmin = state.user?.role === 'ADMIN';
  const links = isAdmin
    ? `<li><a class="${page === 'students' ? 'active' : ''}" href="${routes.students}">Students</a></li>
       <li><a class="${page === 'teachers' ? 'active' : ''}" href="${routes.teachers}">Teachers</a></li>
       <li><a class="${page === 'courses' ? 'active' : ''}" href="${routes.courses}">Courses</a></li>
       <li><a class="${page === 'enrollments' ? 'active' : ''}" href="${routes.enrollments}">Enrollments</a></li>`
    : `<li><a class="${page === 'courses' ? 'active' : ''}" href="${routes.courses}">Courses</a></li>`;
  return `<nav class="topbar"><a class="brand" href="/"><span class="brand-mark">U</span><span><strong>Uni<span>core</span></strong><small>Management portal</small></span></a><ul class="nav-links">
    <li><a class="${page === 'home' || page === 'dashboard' ? 'active' : ''}" href="${routes.dashboard}">Overview</a></li>${links}
  </ul><div class="account"><span class="avatar">${(state.user?.name || 'A').charAt(0).toUpperCase()}</span><span class="account-name">${state.user?.name || 'Administrator'}</span><a class="logout-link" id="logoutBtn" href="/?logout=1">Log out</a></div></nav>`;
}

function bindLogout() {
  const button = document.getElementById('logoutBtn');
  if (!button) return;
  button.onclick = () => {
    localStorage.removeItem('jwt'); sessionStorage.clear(); state.token = ''; state.user = null; render();
  };
}

document.addEventListener('click', event => {
  if (!(event.target instanceof Element) || !event.target.closest('#logoutBtn')) return;
  localStorage.removeItem('jwt');
  sessionStorage.clear();
  state.token = '';
  state.user = null;
  renderLogin();
});

async function loadUser() {
  try {
    state.user = await request('/auth/me');
    await render();
  } catch (error) {
    localStorage.removeItem('jwt');
    state.token = '';
    state.user = null;
    renderLogin();
  }
}

function renderLogin() {
  app.innerHTML = `<div class="login-shell">
    <div class="login-art">
      <div class="orb orb-one"></div><div class="orb orb-two"></div>
      <div class="login-brand"><span class="brand-mark">U</span><span><strong>Uni<span>core</span></strong><small>Management portal</small></span></div>
      <div class="login-message"><p class="eyebrow">SMARTER CAMPUS OPERATIONS</p><h1>One beautiful place to run your university.</h1><p>Bring students, faculty, courses, and enrollment data together in a clear, connected workspace.</p></div>
      <div class="login-art-footer"><span>✦</span> Secure administration <span>•</span> PostgreSQL powered</div>
    </div>
    <div class="login-panel"><div class="login-panel-inner">
      <div class="mobile-login-brand"><span class="brand-mark">U</span><strong>Unicore</strong></div>
      <p class="eyebrow">WELCOME BACK</p><h2>Sign in to your workspace</h2><p class="login-subtitle">Use your administrator account to continue.</p>
      <form id="loginForm"><div class="form-grid">${field('Email address', 'email', 'email')}${field('Password', 'password', 'password')}</div>
      <div class="login-options"><label class="check-label"><input type="checkbox"> Remember me</label><span>Administrator access</span></div>
      <button class="login-button" type="submit">Sign in <span>→</span></button></form>
      <p class="login-help">Accounts are created by your system administrator.</p>
    </div></div>
  </div>`;
  document.getElementById('loginForm').onsubmit = async event => {
    event.preventDefault();
    try {
      const data = await request('/auth/login', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.target))) });
      state.token = data.token; localStorage.setItem('jwt', state.token); await loadUser();
    } catch (error) { alert(error.message); }
  };
}

async function renderStudents() {
  if (resourceId) return renderResource('students', resourceId);
  const data = await request('/students?page=0&size=100');
  app.innerHTML += `<section class="card"><div class="row"><h2>Students</h2><button id="addStudent">Add Student</button></div>
    <form id="studentForm" class="hidden"><div class="form-grid">${field('Name', 'name')}${field('Email', 'email', 'email')}${field('Password', 'password', 'password')}
    ${field('Student number', 'studentNumber')}${field('Department', 'department')}${field('Program', 'program')}${field('Semester', 'semester', 'number')}${field('Enrollment year', 'enrollmentYear', 'number')}</div><button>Save</button></form>
    <form id="studentEditForm" class="hidden crud-form"><div class="form-grid">${field('Name', 'name')}${field('Email', 'email', 'email')}
    ${field('Department', 'department')}${field('Program', 'program')}${field('Semester', 'semester', 'number')}${field('Enrollment year', 'enrollmentYear', 'number')}</div><button>Save changes</button></form>
    <table class="table"><thead><tr><th>Name</th><th>Email</th><th>Student number</th><th>Department</th><th>Program</th><th>Semester</th><th>Year</th><th>Actions</th></tr></thead>
    <tbody>${tableRows(data.content, [x => `<a href="/students/${x.id}">${x.user?.name || ''}</a>`, x => x.user?.email, x => x.studentNumber, x => x.department, x => x.program, x => x.semester, x => x.enrollmentYear], true, true)}</tbody></table></section>`;
  document.getElementById('addStudent').onclick = () => document.getElementById('studentForm').classList.toggle('hidden');
  document.getElementById('studentForm').onsubmit = async event => {
    event.preventDefault(); const values = Object.fromEntries(new FormData(event.target));
    try {
      const user = await request('/users', { method: 'POST', body: JSON.stringify({ name: values.name, email: values.email, password: values.password, role: 'STUDENT' }) });
      await request('/students', { method: 'POST', body: JSON.stringify({ user: { id: user.id }, studentNumber: values.studentNumber, department: values.department, program: values.program, semester: Number(values.semester), enrollmentYear: Number(values.enrollmentYear) }) });
      location.reload();
    } catch (error) { alert(error.message); }
  };
  document.querySelectorAll('.edit').forEach(button => button.onclick = async () => {
    const student = await request(`/students/${button.dataset.id}`);
    const form = document.getElementById('studentEditForm');
    form.classList.remove('hidden');
    form.dataset.id = button.dataset.id;
    form.dataset.userId = button.dataset.userId;
    form.elements.name.value = student.user?.name || '';
    form.elements.email.value = student.user?.email || '';
    form.elements.department.value = student.department || '';
    form.elements.program.value = student.program || '';
    form.elements.semester.value = student.semester || '';
    form.elements.enrollmentYear.value = student.enrollmentYear || '';
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
  document.getElementById('studentEditForm').onsubmit = async event => {
    event.preventDefault(); const form = event.target; const values = Object.fromEntries(new FormData(form));
    try {
      await request(`/users/${form.dataset.userId}`, { method: 'PATCH', body: JSON.stringify({ name: values.name, email: values.email }) });
      await request(`/students/${form.dataset.id}`, { method: 'PATCH', body: JSON.stringify({ department: values.department, program: values.program, semester: Number(values.semester), enrollmentYear: Number(values.enrollmentYear) }) });
      location.reload();
    } catch (error) { alert(error.message); }
  };
  document.querySelectorAll('.delete').forEach(button => button.onclick = async () => {
    if (confirm('Delete this student?')) {
      try { await request(`/students/${button.dataset.id}`, { method: 'DELETE' }); location.reload(); }
      catch (error) { alert(error.message); }
    }
  });
}

async function renderTeachers() {
  if (resourceId) return renderResource('teachers', resourceId);
  const data = await request('/teachers?page=0&size=100');
  app.innerHTML += `<section class="card"><div class="row"><h2>Teachers</h2><button id="addTeacher">Add Teacher</button></div>
    <form id="teacherForm" class="hidden"><div class="form-grid">${field('Name', 'name')}${field('Email', 'email', 'email')}${field('Password', 'password', 'password')}
    ${field('Employee number', 'employeeNumber')}${field('Department', 'department')}${field('Designation', 'designation')}${field('Specialization', 'specialization')}</div><button>Save</button></form>
    <form id="teacherEditForm" class="hidden crud-form"><div class="form-grid">${field('Name', 'name')}${field('Email', 'email', 'email')}
    ${field('Department', 'department')}${field('Designation', 'designation')}${field('Specialization', 'specialization')}</div><button>Save changes</button></form>
    <table class="table"><thead><tr><th>Name</th><th>Email</th><th>Employee number</th><th>Department</th><th>Designation</th><th>Specialization</th><th>Actions</th></tr></thead>
    <tbody>${tableRows(data.content, [x => `<a href="/teachers/${x.id}">${x.user?.name || ''}</a>`, x => x.user?.email, x => x.employeeNumber, x => x.department, x => x.designation, x => x.specialization], true, true)}</tbody></table></section>`;
  document.getElementById('addTeacher').onclick = () => document.getElementById('teacherForm').classList.toggle('hidden');
  document.getElementById('teacherForm').onsubmit = async event => {
    event.preventDefault(); const values = Object.fromEntries(new FormData(event.target));
    try {
      const user = await request('/users', { method: 'POST', body: JSON.stringify({ name: values.name, email: values.email, password: values.password, role: 'TEACHER' }) });
      await request('/teachers', { method: 'POST', body: JSON.stringify({ user: { id: user.id }, employeeNumber: values.employeeNumber, department: values.department, designation: values.designation, specialization: values.specialization }) });
      location.reload();
    } catch (error) { alert(error.message); }
  };
  document.querySelectorAll('.edit').forEach(button => button.onclick = async () => {
    const teacher = await request(`/teachers/${button.dataset.id}`);
    const form = document.getElementById('teacherEditForm');
    form.classList.remove('hidden');
    form.dataset.id = button.dataset.id;
    form.dataset.userId = button.dataset.userId;
    form.elements.name.value = teacher.user?.name || '';
    form.elements.email.value = teacher.user?.email || '';
    form.elements.department.value = teacher.department || '';
    form.elements.designation.value = teacher.designation || '';
    form.elements.specialization.value = teacher.specialization || '';
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
  document.getElementById('teacherEditForm').onsubmit = async event => {
    event.preventDefault(); const form = event.target; const values = Object.fromEntries(new FormData(form));
    try {
      await request(`/users/${form.dataset.userId}`, { method: 'PATCH', body: JSON.stringify({ name: values.name, email: values.email }) });
      await request(`/teachers/${form.dataset.id}`, { method: 'PATCH', body: JSON.stringify({ department: values.department, designation: values.designation, specialization: values.specialization }) });
      location.reload();
    } catch (error) { alert(error.message); }
  };
  document.querySelectorAll('.delete').forEach(button => button.onclick = async () => {
    if (confirm('Delete this teacher?')) {
      try { await request(`/teachers/${button.dataset.id}`, { method: 'DELETE' }); location.reload(); }
      catch (error) { alert(error.message); }
    }
  });
}

async function renderEnrollments() {
  const data = await request('/enrollments?page=0&size=100');
  app.innerHTML += `<section class="card"><h2>Enrollments</h2>
    <table class="table"><thead><tr><th>Student</th><th>Course</th><th>Date</th><th>Status</th><th>Grade</th><th>Actions</th></tr></thead>
    <tbody>${tableRows(data.content, [x => x.student?.user?.name, x => x.course?.title, x => x.enrollmentDate, x => x.status, x => x.grade ?? 'Not graded'], true)}</tbody></table></section>`;
  document.querySelectorAll('.delete').forEach(button => button.onclick = async () => {
    if (confirm('Delete this enrollment?')) {
      try { await request(`/enrollments/${button.dataset.id}`, { method: 'DELETE' }); location.reload(); }
      catch (error) { alert(error.message); }
    }
  });
}

async function renderCourses() {
  if (resourceId) return renderResource('courses', resourceId);
  const canManage = state.user.role === 'ADMIN' || state.user.role === 'TEACHER';
  const data = await request(`${state.user.role === 'TEACHER' ? '/courses/mine' : '/courses'}?page=0&size=100`);
  app.innerHTML += `<section class="card"><div class="row"><h2>${state.user.role === 'TEACHER' ? 'My Courses' : 'Courses'}</h2>${canManage ? '<button id="addCourse">Add Course</button>' : ''}</div>
    ${canManage ? `<form id="courseForm" class="hidden"><div class="form-grid">${field('Course code', 'courseCode')}${field('Title', 'title')}${field('Description', 'description', 'text', false)}
    ${field('Credit hours', 'creditHours', 'number')}${field('Department', 'department')}${state.user.role === 'ADMIN' ? field('Teacher ID', 'teacherId', 'number') : ''}</div><button>Save</button></form>` : ''}
    <table class="table"><thead><tr><th>Code</th><th>Title</th><th>Department</th><th>Credits</th><th>Teacher</th><th>Actions</th></tr></thead>
    <tbody>${tableRows(data.content, [x => `<a href="/courses/${x.id}">${x.courseCode}</a>`, x => x.title, x => x.department, x => x.creditHours, x => x.teacher?.user?.name || x.teacher?.id], canManage)}</tbody></table></section>`;
  if (!canManage) return;
  document.getElementById('addCourse').onclick = () => document.getElementById('courseForm').classList.toggle('hidden');
  document.getElementById('courseForm').onsubmit = async event => {
    event.preventDefault(); const values = Object.fromEntries(new FormData(event.target));
    try {
      const course = { courseCode: values.courseCode, title: values.title, description: values.description, creditHours: Number(values.creditHours), department: values.department };
      if (state.user.role === 'ADMIN') course.teacher = { id: Number(values.teacherId) };
      await request('/courses', { method: 'POST', body: JSON.stringify(course) });
      location.reload();
    } catch (error) { alert(error.message); }
  };
  document.querySelectorAll('.delete').forEach(button => button.onclick = async () => {
    if (confirm('Delete this course?')) {
      try { await request(`/courses/${button.dataset.id}`, { method: 'DELETE' }); location.reload(); }
      catch (error) { alert(error.message); }
    }
  });
}

async function renderOwnProfile() {
  const isStudent = state.user.role === 'STUDENT';
  const resource = isStudent ? 'students' : 'teachers';
  const profile = await request(`/${resource}/me`);
  const user = profile.user || state.user;
  const details = isStudent
    ? [['Name', user.name], ['Email', user.email], ['Student number', profile.studentNumber], ['Department', profile.department], ['Program', profile.program], ['Semester', profile.semester], ['Enrollment year', profile.enrollmentYear]]
    : [['Name', user.name], ['Email', user.email], ['Employee number', profile.employeeNumber], ['Department', profile.department], ['Designation', profile.designation], ['Specialization', profile.specialization]];
  const fields = isStudent
    ? `${field('Department', 'department')}${field('Program', 'program')}${field('Semester', 'semester', 'number')}${field('Enrollment year', 'enrollmentYear', 'number')}`
    : `${field('Department', 'department')}${field('Designation', 'designation')}${field('Specialization', 'specialization')}`;
  document.querySelector('main').insertAdjacentHTML('beforeend', `<section class="card"><div class="row"><h2>My Profile</h2><button id="editOwnProfile" class="secondary" type="button">Edit profile</button></div>
    <div id="ownProfileDetails" class="profile-grid">${details.map(([label, value]) => `<div class="profile-field"><span>${label}</span><strong>${value || ''}</strong></div>`).join('')}</div>
    <form id="ownProfileForm" class="hidden crud-form"><div class="form-grid">${field('Name', 'name')}${field('Email', 'email', 'email')}${fields}</div><button>Save changes</button></form></section>`);
  const form = document.getElementById('ownProfileForm');
  const setValue = (name, value) => form.querySelector(`[name="${name}"]`).value = value || '';
  setValue('name', user.name);
  setValue('email', user.email);
  setValue('department', profile.department);
  if (isStudent) {
    setValue('program', profile.program);
    setValue('semester', profile.semester);
    setValue('enrollmentYear', profile.enrollmentYear);
  } else {
    setValue('designation', profile.designation);
    setValue('specialization', profile.specialization);
  }
  document.getElementById('editOwnProfile').onclick = () => {
    document.getElementById('ownProfileDetails').classList.add('hidden');
    form.classList.remove('hidden');
  };
  form.onsubmit = async event => {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(form));
    try {
      await request('/users/me', { method: 'PATCH', body: JSON.stringify({ name: values.name, email: values.email }) });
      const profileUpdate = isStudent
        ? { department: values.department, program: values.program, semester: Number(values.semester), enrollmentYear: Number(values.enrollmentYear) }
        : { department: values.department, designation: values.designation, specialization: values.specialization };
      await request(`/${resource}/me`, { method: 'PATCH', body: JSON.stringify(profileUpdate) });
      alert('Profile updated successfully.');
      await render();
    } catch (error) { alert(error.message); }
  };
}

async function render() {
  if (!state.token) return renderLogin();
  try {
    state.user = await request('/auth/me');
    await createNavigationLinks();
      const titles = { home: ['Overview', 'A clear view of your university operations.'], dashboard: ['Overview', 'A clear view of your university operations.'], students: ['Students', 'Manage student accounts and academic profiles.'], teachers: ['Teachers', 'Manage faculty members and teaching profiles.'], courses: ['Courses', 'Organize your academic catalog and assignments.'], enrollments: ['Enrollments', 'Track student registrations and academic progress.']       }
      if (state.user.role !== 'ADMIN') {
        app.innerHTML = `${nav()}<main><div class="page-heading"><div><p class="eyebrow">${state.user.role} PORTAL</p><h1>Welcome, ${state.user.name || state.user.email}</h1><p>Your account is ready. Use the available pages above to view your university information.</p></div></div><section class="card"><div class="dashboard-grid"><div class="metric"><span class="metric-icon blue">◈</span><div><h4>Role</h4><p>${state.user.role}</p></div></div><div class="metric"><span class="metric-icon green">✦</span><div><h4>Signed in as</h4><p>${state.user.email}</p></div></div></div></section>`;
        await renderOwnProfile();
        bindLogout();
        return;
      };
      const heading = titles[page] || titles.home;
      app.innerHTML = `${nav()}<main><div class="page-heading"><div><p class="eyebrow">ADMIN CONSOLE</p><h1>${heading[0]}</h1><p>${heading[1]}</p></div><span class="date-chip">${new Date().toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}</span></div><div class="dashboard-grid"><div class="metric"><span class="metric-icon blue">◈</span><div><h4>Role</h4><p>${state.user.role}</p></div></div><div class="metric"><span class="metric-icon green">✦</span><div><h4>Signed in as</h4><p>${state.user.email}</p></div></div></div>`;
    if (page === 'students') await renderStudents();
    else if (page === 'teachers') await renderTeachers();
    else if (page === 'courses') await renderCourses();
    else if (page === 'enrollments') await renderEnrollments();
    else app.innerHTML += '<section class="card"><h2>Welcome</h2><p>Use the navigation above to manage each area on its own page.</p></section>';
    bindLogout();
  } catch (error) {
    localStorage.removeItem('jwt'); state.token = ''; renderLogin();
  }
}

render();
