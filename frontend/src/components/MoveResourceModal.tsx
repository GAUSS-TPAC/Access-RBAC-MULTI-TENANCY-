'use client';

import { useState, useEffect } from 'react';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { api } from '@/services/api';
import { Loader2, ArrowRightLeft } from 'lucide-react';

interface Resource {
    id: string;
    name: string;
    type: string;
    level: number;
}

interface MoveResourceModalProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    resourceToMove: { id: string; name: string } | null;
    onMoved?: () => void;
}

function flattenResources(resources: any[], level = 0): Resource[] {
    const result: Resource[] = [];
    for (const resource of resources) {
        result.push({ id: resource.id, name: resource.name, level, type: resource.type });
        if (resource.children?.length > 0) {
            result.push(...flattenResources(resource.children, level + 1));
        }
    }
    return result;
}

export default function MoveResourceModal({
    open,
    onOpenChange,
    resourceToMove,
    onMoved,
}: MoveResourceModalProps) {
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [resources, setResources] = useState<Resource[]>([]);
    const [selectedParentId, setSelectedParentId] = useState<string>('');
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (open && resourceToMove) {
            fetchResources();
            setSelectedParentId('');
            setError(null);
        }
    }, [open, resourceToMove]);

    async function fetchResources() {
        try {
            setLoading(true);
            const tenantsRes = await api.get<any[]>('/api/tenants');
            if (tenantsRes.data.length > 0) {
                const tenantId = tenantsRes.data[0].id;
                const res = await api.get<any[]>(`/api/resources/tenant/${tenantId}`);
                const list = flattenResources(res.data);
                // On ne peut pas déplacer une ressource vers elle-même ou ses enfants (le backend checkera aussi)
                setResources(list.filter(r => r.id !== resourceToMove?.id));
            }
        } catch (err) {
            console.error('Error fetching resources:', err);
        } finally {
            setLoading(false);
        }
    }

    const handleMove = async () => {
        if (!resourceToMove || !selectedParentId) return;

        try {
            setSubmitting(true);
            setError(null);
            await api.patch(`/api/resources/${resourceToMove.id}/move`, {
                newParentId: selectedParentId
            });
            onOpenChange(false);
            if (onMoved) onMoved();
            else window.location.reload();
        } catch (err: any) {
            console.error('Error moving resource:', err);
            setError(err.message || 'Erreur lors du déplacement');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                        <ArrowRightLeft className="h-5 w-5 text-blue-600" />
                        Déplacer la ressource
                    </DialogTitle>
                </DialogHeader>

                <div className="py-6 space-y-4">
                    <div className="p-3 bg-blue-50 rounded-lg border border-blue-100 italic text-sm text-blue-800">
                        Vous déplacez <strong>{resourceToMove?.name}</strong> et toute sa sous-hiérarchie.
                    </div>

                    {error && (
                        <div className="p-3 bg-red-50 text-red-600 rounded-md text-sm">
                            {error}
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label>Sélectionner le nouveau parent</Label>
                        {loading ? (
                            <div className="flex items-center gap-2 text-gray-500 py-2">
                                <Loader2 className="h-4 w-4 animate-spin" />
                                Chargement des destinations...
                            </div>
                        ) : (
                            <Select value={selectedParentId} onValueChange={setSelectedParentId}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Choisir une destination..." />
                                </SelectTrigger>
                                <SelectContent>
                                    {resources.map((res) => (
                                        <SelectItem key={res.id} value={res.id}>
                                            <span className="opacity-50 mr-2">{'—'.repeat(res.level)}</span>
                                            {res.type === 'ROOT' ? '🏢 ' : ''}{res.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        )}
                    </div>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
                        Annuler
                    </Button>
                    <Button onClick={handleMove} disabled={submitting || !selectedParentId}>
                        {submitting ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                                Déplacement...
                            </>
                        ) : (
                            'Confirmer le déplacement'
                        )}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
