document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const usernameInput = document.getElementById('username').value.trim();
    const passwordInput = document.getElementById('password').value.trim();
    const errorMsg = document.getElementById('errorMsg');
    const loginBtn = document.getElementById('loginBtn');

    errorMsg.style.display = 'none';
    errorMsg.textContent = '';
    loginBtn.textContent = 'A autenticar...';
    loginBtn.disabled = true;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: usernameInput,
                password: passwordInput
            })
        });

        if (!response.ok) {
            throw new Error('Credenciais inválidas. Verifique o seu utilizador e palavra-passe.');
        }

        const data = await response.json();

        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('user_role', data.role);

        // Redireciona para o dashboard localizado na pasta html/
        window.location.href = '/html/dashboard.html';

    } catch (error) {
        errorMsg.textContent = error.message;
        errorMsg.style.display = 'block';
        loginBtn.textContent = 'Entrar';
        loginBtn.disabled = false;
    }
});