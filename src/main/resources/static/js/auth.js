class AuthManager {
  constructor() {
    this.keycloak = null;
    this.token = null;
    this.onAuthStatusChange = null;
  }

  async initialize() {
    this.keycloak = new Keycloak({
      url: "http://localhost:8180/",
      realm: "polygon-tiling",
      clientId: "tile-client",
    });

    try {
      const authenticated = await this.keycloak.init({
        onLoad: "login-required",
        pkceMethod: "S256",
        flow: "standard",
      });
      this.token = this.keycloak.token;
      this.setupTokenRefresh();
      this.updateUI(authenticated);
      this.setupEventListeners();
      return authenticated;
    } catch (error) {
      console.error("Failed to initialize Keycloak:", error);
      this.updateUI(false);
      return false;
    }
  }

  setupEventListeners() {
    const loginBtn = document.getElementById("loginButton");
    const registerBtn = document.getElementById("registerButton");
    const logoutBtn = document.getElementById("logoutButton");

    if (loginBtn) {
      loginBtn.addEventListener("click", () => this.login());
    }
    if (registerBtn) {
      registerBtn.addEventListener("click", () => this.register());
    }
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => this.logout());
    }
  }

  setupTokenRefresh() {
    setInterval(() => {
      this.keycloak
        .updateToken(60)
        .then((refreshed) => {
          if (refreshed) {
            this.token = this.keycloak.token;
          }
        })
        .catch(() => {
          console.error("Failed to refresh token");
          this.updateUI(false);
          this.keycloak.logout();
        });
    }, 60000);
  }

  updateUI(authenticated) {
    const unauthenticatedButtons = document.getElementById(
      "unauthenticated-buttons"
    );
    const authenticatedButtons = document.getElementById(
      "authenticated-buttons"
    );
    const userInfo = document.getElementById("userInfo");
    const loginStatus = document.getElementById("loginStatus");
    const controlsPanel = document.getElementById("controls-panel");

    if (authenticated) {
      unauthenticatedButtons.classList.add("hidden");
      authenticatedButtons.classList.remove("hidden");
      controlsPanel.classList.add("authenticated");

      const username =
        this.keycloak.tokenParsed.preferred_username ||
        this.keycloak.tokenParsed.email ||
        "User";
      userInfo.textContent = `Welcome, ${username}`;
      loginStatus.textContent = "✓ Authenticated";
      loginStatus.classList.add("text-green-600");
    } else {
      unauthenticatedButtons.classList.remove("hidden");
      authenticatedButtons.classList.add("hidden");
      controlsPanel.classList.remove("authenticated");
      userInfo.textContent = "";
      loginStatus.textContent = "Please log in to generate tiles";
      loginStatus.classList.remove("text-green-600");
    }

    if (this.onAuthStatusChange) {
      this.onAuthStatusChange(authenticated);
    }
  }

  login() {
    console.log("Login button clicked");
    if (this.keycloak) {
      console.log("Keycloak instance exists, attempting login...");
      this.keycloak
        .login({
          redirectUri: window.location.origin,
          prompt: "login",
        })
        .catch((error) => {
          console.error("Login failed:", error);
        });
    }
  }

  logout() {
    if (this.keycloak) {
      this.keycloak.logout({
        redirectUri: window.location.origin,
      });
    }
  }

  register() {
    if (this.keycloak) {
      window.location.href = `${this.keycloak.authServerUrl}/realms/${
        this.keycloak.realm
      }/protocol/openid-connect/registrations?client_id=${
        this.keycloak.clientId
      }&response_type=code&redirect_uri=${encodeURIComponent(
        window.location.origin
      )}`;
    }
  }

  getToken() {
    return this.token;
  }

  isAuthenticated() {
    return this.keycloak?.authenticated || false;
  }
}

window.authManager = new AuthManager(); // Make globally available
export const authManager = window.authManager;
