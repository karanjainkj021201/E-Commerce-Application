import { Injectable, signal } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { APP_CONFIG } from './app.constants';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = new Keycloak(APP_CONFIG.keycloak);
  readonly authenticated = signal(false);
  readonly username = signal('Guest');
  readonly roles = signal<string[]>([]);
  readonly ready = signal(false);
  private profile?: KeycloakProfile;

  async init(): Promise<void> {
    try {
      const loggedIn = await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
        checkLoginIframe: false
      });
      this.authenticated.set(loggedIn);
      this.syncTokenState();
      if (loggedIn) {
        this.profile = await this.keycloak.loadUserProfile();
        this.username.set(
          this.profile.firstName || this.profile.username || this.keycloak.tokenParsed?.['preferred_username'] || 'Customer'
        );
      }
      this.keycloak.onTokenExpired = () => void this.ensureFreshToken();
    } catch (error) {
      console.error('Keycloak initialization failed. Public pages remain available.', error);
      this.authenticated.set(false);
    } finally {
      this.ready.set(true);
    }
  }

  login(redirectPath = '/account'): Promise<void> {
    return this.keycloak.login({ redirectUri: `${window.location.origin}${redirectPath}` });
  }

  logout(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  register(): Promise<void> {
    return this.keycloak.register({ redirectUri: `${window.location.origin}/account` });
  }

  isAdmin(): boolean {
    return this.roles().includes('ADMIN');
  }

  getProfile(): KeycloakProfile | undefined {
    return this.profile;
  }

  async ensureFreshToken(): Promise<string | undefined> {
    if (!this.keycloak.authenticated) {
      return undefined;
    }
    try {
      await this.keycloak.updateToken(30);
      this.syncTokenState();
      return this.keycloak.token;
    } catch (error) {
      console.error('Unable to refresh access token', error);
      await this.logout();
      return undefined;
    }
  }

  private syncTokenState(): void {
    const realmRoles = this.keycloak.realmAccess?.roles ?? [];
    const clientRoles = Object.values(this.keycloak.resourceAccess ?? {}).flatMap(access => access.roles ?? []);
    this.roles.set([...new Set([...realmRoles, ...clientRoles])]);
  }
}
