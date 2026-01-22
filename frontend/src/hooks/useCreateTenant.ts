import { useState } from 'react';
import { TenantService } from '@/services/tenant.service';
import { generateTenantCode } from '@/lib/tenant-code';

export function useCreateTenant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function create(name: string) {
    try {
      setLoading(true);
      setError(null);

      await TenantService.create({
        name,
        code: generateTenantCode(name),
      });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Unknown error');
    } finally {
      setLoading(false);
    }
  }

  return { create, loading, error };
}
