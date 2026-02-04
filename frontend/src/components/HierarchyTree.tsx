// src/components/HierarchyTree.tsx
'use client';

import { useState, useEffect } from 'react';
import { Building2, Users, Plus, Loader2, AlertCircle, Trash2, TreePine } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { api } from '@/services/api';
import MoveResourceModal from './MoveResourceModal';

// Interface pour les ressources du backend
interface ResourceTree {
  id: string;
  name: string;
  type: string;
  children: ResourceTree[];
}

// Interface pour les tenants
interface Tenant {
  id: string;
  name: string;
  code: string;
}

// Interface pour l'affichage (compatible avec l'ancienne structure)
export interface Department {
  id: string;
  name: string;
  description: string;
  managerName: string;
  children: Department[];
}

interface HierarchyTreeProps {
  onCreateSubDepartment: (parent: Department) => void;
}

// Convertir ResourceTree en Department pour l'affichage
function resourceToDepartment(resource: ResourceTree): Department {
  return {
    id: resource.id,
    name: resource.name,
    description: resource.type || 'Ressource',
    managerName: '-',
    children: resource.children?.map(resourceToDepartment) || [],
  };
}

function DepartmentNode({
  dept,
  level = 0,
  onCreateSubDepartment,
  onMove
}: {
  dept: Department;
  level?: number;
  onCreateSubDepartment: (parent: Department) => void;
  onMove: (dept: Department) => void;
}) {
  return (
    <div className={`${level > 0 ? 'ml-8 border-l-2 border-gray-200 pl-6' : ''}`}>
      <div className="flex items-center justify-between py-4 px-6 bg-white rounded-lg shadow-sm hover:shadow-md transition">
        <div className="flex items-center gap-4">
          <Building2 className="h-8 w-8 text-blue-600" />
          <div>
            <h3 className="text-lg font-semibold">{dept.name}</h3>
            <p className="text-sm text-gray-600">{dept.description}</p>
            {dept.managerName !== '-' && (
              <p className="text-sm text-gray-500 mt-1 flex items-center gap-2">
                <Users className="h-4 w-4" />
                Responsable : <span className="font-medium">{dept.managerName}</span>
              </p>
            )}
          </div>
        </div>
        <div className="flex gap-2">
          <Button
            size="sm"
            variant="ghost"
            className="text-gray-500 hover:text-blue-600"
            title="Déplacer cette ressource"
            onClick={() => onMove(dept)}
          >
            <TreePine className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-gray-500 hover:text-red-600"
            onClick={async () => {
              if (confirm(`Êtes-vous sûr de vouloir supprimer ${dept.name} ?`)) {
                try {
                  await api.delete(`/api/resources/${dept.id}`);
                  window.location.reload();
                } catch (err: any) {
                  alert(err.message || "Erreur lors de la suppression");
                }
              }
            }}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={() => onCreateSubDepartment(dept)}
          >
            <Plus className="h-4 w-4 mr-2" />
            Sous-département
          </Button>
        </div>
      </div>

      {dept.children.length > 0 && (
        <div className="mt-4">
          {dept.children.map((child) => (
            <DepartmentNode
              key={child.id}
              dept={child}
              level={level + 1}
              onCreateSubDepartment={onCreateSubDepartment}
              onMove={onMove}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function HierarchyTree({ onCreateSubDepartment }: HierarchyTreeProps) {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [moveModalOpen, setMoveModalOpen] = useState(false);
  const [resourceToMove, setResourceToMove] = useState<{ id: string; name: string } | null>(null);

  const handleMoveClick = (dept: Department) => {
    setResourceToMove({ id: dept.id, name: dept.name });
    setMoveModalOpen(true);
  };

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);

        // 1. Récupérer la liste des tenants accessibles
        const tenantsResponse = await api.get<Tenant[]>('/api/tenants');
        const tenants = tenantsResponse.data;

        if (tenants.length === 0) {
          setDepartments([]);
          return;
        }

        // 2. Pour le premier tenant, récupérer l'arbre des ressources
        const tenantId = tenants[0].id;
        const resourcesResponse = await api.get<ResourceTree[]>(`/api/resources/tenant/${tenantId}`);
        const resources = resourcesResponse.data;

        // 3. Convertir les ressources en départements
        const depts = resources.map(resourceToDepartment);
        setDepartments(depts);

      } catch (err: any) {
        console.error('Erreur lors du chargement:', err);
        setError(err.message || 'Erreur lors du chargement des données');
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        <span className="ml-2 text-gray-600">Chargement...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
        <p className="text-xl text-red-600">{error}</p>
      </div>
    );
  }

  if (departments.length === 0) {
    return (
      <div className="text-center py-12">
        <Building2 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <p className="text-xl text-gray-600">Aucun département créé</p>
        <p className="text-sm text-gray-500 mt-2">
          Créez votre premier département pour commencer
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {departments.map((dept) => (
        <DepartmentNode
          key={dept.id}
          dept={dept}
          onCreateSubDepartment={onCreateSubDepartment}
          onMove={handleMoveClick}
        />
      ))}

      <MoveResourceModal
        open={moveModalOpen}
        onOpenChange={setMoveModalOpen}
        resourceToMove={resourceToMove}
      />
    </div>
  );
}
