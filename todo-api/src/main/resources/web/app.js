const authSection = document.getElementById('authSection');
const registerCard = document.getElementById('registerCard');
const taskSection = document.getElementById('taskSection');
const taskList = document.getElementById('taskList');
const taskFormCard = document.getElementById('taskFormCard');

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const newTaskButton = document.getElementById('newTaskButton');
const taskForm = document.getElementById('taskForm');
const logoutButton = document.getElementById('logoutButton');
const showRegister = document.getElementById('showRegister');
const showLogin = document.getElementById('showLogin');

showRegister.addEventListener('click', (event) => {
  event.preventDefault();
  registerCard.classList.remove('hidden');
});

showLogin.addEventListener('click', (event) => {
  event.preventDefault();
  registerCard.classList.add('hidden');
});

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = new FormData(loginForm);
  const response = await fetch('/api/login', {
    method: 'POST',
    body: new URLSearchParams(data),
  });

  if (response.ok) {
    showTaskView();
    loadTasks();
  } else {
    const body = await response.json();
    alert(body.error || 'Erro ao fazer login');
  }
});

registerForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = new FormData(registerForm);
  const response = await fetch('/api/register', {
    method: 'POST',
    body: new URLSearchParams(data),
  });

  if (response.ok) {
    showTaskView();
    loadTasks();
  } else {
    const body = await response.json();
    alert(body.error || 'Erro ao registrar');
  }
});

logoutButton.addEventListener('click', async () => {
  await fetch('/api/logout', { method: 'POST' });
  showAuthView();
});

newTaskButton.addEventListener('click', () => {
  taskFormCard.classList.toggle('hidden');
});

taskForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const data = new FormData(taskForm);
  const response = await fetch('/api/tasks', {
    method: 'POST',
    body: new URLSearchParams(data),
  });

  if (response.ok) {
    taskForm.reset();
    taskFormCard.classList.add('hidden');
    loadTasks();
  } else {
    const body = await response.json();
    alert(body.error || 'Erro ao criar tarefa');
  }
});

async function loadTasks() {
  const response = await fetch('/api/tasks');
  if (response.ok) {
    const tasks = await response.json();
    taskList.innerHTML = tasks.map(renderTask).join('');
  } else {
    taskList.innerHTML = '<p>Não foi possível carregar as tarefas.</p>';
  }
}

function renderTask(task) {
  return `
    <div class="task-card">
      <div>
        <strong>${task.title}</strong>
        <p>${task.description || ''}</p>
      </div>
      <div class="meta">
        <span>Prioridade: ${task.priority}</span>
        <span>Vencimento: ${task.dueDate || 'não definida'}</span>
      </div>
    </div>
  `;
}

function showTaskView() {
  authSection.classList.add('hidden');
  taskSection.classList.remove('hidden');
}

function showAuthView() {
  authSection.classList.remove('hidden');
  taskSection.classList.add('hidden');
  registerCard.classList.add('hidden');
}

showAuthView();
