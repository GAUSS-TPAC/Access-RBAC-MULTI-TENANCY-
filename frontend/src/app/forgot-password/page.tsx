'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Mail, Loader2, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { api } from '@/services/api';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            await api.post('/api/auth/forgot-password', { email });
            setSuccess(true);
        } catch (err: any) {
            console.error('Forgot password error:', err);
            setError(err.message || "Une erreur est survenue");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="max-w-md w-full space-y-8">
                <div className="text-center">
                    <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Récupération</h1>
                    <p className="mt-2 text-slate-600">Retrouvez l'accès à votre compte YowAccess</p>
                </div>

                <Card className="border-none shadow-xl">
                    <CardHeader>
                        <CardTitle className="text-xl">Mot de passe oublié ?</CardTitle>
                        <CardDescription>
                            Saisissez votre email pour recevoir un lien de réinitialisation.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {success ? (
                            <div className="text-center space-y-4 py-4">
                                <div className="mx-auto h-12 w-12 bg-green-100 rounded-full flex items-center justify-center">
                                    <CheckCircle2 className="h-8 w-8 text-green-600" />
                                </div>
                                <h3 className="text-lg font-semibold text-green-800">Email envoyé !</h3>
                                <p className="text-sm text-slate-600">
                                    Si un compte existe pour <strong>{email}</strong>, un lien a été envoyé.
                                    Vérifiez vos spams si besoin.
                                </p>
                                <Button variant="outline" className="w-full" asChild>
                                    <Link href="/login">Retour à la connexion</Link>
                                </Button>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="space-y-4">
                                {error && (
                                    <div className="p-3 bg-red-50 text-red-600 rounded-md text-sm">
                                        {error}
                                    </div>
                                )}
                                <div className="space-y-2">
                                    <Label htmlFor="email">Email professionnel</Label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                        <Input
                                            id="email"
                                            type="email"
                                            placeholder="nom@entreprise.com"
                                            className="pl-10"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            required
                                        />
                                    </div>
                                </div>
                                <Button className="w-full h-11 bg-blue-600 hover:bg-blue-700 font-semibold" disabled={isLoading}>
                                    {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : 'Envoyer le lien'}
                                </Button>
                                <Button variant="ghost" className="w-full text-slate-600" asChild>
                                    <Link href="/login" className="flex items-center gap-2">
                                        <ArrowLeft className="h-4 w-4" /> Retour à la connexion
                                    </Link>
                                </Button>
                            </form>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
