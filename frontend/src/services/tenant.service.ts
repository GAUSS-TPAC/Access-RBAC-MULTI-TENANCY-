import { api } from './api';

export interface Tenant {
  id: string;
  name: string;
  code: string;
  status: string;
  createdAt: string;
}

export interface CreateTenantPayload {
  name: string;
  code: string;
}

export const TenantService = {
  // Créer un tenant
  create(payload: CreateTenantPayload): Promise<void> {
    return api.post('/api/tenants', payload).then(() => {});
  },

  // Lister tous les tenants (filtrés par RBAC backend)
  list(): Promise<Tenant[]> {
    return api.get('/api/tenants').then(response => response.data);
  },

  // Récupérer un tenant spécifique
  get(id: string): Promise<Tenant> {
    return api.get(`/api/tenants/${id}`).then(response => response.data);
  }
};