'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import CreateOrganizationTab from '@/components/CreateOrganizationTab';

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [activeTab, setActiveTab] = useState('signin');
  const [isLoading, setIsLoading] = useState(false);

  // Initialiser l'onglet actif depuis l'URL
  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab === 'create') {
      setActiveTab('create');
    }
  }, [searchParams]);

  // Mock authentication pour tests (à remplacer par vrai auth plus tard)
  const handleLogin = async (email: string, password: string) => {
    setIsLoading(true);

    try {
      // TODO: Remplacer par vrai appel API
      // Pour l'instant, générer un JWT mock
      const mockToken = 'mock-jwt-token-for-development';
      if (typeof window !== 'undefined') {
        localStorage.setItem('access_token', mockToken);
      }

      // Rediriger vers le dashboard
      router.push('/dashboard');
    } catch (error) {
      console.error('Login error:', error);
      alert('Échec de la connexion');
    } finally {
      setIsLoading(false);
    }
  };

  const handleMockLogin = (role: 'admin' | 'manager' | 'member') => {
    setIsLoading(true);

    // Mock login pour développement
    if (typeof window !== 'undefined') {
      localStorage.setItem('access_token', `mock-jwt-${role}`);
      localStorage.setItem('user_role', role);
    }

    setTimeout(() => {
      if (role === 'admin') {
        router.push('/dashboard');
      } else {
        router.push('/community');
      }
    }, 500);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">Gestionnaire de Communauté</CardTitle>
          <CardDescription className="mt-2">
            Plateforme multi-tenant RBAC
          </CardDescription>
        </CardHeader>

        <CardContent>
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="signin">Connexion</TabsTrigger>
              <TabsTrigger value="create">Créer Organisation</TabsTrigger>
            </TabsList>

            {/* Tab Connexion */}
            <TabsContent value="signin" className="space-y-4 mt-4">
              <div>
                <Label htmlFor="email-signin">Email</Label>
                <Input
                  id="email-signin"
                  type="email"
                  placeholder="admin@example.com"
                  required
                  defaultValue="admin@example.com"
                />
              </div>
              <div>
                <Label htmlFor="password-signin">Mot de passe</Label>
                <Input
                  id="password-signin"
                  type="password"
                  required
                  defaultValue="password"
                />
              </div>

              <Button
                className="w-full"
                onClick={() => handleLogin('admin@example.com', 'password')}
                disabled={isLoading}
              >
                {isLoading ? 'Connexion...' : 'Se connecter'}
              </Button>

              {/* Boutons mock pour développement */}
              <div className="pt-4 border-t">
                <p className="text-sm text-gray-500 mb-3">Développement seulement:</p>
                <div className="grid grid-cols-3 gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleMockLogin('admin')}
                    className="text-red-600 border-red-200 hover:bg-red-50"
                  >
                    Admin
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleMockLogin('manager')}
                    className="text-blue-600 border-blue-200 hover:bg-blue-50"
                  >
                    Manager
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleMockLogin('member')}
                    className="text-green-600 border-green-200 hover:bg-green-50"
                  >
                    Membre
                  </Button>
                </div>
              </div>
            </TabsContent>

            {/* Tab Création Organisation */}
            <TabsContent value="create" className="space-y-4 mt-4">
              <CreateOrganizationTab />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}