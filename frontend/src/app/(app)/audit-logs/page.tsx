'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Activity, Loader2, AlertCircle, Search, Filter } from 'lucide-react';
import { api } from '@/services/api';
import { Input } from '@/components/ui/input';

interface AuditLog {
    id: string;
    action: string;
    timestamp: string;
    username: string;
    resourceType: string;
    outcome: string;
    message: string;
}

export default function AuditLogsPage() {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchLogs();
    }, []);

    async function fetchLogs() {
        try {
            setLoading(true);
            setError(null);
            const response = await api.get<AuditLog[]>('/api/audit-logs');
            setLogs(response.data);
        } catch (err: any) {
            console.error('Erreur chargement logs:', err);
            setError(err.message || 'Erreur lors du chargement des logs');
        } finally {
            setLoading(false);
        }
    }

    const filteredLogs = logs.filter(log =>
        log.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.message.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Journaux d'Audit</h1>
                <p className="text-gray-600 mt-2">
                    Suivez toutes les activités et modifications de sécurité
                </p>
            </div>

            <div className="flex gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                    <Input
                        placeholder="Rechercher une action, un utilisateur..."
                        className="pl-10"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <Button variant="outline" onClick={fetchLogs}>
                    Actualiser
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-3 text-xl">
                        <Activity className="h-6 w-6 text-blue-600" />
                        Activités récentes
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="flex items-center justify-center py-12">
                            <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
                            <span className="ml-2">Chargement des logs...</span>
                        </div>
                    ) : error ? (
                        <div className="text-center py-12 text-red-600 bg-red-50 rounded-lg">
                            <AlertCircle className="h-10 w-10 mx-auto mb-2" />
                            <p>{error}</p>
                        </div>
                    ) : filteredLogs.length === 0 ? (
                        <div className="text-center py-12 text-gray-500">
                            <p>Aucun log trouvé</p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Date</TableHead>
                                    <TableHead>Utilisateur</TableHead>
                                    <TableHead>Action</TableHead>
                                    <TableHead>Ressource</TableHead>
                                    <TableHead>Résultat</TableHead>
                                    <TableHead>Détails</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {filteredLogs.map((log) => (
                                    <TableRow key={log.id}>
                                        <TableCell className="whitespace-nowrap font-mono text-xs">
                                            {new Date(log.timestamp).toLocaleString('fr-FR')}
                                        </TableCell>
                                        <TableCell className="font-medium">{log.username}</TableCell>
                                        <TableCell>
                                            <span className="px-2 py-1 rounded-md bg-gray-100 text-xs font-semibold">
                                                {log.action}
                                            </span>
                                        </TableCell>
                                        <TableCell>{log.resourceType}</TableCell>
                                        <TableCell>
                                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${log.outcome === 'SUCCESS' ? 'bg-green-100 text-green-700' :
                                                    log.outcome === 'FAILURE' ? 'bg-red-100 text-red-700' :
                                                        'bg-gray-100 text-gray-700'
                                                }`}>
                                                {log.outcome}
                                            </span>
                                        </TableCell>
                                        <TableCell className="max-w-xs truncate text-gray-600 text-sm">
                                            {log.message}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}

import { Button } from '@/components/ui/button';
