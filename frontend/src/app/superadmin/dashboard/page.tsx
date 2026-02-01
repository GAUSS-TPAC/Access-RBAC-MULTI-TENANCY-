// src/app/superadmin/dashboard/page.tsx
'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { LogOut, Building2, Users, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

const mockOrganizations = [
  {
    id: 'org_1',
    name: 'Hello Community',
    adminName: 'Adam Johnson',
    adminEmail: 'adam@hellocommunity.com',
    userCount: 48,
    deptCount: 5,
    createdAt: '15 Oct 2025',
    status: 'Active',
  },
  {
    id: 'org_2',
    name: 'TechNova Solutions',
    adminName: 'Sophie Laurent',
    adminEmail: 'sophie@technova.io',
    userCount: 132,
    deptCount: 12,
    createdAt: '03 Nov 2025',
    status: 'Active',
  },
  {
    id: 'org_3',
    name: 'Green Future NGO',
    adminName: 'Lucas Bertrand',
    adminEmail: 'lucas@greenfuture.org',
    userCount: 19,
    deptCount: 3,
    createdAt: '20 Dec 2025',
    status: 'Pending',
  },
];

export default function SuperAdminDashboard() {
  const router = useRouter();

  useEffect(() => {
    const role = localStorage.getItem('superRole');
    if (role !== 'superadmin') {
      router.push('/superadmin/login');
    }
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem('superRole');
    window.location.href = '/superadmin/login';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top bar with title and logout */}
      <div className="bg-white border-b shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-5 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Super Admin – Vue Globale</h1>
            <p className="text-gray-600 mt-1">
              Aperçu de toutes les organisations inscrites sur la plateforme
            </p>
          </div>

          {/* Small logout button */}
          <Button
            variant="outline"
            size="sm"
            onClick={handleLogout}
            className="text-red-600 hover:text-red-700 border-red-200 hover:bg-red-50"
          >
            <LogOut className="h-4 w-4 mr-2" />
            Déconnexion
          </Button>
        </div>
      </div>

      {/* Main content */}
      <div className="max-w-7xl mx-auto px-6 py-10 space-y-12">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          <Card className="border-none shadow-md hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-lg font-medium text-gray-700">
                Organisations
              </CardTitle>
              <Building2 className="h-6 w-6 text-purple-600" />
            </CardHeader>
            <CardContent>
              <div className="text-4xl font-bold">12</div>
              <p className="text-sm text-gray-500 mt-1">Actives sur la plateforme</p>
            </CardContent>
          </Card>

          <Card className="border-none shadow-md hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-lg font-medium text-gray-700">
                Utilisateurs totaux
              </CardTitle>
              <Users className="h-6 w-6 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-4xl font-bold">1,245</div>
              <p className="text-sm text-gray-500 mt-1">Répartis dans toutes les orgs</p>
            </CardContent>
          </Card>

          <Card className="border-none shadow-md hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-lg font-medium text-gray-700">
                Départements totaux
              </CardTitle>
              <Building2 className="h-6 w-6 text-blue-600" />
            </CardHeader>
            <CardContent>
              <div className="text-4xl font-bold">87</div>
              <p className="text-sm text-gray-500 mt-1">Moyenne 7.25 par organisation</p>
            </CardContent>
          </Card>
        </div>

        {/* Organizations Table */}
        <Card className="border-none shadow-lg">
          <CardHeader>
            <CardTitle className="text-2xl font-bold text-gray-900">
              Organisations inscrites
            </CardTitle>
            <p className="text-gray-600 mt-1">
              {mockOrganizations.length} organisations actives sur la plateforme
            </p>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="bg-gray-50 border-b">
                    <TableHead className="py-4 px-6 font-semibold text-gray-700">Organisation</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700">Admin principal</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700">Email admin</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700 text-center">Utilisateurs</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700 text-center">Départements</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700">Créée le</TableHead>
                    <TableHead className="py-4 px-6 font-semibold text-gray-700 text-center">Statut</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {mockOrganizations.map((org, index) => (
                    <TableRow
                      key={org.id}
                      className={`hover:bg-gray-50 transition-colors ${
                        index % 2 === 0 ? 'bg-white' : 'bg-gray-50/50'
                      }`}
                    >
                      <TableCell className="py-5 px-6 font-medium">{org.name}</TableCell>
                      <TableCell className="py-5 px-6">{org.adminName}</TableCell>
                      <TableCell className="py-5 px-6 text-gray-600">{org.adminEmail}</TableCell>
                      <TableCell className="py-5 px-6 text-center font-medium">{org.userCount}</TableCell>
                      <TableCell className="py-5 px-6 text-center font-medium">{org.deptCount}</TableCell>
                      <TableCell className="py-5 px-6 text-gray-600">{org.createdAt}</TableCell>
                      <TableCell className="py-5 px-6 text-center">
                        <Badge
                          variant={org.status === 'Active' ? 'default' : 'secondary'}
                          className={
                            org.status === 'Active'
                              ? 'bg-green-100 text-green-800 hover:bg-green-100'
                              : 'bg-amber-100 text-amber-800 hover:bg-amber-100'
                          }
                        >
                          {org.status}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}