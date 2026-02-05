'use client'; // Important: this is a Client Component

import { useState, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { Home, Users, Shield, Settings, LogOut, Building2, Activity } from 'lucide-react';
import Link from 'next/link';

export default function Sidebar() {
  const pathname = usePathname();

  const [role, setRole] = useState<string | null>(null);

  useEffect(() => {
    setRole(localStorage.getItem('userRole'));
  }, []);

  const isActive = (path: string) => pathname === path;

  return (
    <div className="w-64 bg-gray-900 text-white h-screen p-6 flex flex-col">
      <div className="mb-10 text-center">
        <div className="mx-auto h-12 w-12 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-900/20 mb-3">
          <Shield className="h-7 w-7 text-white" />
        </div>
        <h1 className="text-xl font-bold tracking-tight">
          YowAccess
        </h1>
        <p className="text-[10px] text-gray-500 uppercase tracking-widest mt-1">
          {role?.replace('_', ' ') || 'Chargement...'}
        </p>
      </div>

      <nav className="flex-1 overflow-y-auto pr-2 -mr-2">
        <ul className="space-y-1">
          <li>
            <Link
              href="/dashboard"
              className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/dashboard')
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800'
                }`}
            >
              <Home className="h-5 w-5" />
              <span className="text-sm font-medium">Tableau de bord</span>
            </Link>
          </li>

          {role === 'super_admin' && (
            <li>
              <Link
                href="/tenants"
                className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/tenants')
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                    : 'text-gray-400 hover:text-white hover:bg-gray-800'
                  }`}
              >
                <Building2 className="h-5 w-5" />
                <span className="text-sm font-medium">Organisations</span>
              </Link>
            </li>
          )}

          {role === 'tenant_admin' && (
            <>
              <li>
                <Link
                  href="/departments"
                  className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/departments')
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800'
                    }`}
                >
                  <Building2 className="h-5 w-5" />
                  <span className="text-sm font-medium">Départements</span>
                </Link>
              </li>

              <li>
                <Link
                  href="/users"
                  className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/users')
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800'
                    }`}
                >
                  <Users className="h-5 w-5" />
                  <span className="text-sm font-medium">Utilisateurs</span>
                </Link>
              </li>

              <li>
                <Link
                  href="/roles"
                  className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/roles')
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                      : 'text-gray-400 hover:text-white hover:bg-gray-800'
                    }`}
                >
                  <Shield className="h-5 w-5" />
                  <span className="text-sm font-medium">Rôles</span>
                </Link>
              </li>
            </>
          )}

          <li>
            <Link
              href="/audit-logs"
              className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/audit-logs')
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800'
                }`}
            >
              <Activity className="h-5 w-5" />
              <span className="text-sm font-medium">Audit Logs</span>
            </Link>
          </li>

          <li>
            <Link
              href="/settings"
              className={`flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 ${isActive('/settings')
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800'
                }`}
            >
              <Settings className="h-5 w-5" />
              <span className="text-sm font-medium">Paramètres</span>
            </Link>
          </li>
        </ul>
      </nav>

      <div className="border-t border-gray-700 pt-4">
        <button
          onClick={() => {
            localStorage.removeItem('userRole');
            window.location.href = '/login';
          }}
          className="flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-gray-800 w-full text-left text-red-400 hover:text-red-300"
        >
          <LogOut className="h-5 w-5" />
          Déconnexion
        </button>
      </div>
    </div>
  );
}
