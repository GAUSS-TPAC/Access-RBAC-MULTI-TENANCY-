// src/app/superadmin/login/page.tsx
'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';

export default function SuperAdminLogin() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = () => {
    // Mock super-admin credentials – replace with real API call later
    if (email === 'superadmin@platform.com' && password === 'supersecure123') {
      localStorage.setItem('superRole', 'superadmin');
      router.push('/superadmin/dashboard');
    } else {
      setError('Identifiants incorrects');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 to-indigo-100 px-4">
      <Card className="w-full max-w-md shadow-2xl">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-bold text-purple-800">
            Super Admin – Gestion Globale
          </CardTitle>
          <p className="text-sm text-gray-600 mt-2">
            Accès réservé au propriétaire de la plateforme
          </p>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-4">
            <div>
              <Label htmlFor="email">Email super-admin</Label>
              <Input
                id="email"
                type="email"
                placeholder="superadmin@platform.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div>
              <Label htmlFor="password">Mot de passe</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {error && <p className="text-red-600 text-center font-medium">{error}</p>}

          <Button
            onClick={handleLogin}
            className="w-full bg-purple-700 hover:bg-purple-800"
          >
            Se connecter
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}