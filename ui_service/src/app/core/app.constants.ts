export const APP_CONFIG = {
  apiBaseUrl: '/api',
  keycloak: {
    url: 'http://localhost:8080',
    realm: 'ecommerce',
    clientId: 'ecommerce-ui'
  }
} as const;
