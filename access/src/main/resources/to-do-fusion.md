🎯 OBJECTIF DE LA FUSION

Mettre en place un flux complet réel :

UI → API → RBAC → DB → Audit
sans casser l’architecture existante.

👉 Pas de logique métier côté front.
👉 Le front consomme, observe, révèle les bugs.

🧭 ROADMAP FUSION FRONT / BACK (OPTIMISÉE)
PHASE 0 — CADRAGE (OBLIGATOIRE, RAPIDE)

⏱ 30–45 min

Front = client (React / Vue / Blade / autre)

Backend = source of truth

Auth = JWT (temporaire OK)

RBAC = 100% backend

👉 Décision ferme :
le front n’implémente aucune règle de permission.

PHASE 1 — CONTRAT API (CLÉ DE L’EFFICACITÉ)

⏱ 1–2 h

1.1 Lister les endpoints MINIMUMS

(ne pas tout exposer)

Tenant

POST /api/tenants

GET /api/tenants

GET /api/tenants/{id}

Resource

POST /api/resources/{parentId}

GET /api/resources/{rootId}/tree

User

POST /api/users

GET /api/users

Role Assignment

POST /api/roles/assign

1.2 Définir les DTOs de réponse

Pas les entités.

✔ plats
✔ stables
✔ versionnables

👉 Si un DTO change = breaking change assumé.

PHASE 2 — AUTH SIMPLE MAIS PROPRE

⏱ 1–2 h

Objectif : auth fonctionnelle, pas parfaite

Login → JWT

JWT → AuthenticatedUserContext

Pas de refresh token pour l’instant

Pas d’OAuth

🎯 But : identifier userId côté backend.

PHASE 3 — FRONT SQUELETTE (PAS DE DESIGN)

⏱ 2–3 h

3.1 Structure minimale

/login

/tenants

/tenants/:id/resources

Aucun style lourd.
Lisibilité > esthétique.

3.2 Service HTTP unique

Exemple conceptuel :

api.ts

gestion automatique du token

gestion centralisée des erreurs (401 / 403)

👉 Interdit : appels API dispersés.

PHASE 4 — FLUX COMPLET 1 (CRITIQUE)

⏱ 1–2 h

🎯 Flux : CRÉATION TENANT

Front → POST /tenants

Backend :

check permission globale

create tenant

create root resource

assign ADMIN

audit log

Front :

redirection /tenants

affichage liste

✔ Si ce flux marche → backend viable

PHASE 5 — FLUX COMPLET 2 (RBAC PUR)

⏱ 1–2 h

🎯 Flux : ARBRE DE RESSOURCES

Front → GET /resources/{rootId}/tree

Backend :

check RESOURCE_READ

build tree

Front :

affichage hiérarchique simple

👉 Test visuel immédiat des permissions.

PHASE 6 — GESTION DES ERREURS (SOUVENT OUBLIÉ)

⏱ 1 h

Le front doit afficher :

401 → session expirée

403 → accès interdit

400 → erreur utilisateur

500 → erreur système

🎯 Un RBAC sans bons messages est inutilisable.

PHASE 7 — VERROUILLAGE AVANT TESTS

⏱ 30 min

Checklist :

aucun calcul de droit côté front

aucun contournement possible via UI

tout passe par AuthorizationService

audit log écrit même en cas d’échec

👉 Maintenant seulement → TESTS AUTOMATISÉS.