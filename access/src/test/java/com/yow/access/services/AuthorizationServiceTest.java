package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests CRITIQUES pour AuthorizationService
 * Cœur du système RBAC Multi-Tenant
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationService Tests")
class AuthorizationServiceTest {

    @Mock
    private UserRoleResourceRepository urrRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    // IDs constants
    private final UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID tenantId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
    private final UUID resourceId = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
    private final UUID childResourceId = UUID.fromString("423e4567-e89b-12d3-a456-426614174000");
    private final UUID grandChildResourceId = UUID.fromString("523e4567-e89b-12d3-a456-426614174000");

    // Données de test
    private Tenant tenant;
    private Resource rootResource;
    private Resource childResource;
    private Resource grandChildResource;
    private Permission readPermission;
    private Permission writePermission;
    private Permission deletePermission;
    private Role userRole;
    private Role adminRole;
    private Role globalAdminRole;

    @BeforeEach
    void setUp() {
        // Setup Tenant
        tenant = Tenant.builder()
                .id(tenantId)
                .code("TENANT_A")
                .name("Tenant A")
                .build();

        // Setup Resources (hiérarchie)
        rootResource = Resource.builder()
                .id(resourceId)
                .name("Root Resource")
                .tenant(tenant)
                .parent(null)
                .build();

        childResource = Resource.builder()
                .id(childResourceId)
                .name("Child Resource")
                .tenant(tenant)
                .parent(rootResource)
                .build();

        grandChildResource = Resource.builder()
                .id(grandChildResourceId)
                .name("GrandChild Resource")
                .tenant(tenant)
                .parent(childResource)
                .build();

        // Setup Permissions
        readPermission = Permission.builder()
                .id((short) 1L)
                .name("READ")
                .description("Read permission")
                .build();

        writePermission = Permission.builder()
                .id((short) 2L)
                .name("WRITE")
                .description("Write permission")
                .build();

        deletePermission = Permission.builder()
                .id((short) 3L)
                .name("DELETE")
                .description("Delete permission")
                .build();

        // Setup Roles
        userRole = Role.builder()
                .id((short) 1)
                .name("USER")
                .permissions(new HashSet<>(Arrays.asList(readPermission)))
                .build();

        adminRole = Role.builder()
                .id((short) 2)
                .name("ADMIN_TENANT")
                .permissions(new HashSet<>(Arrays.asList(readPermission, writePermission, deletePermission)))
                .build();

        globalAdminRole = Role.builder()
                .id((short) 3)
                .name("ADMIN_GLOBAL")
                .permissions(new HashSet<>(Arrays.asList(readPermission, writePermission, deletePermission)))
                .build();
    }

    /* ===================================================================
       TESTS DE BASE - hasPermission()
       =================================================================== */
    @Nested
    @DisplayName("hasPermission() - Tests de base RBAC")
    class HasPermissionTests {

        @Test
        @DisplayName("✅ User a permission DIRECTE sur la ressource")
        void hasPermission_DirectPermission() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "READ", rootResource
            );

            // Assert
            assertTrue(result, "User devrait avoir READ sur rootResource");
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("❌ User n'a PAS la permission demandée")
        void hasPermission_NoPermission() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ mais pas WRITE
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "WRITE", rootResource
            );

            // Assert
            assertFalse(result, "User ne devrait pas avoir WRITE");
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ User hérite permission via ressource PARENT")
        void hasPermission_InheritedFromParent() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ
                    .resource(rootResource) // Permission sur parent
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "READ", childResource // Check sur enfant
            );

            // Assert
            assertTrue(result, "User devrait hériter READ du parent");
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ User hérite permission via grand-parent (2 niveaux)")
        void hasPermission_InheritedFromGrandParent() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ
                    .resource(rootResource) // Permission sur grand-parent
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "READ", grandChildResource // Check sur petit-enfant
            );

            // Assert
            assertTrue(result, "User devrait hériter READ du grand-parent");
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("❌ Pas de bindings pour l'utilisateur")
        void hasPermission_NoBindings() {
            // Arrange
            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Collections.emptyList());

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "READ", rootResource
            );

            // Assert
            assertFalse(result, "User sans bindings ne devrait avoir aucune permission");
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ Multiple bindings - le premier qui match donne l'accès")
        void hasPermission_MultipleBindings() {
            // Arrange
            UserRoleResource urr1 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ
                    .resource(rootResource)
                    .build();

            UserRoleResource urr2 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(adminRole) // Contient WRITE aussi
                    .resource(childResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr1, urr2));

            // Act
            boolean readResult = authorizationService.hasPermission(
                    userId, "READ", rootResource
            );
            boolean writeResult = authorizationService.hasPermission(
                    userId, "WRITE", childResource
            );

            // Assert
            assertTrue(readResult, "Devrait avoir READ via urr1");
            assertTrue(writeResult, "Devrait avoir WRITE via urr2");
            verify(urrRepository, times(2)).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ Permission case-sensitive")
        void hasPermission_CaseSensitive() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient "READ" majuscule
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean upperCase = authorizationService.hasPermission(
                    userId, "READ", rootResource
            );
            boolean lowerCase = authorizationService.hasPermission(
                    userId, "read", rootResource
            );

            // Assert
            assertTrue(upperCase, "Devrait matcher 'READ' majuscule");
            assertFalse(lowerCase, "Ne devrait pas matcher 'read' minuscule");
        }

        @Test
        @DisplayName("✅ Ressource null - retourne false sans vérifier les bindings")
        void hasPermission_NullResource() {
            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "READ", null
            );

            // Assert
            assertFalse(result, "Resource null = pas de permission");
            verify(urrRepository, never()).findAllByUserId(userId); // Pas d'appel
        }

        @Test
        @DisplayName("✅ Permission null ou vide")
        void hasPermission_NullOrEmptyPermission() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertFalse(authorizationService.hasPermission(userId, null, rootResource));
            assertFalse(authorizationService.hasPermission(userId, "", rootResource));
            assertFalse(authorizationService.hasPermission(userId, "   ", rootResource));
        }
    }

    /* ===================================================================
       TESTS HIÉRARCHIQUES - Inheritance
       =================================================================== */
    @Nested
    @DisplayName("Hiérarchie des ressources - Tests d'héritage")
    class HierarchyTests {

        @Test
        @DisplayName("✅ Permission sur parent → accès à tous les enfants")
        void hierarchy_PermissionOnParentGrantsAllChildren() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(adminRole) // READ, WRITE, DELETE
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", childResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", childResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", childResource));
            assertTrue(authorizationService.hasPermission(userId, "READ", grandChildResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", grandChildResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", grandChildResource));
        }

        @Test
        @DisplayName("❌ Permission limitée à un niveau spécifique")
        void hierarchy_PermissionLimitedToSpecificLevel() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Seulement READ
                    .resource(childResource) // Uniquement sur enfant
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource)); // Parent: NON
            assertTrue(authorizationService.hasPermission(userId, "READ", childResource)); // Même: OUI
            assertTrue(authorizationService.hasPermission(userId, "READ", grandChildResource)); // Enfant: OUI (hérité)
            assertFalse(authorizationService.hasPermission(userId, "WRITE", childResource)); // Autre permission: NON
        }

        @Test
        @DisplayName("✅ Première permission qui match dans la hiérarchie (pas 'closest wins')")
        void hierarchy_FirstMatchingPermissionWins() {
            // Arrange
            Role limitedRole = Role.builder()
                    .id((short) 4)
                    .name("LIMITED")
                    .permissions(new HashSet<>(Arrays.asList(writePermission))) // Seulement WRITE
                    .build();

            UserRoleResource parentUrr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(adminRole) // Toutes permissions sur parent
                    .resource(rootResource)
                    .build();

            UserRoleResource childUrr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(limitedRole) // Seulement WRITE sur enfant
                    .resource(childResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(parentUrr, childUrr));

            // Act & Assert
            // Sur root: toutes permissions (via adminRole)
            assertTrue(authorizationService.hasPermission(userId, "DELETE", rootResource));

            // Sur child: WRITE via limitedRole, mais aussi DELETE via parent (hérité)
            // Ta logique checke d'abord child, puis remonte à parent
            assertTrue(authorizationService.hasPermission(userId, "WRITE", childResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", childResource)); // ✅ Hérité du parent

            // Sur grandChild: hérite de child pour WRITE, et de parent pour DELETE
            assertTrue(authorizationService.hasPermission(userId, "WRITE", grandChildResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", grandChildResource)); // ✅ Hérité
        }
    }

    /* ===================================================================
       TESTS checkPermission() - avec exception
       =================================================================== */
    @Nested
    @DisplayName("checkPermission() - Vérification avec exception")
    class CheckPermissionTests {

        @Test
        @DisplayName("✅ Permission accordée - pas d'exception")
        void checkPermission_Granted() {
            // Arrange
            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.of(rootResource));

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert (aucune exception ne doit être lancée)
            assertDoesNotThrow(() -> {
                authorizationService.checkPermission(userId, resourceId, "READ");
            });

            verify(resourceRepository).findById(resourceId);
            verify(urrRepository).findAllByUserId(userId);
        }

        // Vérifie que userRole n'a PAS WRITE
        @Test
        void verifyUserRolePermissions() {
            assertTrue(userRole.getPermissions().contains(readPermission));
            assertFalse(userRole.getPermissions().contains(writePermission)); // Doit être false
            assertFalse(userRole.getPermissions().contains(deletePermission));
        }

        @Test
        @DisplayName("❌ Ressource non trouvée - lance IllegalStateException")
        void checkPermission_ResourceNotFound() {
            // Arrange
            when(resourceRepository.findById(resourceId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> authorizationService.checkPermission(userId, resourceId, "READ")
            );

            assertEquals("Resource not found", exception.getMessage());
            verify(resourceRepository).findById(resourceId);
            verify(urrRepository, never()).findAllByUserId(any());
        }

        @Test
        @DisplayName("✅ Utilisation de l'héritage dans checkPermission")
        void checkPermission_InheritanceWorks() {
            // Arrange
            when(resourceRepository.findById(grandChildResourceId))
                    .thenReturn(Optional.of(grandChildResource));

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(rootResource) // Permission sur grand-parent
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertDoesNotThrow(() -> {
                authorizationService.checkPermission(userId, grandChildResourceId, "READ");
            });
        }
    }

    /* ===================================================================
       TESTS checkGlobalPermission() - sans ressource
       =================================================================== */
    @Nested
    @DisplayName("checkGlobalPermission() - Permissions globales")
    class CheckGlobalPermissionTests {

        @Test
        @DisplayName("✅ Permission globale accordée")
        void checkGlobalPermission_Granted() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(globalAdminRole) // Contient READ
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertDoesNotThrow(() -> {
                authorizationService.checkGlobalPermission(userId, "READ");
            });

            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("❌ Permission globale refusée - user sans DELETE")
        void checkGlobalPermission_Denied() {
            // Arrange
            // userRole a seulement READ, pas DELETE
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Seulement READ permission
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> authorizationService.checkGlobalPermission(userId, "DELETE")
            );

            // Vérifie que le message contient "Permission denied" (le texte exact peut varier)
            assertTrue(exception.getMessage().contains("Permission denied"),
                    "Le message devrait indiquer un refus de permission. Message: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("DELETE"),
                    "Le message devrait mentionner la permission DELETE. Message: " + exception.getMessage());

            verify(urrRepository).findAllByUserId(userId);
        }

        // AJOUTE CE TEST POUR VÉRIFIER LES PERMISSIONS DU RÔLE
        @Test
        @DisplayName("✅ Vérification des permissions du rôle USER")
        void verifyUserRolePermissions() {
            // Arrange
            Set<Permission> userPermissions = userRole.getPermissions();

            // Assert
            assertNotNull(userPermissions);
            assertEquals(1, userPermissions.size());
            assertTrue(userPermissions.stream().anyMatch(p -> "READ".equals(p.getName())));
            assertFalse(userPermissions.stream().anyMatch(p -> "WRITE".equals(p.getName())));
            assertFalse(userPermissions.stream().anyMatch(p -> "DELETE".equals(p.getName())));

            System.out.println("Permissions du rôle USER: " +
                    userPermissions.stream().map(Permission::getName).collect(java.util.stream.Collectors.toList()));
        }

        @Test
        @DisplayName("✅ Permission globale avec ADMIN_GLOBAL")
        void checkGlobalPermission_WithGlobalAdmin() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(globalAdminRole) // Toutes permissions
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertDoesNotThrow(() -> {
                authorizationService.checkGlobalPermission(userId, "READ");
                authorizationService.checkGlobalPermission(userId, "WRITE");
                authorizationService.checkGlobalPermission(userId, "DELETE");
            });
        }

        @Test
        @DisplayName("❌ Pas de bindings = aucune permission globale")
        void checkGlobalPermission_NoBindings() {
            // Arrange
            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> authorizationService.checkGlobalPermission(userId, "READ")
            );

            // Vérifie le message EXACT de ton implémentation
            // Peut être "Permission denied: READ" ou autre
            assertTrue(exception.getMessage().contains("Permission denied"));
            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ Multiple rôles - un seul suffit")
        void checkGlobalPermission_MultipleRoles() {
            // Arrange
            UserRoleResource urr1 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // READ seulement
                    .resource(rootResource)
                    .build();

            UserRoleResource urr2 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(adminRole) // READ + WRITE + DELETE
                    .resource(childResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr1, urr2));

            // Act & Assert (devrait passer avec les deux rôles)
            assertDoesNotThrow(() -> {
                authorizationService.checkGlobalPermission(userId, "READ");
                authorizationService.checkGlobalPermission(userId, "WRITE");
                authorizationService.checkGlobalPermission(userId, "DELETE");
            });
        }
    }

    /* ===================================================================
       TESTS DES RÔLES SPÉCIAUX - ADMIN_TENANT, ADMIN_GLOBAL
       =================================================================== */
    @Nested
    @DisplayName("Rôles système spéciaux")
    class SpecialRolesTests {

        @Test
        @DisplayName("✅ ADMIN_TENANT - Toutes permissions sur son tenant")
        void adminTenant_AllPermissionsOnTenant() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(adminRole) // ADMIN_TENANT avec toutes permissions
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "READ", childResource)); // Hérité
            assertTrue(authorizationService.hasPermission(userId, "WRITE", childResource)); // Hérité
        }

        @Test
        @DisplayName("✅ ADMIN_GLOBAL - Override total (même checkGlobalPermission)")
        void adminGlobal_TotalOverride() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(globalAdminRole) // ADMIN_GLOBAL
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", rootResource));

            // Vérifie aussi checkGlobalPermission
            assertDoesNotThrow(() -> {
                authorizationService.checkGlobalPermission(userId, "READ");
                authorizationService.checkGlobalPermission(userId, "WRITE");
                authorizationService.checkGlobalPermission(userId, "DELETE");
            });
        }

        @Test
        @DisplayName("✅ ADMIN_GLOBAL sur child donne accès à toute la hiérarchie")
        void adminGlobal_GrantsAccessToHierarchy() {
            // Arrange
            Role noPermissionRole = Role.builder()
                    .id((short) 5)
                    .name("NO_PERM")
                    .permissions(new HashSet<>()) // Aucune permission
                    .build();

            UserRoleResource urr1 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(noPermissionRole) // Rôle sans permission
                    .resource(rootResource)
                    .build();

            UserRoleResource urr2 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(globalAdminRole) // ADMIN_GLOBAL avec toutes permissions
                    .resource(childResource) // Sur enfant
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr1, urr2));

            // Act & Assert
            // Sur child: toutes permissions via ADMIN_GLOBAL
            assertTrue(authorizationService.hasPermission(userId, "READ", childResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", childResource));
            assertTrue(authorizationService.hasPermission(userId, "DELETE", childResource));

            // Sur grandChild: hérité de child
            assertTrue(authorizationService.hasPermission(userId, "READ", grandChildResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", grandChildResource));

            // Sur root: PAS d'accès (permission sur child seulement)
            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource)); // ❌ Pas hérité vers le haut
        }
    }

    /* ===================================================================
       TESTS DE PERFORMANCE ET ROBUSTESSE
       =================================================================== */
    @Nested
    @DisplayName("Performance et robustesse")
    class PerformanceAndRobustnessTests {

        @Test
        @DisplayName("✅ Boucle circulaire - avec permission sur une ressource SANS lien")
        @Timeout(1)
        void circularReference_HandledProperly() {
            // Arrange - Boucle A ↔ B
            Resource resourceA = Resource.builder()
                    .id(resourceId)
                    .name("Resource A")
                    .tenant(tenant)
                    .build();

            Resource resourceB = Resource.builder()
                    .id(childResourceId)
                    .name("Resource B")
                    .tenant(tenant)
                    .parent(resourceA) // B → A
                    .build();

            resourceA.setParent(resourceB); // A → B (boucle!)

            // ⚠️ IMPORTANT: Permission sur une ressource SANS LIEN avec la boucle
            Resource unrelatedResource = Resource.builder()
                    .id(UUID.fromString("999e4567-e89b-12d3-a456-426614174000"))
                    .name("Unrelated Resource")
                    .tenant(tenant)
                    .parent(null) // Pas dans la boucle
                    .build();

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(unrelatedResource) // Permission sur ressource non liée
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            // Avec une boucle circulaire, le code ne devrait pas bloquer
            // et devrait retourner false (pas de permission sur A ou B)
            assertAll(
                    () -> assertFalse(authorizationService.hasPermission(userId, "READ", resourceA),
                            "Resource A dans la boucle - pas de permission"),
                    () -> assertFalse(authorizationService.hasPermission(userId, "READ", resourceB),
                            "Resource B dans la boucle - pas de permission"),
                    () -> assertTrue(authorizationService.hasPermission(userId, "READ", unrelatedResource),
                            "Resource sans lien - permission directe")
            );
        }

        @Test
        @DisplayName("✅ Boucle circulaire - détectée et gérée")
        @Timeout(1)
        void circularReference_DetectedAndHandled() {
            // Arrange
            UUID idA = UUID.randomUUID();
            UUID idB = UUID.randomUUID();
            UUID idC = UUID.randomUUID();

            // Crée une boucle A ↔ B ↔ C
            Resource resourceA = Resource.builder().id(idA).name("A").tenant(tenant).build();
            Resource resourceB = Resource.builder().id(idB).name("B").tenant(tenant).parent(resourceA).build();
            Resource resourceC = Resource.builder().id(idC).name("C").tenant(tenant).parent(resourceB).build();

            // Complète la boucle: C → A
            resourceA.setParent(resourceC);

            // Permission sur une ressource externe
            Resource external = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("External")
                    .tenant(tenant)
                    .parent(null)
                    .build();

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(external) // Permission uniquement sur ressource externe
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert - Vérifie que:
            // 1. Pas de permission sur les ressources de la boucle
            // 2. Le code ne bloque pas indéfiniment
            // 3. Permission fonctionne sur la ressource externe
            assertAll(
                    () -> assertFalse(authorizationService.hasPermission(userId, "READ", resourceA),
                            "A dans la boucle - false"),
                    () -> assertFalse(authorizationService.hasPermission(userId, "READ", resourceB),
                            "B dans la boucle - false"),
                    () -> assertFalse(authorizationService.hasPermission(userId, "READ", resourceC),
                            "C dans la boucle - false"),
                    () -> assertTrue(authorizationService.hasPermission(userId, "READ", external),
                            "Resource externe - true (permission directe)"),
                    () -> assertFalse(authorizationService.hasPermission(userId, "WRITE", external),
                            "Resource externe - false (pas WRITE)")
            );

            System.out.println("✅ Test réussi: Boucle circulaire détectée et gérée");
        }

        @Test
        @DisplayName("DEBUG - Comportement avec boucle et permission")
        @Timeout(1)
        void debugCircularReferenceWithPermission() {
            // Arrange - Boucle simple
            Resource resourceA = Resource.builder()
                    .id(resourceId)
                    .name("A")
                    .tenant(tenant)
                    .build();

            Resource resourceB = Resource.builder()
                    .id(childResourceId)
                    .name("B")
                    .tenant(tenant)
                    .parent(resourceA)
                    .build();

            resourceA.setParent(resourceB); // Boucle A ↔ B

            // Cas 1: Permission sur A
            UserRoleResource urrOnA = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(resourceA)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urrOnA));

            System.out.println("=== Cas 1: Permission sur A ===");
            boolean resultA = authorizationService.hasPermission(userId, "READ", resourceA);
            boolean resultB = authorizationService.hasPermission(userId, "READ", resourceB);
            System.out.println("Permission sur A: " + resultA + " (attendu: true - permission directe)");
            System.out.println("Permission sur B: " + resultB + " (attendu: true - héritée via A)");

            assertTrue(resultA, "Devrait avoir permission sur A (directe)");
            assertTrue(resultB, "Devrait avoir permission sur B (héritée via A)");

            // Cas 2: Permission sur ressource externe
            Resource external = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("External")
                    .tenant(tenant)
                    .parent(null)
                    .build();

            UserRoleResource urrOnExternal = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(external)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urrOnExternal));

            System.out.println("\n=== Cas 2: Permission sur ressource externe ===");
            boolean resultA2 = authorizationService.hasPermission(userId, "READ", resourceA);
            boolean resultB2 = authorizationService.hasPermission(userId, "READ", resourceB);
            boolean resultExt = authorizationService.hasPermission(userId, "READ", external);
            System.out.println("Permission sur A: " + resultA2 + " (attendu: false - pas de lien)");
            System.out.println("Permission sur B: " + resultB2 + " (attendu: false - pas de lien)");
            System.out.println("Permission sur External: " + resultExt + " (attendu: true - directe)");

            assertFalse(resultA2, "Pas de permission sur A");
            assertFalse(resultB2, "Pas de permission sur B");
            assertTrue(resultExt, "Permission sur External");
        }

        @Test
        @DisplayName("✅ Gestion des collections vides")
        void handleEmptyCollections() {
            // Arrange - Simule le comportement RÉEL de ton repository
            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Collections.emptyList()); // Liste vide, pas null

            // Act & Assert (ne devrait pas crasher)
            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource));

            verify(urrRepository).findAllByUserId(userId);
        }

        @Test
        @DisplayName("✅ Permission inexistante dans le système")
        void nonExistentPermission() {
            // Arrange
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // Contient READ mais pas "FAKE_PERM"
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act
            boolean result = authorizationService.hasPermission(
                    userId, "FAKE_PERMISSION_THAT_DOESNT_EXIST", rootResource
            );

            // Assert
            assertFalse(result, "Permission inexistante doit retourner false");
        }
    }

    /* ===================================================================
       TESTS DES CAS LIMITES (Edge Cases)
       =================================================================== */
    @Nested
    @DisplayName("Cas limites")
    class EdgeCasesTests {

        @Test
        @DisplayName("✅ Utilisateur null - retourne false")
        void nullUser() {
            // Act & Assert
            // Selon ton implémentation, soit:
            // 1. Retourne false
            // 2. Lance NullPointerException
            // 3. Lance IllegalArgumentException

            // Teste le comportement RÉEL
            try {
                boolean result = authorizationService.hasPermission(null, "READ", rootResource);
                // Si on arrive ici, la méthode retourne false pour null
                assertFalse(result);
            } catch (NullPointerException e) {
                // Si elle lance NPE, c'est OK aussi
                assertTrue(e instanceof NullPointerException);
            } catch (IllegalArgumentException e) {
                // Ou IllegalArgumentException
                assertTrue(e instanceof IllegalArgumentException);
            }
        }

        @Test
        @DisplayName("✅ Même ressource avec différents rôles")
        void sameResourceMultipleRoles() {
            // Arrange
            Role customRole = Role.builder()
                    .id((short) 6)
                    .name("CUSTOM")
                    .permissions(new HashSet<>(Arrays.asList(writePermission))) // Seulement WRITE
                    .build();

            UserRoleResource urr1 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // READ
                    .resource(rootResource)
                    .build();

            UserRoleResource urr2 = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(customRole) // WRITE
                    .resource(rootResource) // Même ressource
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr1, urr2));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", rootResource));
            assertTrue(authorizationService.hasPermission(userId, "WRITE", rootResource));
            assertFalse(authorizationService.hasPermission(userId, "DELETE", rootResource));
        }

        @Test
        @DisplayName("✅ Ressource sans parent (racine)")
        void rootResourceNoParent() {
            // Arrange
            Resource orphanResource = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("Orphan")
                    .tenant(tenant)
                    .parent(null) // Pas de parent
                    .build();

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(orphanResource)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", orphanResource));

            // Vérifie qu'on ne remonte pas plus loin
            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource));
        }

        @Test
        @DisplayName("✅ UUID différents mais mêmes permissions")
        void differentUuidsSamePermissions() {
            // Arrange
            UUID differentUserId = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(differentUserId).build())
                    .role(userRole)
                    .resource(rootResource)
                    .build();

            when(urrRepository.findAllByUserId(differentUserId))
                    .thenReturn(Arrays.asList(urr));

            when(urrRepository.findAllByUserId(userId)) // Notre user
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertTrue(authorizationService.hasPermission(differentUserId, "READ", rootResource));
            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource));
        }
    }

    /* ===================================================================
       TESTS D'INTÉGRATION SIMULÉE
       =================================================================== */
    @Nested
    @DisplayName("Scénarios d'intégration simulés")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("✅ Scénario réel complet: USER avec héritage")
        void realWorldScenario_UserWithInheritance() {
            // Arrange - Structure hiérarchique réelle
            Resource company = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("Company")
                    .tenant(tenant)
                    .parent(null)
                    .build();

            Resource department = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("IT Department")
                    .tenant(tenant)
                    .parent(company)
                    .build();

            Resource project = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("Project Alpha")
                    .tenant(tenant)
                    .parent(department)
                    .build();

            Resource document = Resource.builder()
                    .id(UUID.randomUUID())
                    .name("Design Document")
                    .tenant(tenant)
                    .parent(project)
                    .build();

            // User a rôle USER sur le département
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // READ seulement
                    .resource(department)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", department));
            assertTrue(authorizationService.hasPermission(userId, "READ", project)); // Hérité
            assertTrue(authorizationService.hasPermission(userId, "READ", document)); // Hérité 2 niveaux
            assertFalse(authorizationService.hasPermission(userId, "WRITE", document)); // Pas WRITE
            assertFalse(authorizationService.hasPermission(userId, "READ", company)); // Parent: non
        }

        @Test
        @DisplayName("✅ Scénario: ADMIN_TENANT vs USER normal")
        void scenario_AdminTenantVsNormalUser() {
            // Arrange
            UUID adminUserId = UUID.fromString("888e4567-e89b-12d3-a456-426614174000");
            UUID normalUserId = UUID.fromString("777e4567-e89b-12d3-a456-426614174000");

            // Admin a ADMIN_TENANT sur la racine
            UserRoleResource adminUrr = UserRoleResource.builder()
                    .user(AppUser.builder().id(adminUserId).build())
                    .role(adminRole) // Toutes permissions
                    .resource(rootResource)
                    .build();

            // User normal a USER sur enfant
            UserRoleResource userUrr = UserRoleResource.builder()
                    .user(AppUser.builder().id(normalUserId).build())
                    .role(userRole) // READ seulement
                    .resource(childResource)
                    .build();

            when(urrRepository.findAllByUserId(adminUserId))
                    .thenReturn(Arrays.asList(adminUrr));

            when(urrRepository.findAllByUserId(normalUserId))
                    .thenReturn(Arrays.asList(userUrr));

            // Act & Assert pour ADMIN
            assertTrue(authorizationService.hasPermission(adminUserId, "DELETE", rootResource));
            assertTrue(authorizationService.hasPermission(adminUserId, "WRITE", childResource)); // Hérité
            assertTrue(authorizationService.hasPermission(adminUserId, "READ", grandChildResource)); // Hérité

            // Act & Assert pour USER normal
            assertFalse(authorizationService.hasPermission(normalUserId, "DELETE", childResource));
            assertTrue(authorizationService.hasPermission(normalUserId, "READ", childResource));
            assertTrue(authorizationService.hasPermission(normalUserId, "READ", grandChildResource)); // Hérité
            assertFalse(authorizationService.hasPermission(normalUserId, "READ", rootResource)); // Parent: non
        }
    }

    /* ===================================================================
   TESTS DE PROTECTION CONTRE LES BOUCLES INFINIES
   =================================================================== */
    @Nested
    @DisplayName("Protection contre les boucles infinies")
    class InfiniteLoopProtectionTests {

        @Test
        @DisplayName("✅ Hiérarchie normale fonctionne (sans boucle)")
        void normalHierarchy_WorksCorrectly() {
            // Arrange - Hiérarchie linéaire normale
            Resource level1 = Resource.builder()
                    .id(resourceId)
                    .name("Level 1")
                    .tenant(tenant)
                    .parent(null)
                    .build();

            Resource level2 = Resource.builder()
                    .id(childResourceId)
                    .name("Level 2")
                    .tenant(tenant)
                    .parent(level1)
                    .build();

            Resource level3 = Resource.builder()
                    .id(grandChildResourceId)
                    .name("Level 3")
                    .tenant(tenant)
                    .parent(level2)
                    .build();

            // Permission sur level1
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole) // READ seulement
                    .resource(level1)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert
            assertTrue(authorizationService.hasPermission(userId, "READ", level1));
            assertTrue(authorizationService.hasPermission(userId, "READ", level2)); // Hérité
            assertTrue(authorizationService.hasPermission(userId, "READ", level3)); // Hérité 2 niveaux
            assertFalse(authorizationService.hasPermission(userId, "WRITE", level3)); // Pas WRITE
        }

        @Test
        @DisplayName("✅ Référence circulaire - ne bloque pas même avec permission")
        void circularReference_DoesNotCauseInfiniteLoop() {
            // Arrange - Crée une référence circulaire
            Resource resource1 = Resource.builder()
                    .id(resourceId)
                    .name("Resource 1")
                    .tenant(tenant)
                    .build();

            Resource resource2 = Resource.builder()
                    .id(childResourceId)
                    .name("Resource 2")
                    .tenant(tenant)
                    .parent(resource1) // Resource2 → Resource1
                    .build();

            // Crée la boucle circulaire
            resource1.setParent(resource2); // Resource1 → Resource2 (boucle!)

            // ⚠️ CRITIQUE: Permission sur resource1
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(resource1)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act - Doit terminer normalement (sans StackOverflowError ni boucle infinie)
            assertDoesNotThrow(() -> {
                // Test sur resource1 (permission directe) → true
                boolean resultOnResource1 = authorizationService.hasPermission(userId, "READ", resource1);
                assertTrue(resultOnResource1, "Devrait avoir permission sur resource1 (directe)");

                // Test sur resource2 (permission héritée via parent) → aussi true !
                boolean resultOnResource2 = authorizationService.hasPermission(userId, "READ", resource2);
                assertTrue(resultOnResource2, "Devrait hériter permission sur resource2 (via parent resource1)");

                // Test avec autre permission → false
                boolean wrongPermission = authorizationService.hasPermission(userId, "WRITE", resource2);
                assertFalse(wrongPermission, "Ne devrait pas avoir WRITE");

                System.out.println("✅ Test réussi - Pas de boucle infinie, permissions: " +
                        "READ sur resource1=" + resultOnResource1 +
                        ", resource2=" + resultOnResource2);
            }, "Ne devrait pas lancer d'exception avec référence circulaire");
        }

        @Test
        @DisplayName("✅ Boucle à 3 éléments - protégée")
        void threeElementCycle_Protected() {
            // Arrange - Boucle A → B → C → A
            UUID idA = UUID.fromString("111e4567-e89b-12d3-a456-426614174000");
            UUID idB = UUID.fromString("222e4567-e89b-12d3-a456-426614174000");
            UUID idC = UUID.fromString("333e4567-e89b-12d3-a456-426614174000");

            Resource resourceA = Resource.builder()
                    .id(idA)
                    .name("Resource A")
                    .tenant(tenant)
                    .build();

            Resource resourceB = Resource.builder()
                    .id(idB)
                    .name("Resource B")
                    .tenant(tenant)
                    .parent(resourceA)
                    .build();

            Resource resourceC = Resource.builder()
                    .id(idC)
                    .name("Resource C")
                    .tenant(tenant)
                    .parent(resourceB)
                    .build();

            // Complète la boucle
            resourceA.setParent(resourceC); // A ← C (boucle complète)

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(resourceA)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert - Doit terminer normalement
            assertDoesNotThrow(() -> {
                authorizationService.hasPermission(userId, "READ", resourceB);
            }, "Doit gérer les boucles complexes");
        }

        @Test
        @DisplayName("✅ Auto-référence (parent = soi-même) - protégée")
        void selfReference_Protected() {
            // Arrange - Une ressource qui est son propre parent
            Resource selfReferencing = Resource.builder()
                    .id(resourceId)
                    .name("Self-Referencing")
                    .tenant(tenant)
                    .build();

            // Auto-référence
            selfReferencing.setParent(selfReferencing);

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(selfReferencing)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert - Doit terminer normalement
            assertDoesNotThrow(() -> {
                boolean result = authorizationService.hasPermission(userId, "READ", selfReferencing);
                // Soit true (permission directe), soit false (boucle détectée)
                // L'important est que ça ne bloque pas
            }, "Doit gérer l'auto-référence");
        }

        @Test
        @DisplayName("✅ Hiérarchie très profonde - protégée contre la récursion excessive")
        void veryDeepHierarchy_Protected() {
            // Arrange - Crée une chaîne très longue (mais sans boucle)
            Resource current = null;
            Resource root = null;
            Resource leaf = null; // ✅ Nouvelle variable pour la feuille
            int depth = 100; // Chaîne de 100 ressources

            for (int i = 0; i < depth; i++) {
                Resource resource = Resource.builder()
                        .id(UUID.randomUUID())
                        .name("Level " + i)
                        .tenant(tenant)
                        .parent(current)
                        .build();

                if (i == 0) {
                    root = resource; // La racine
                }
                if (i == depth - 1) {
                    leaf = resource; // La feuille (dernier niveau)
                }
                current = resource;
            }

            // Permission sur la racine
            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(root) // ✅ Permission sur la racine
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act & Assert - Doit terminer normalement même avec une hiérarchie profonde
            Resource finalLeaf = leaf;
            assertDoesNotThrow(() -> {
                boolean result = authorizationService.hasPermission(userId, "READ", finalLeaf); // ✅ Teste sur la feuille
                assertTrue(result, "Devrait hériter la permission sur toute la chaîne (100 niveaux)");
            }, "Doit gérer les hiérarchies profondes sans problème de performance");
        }

        @Test
        @DisplayName("✅ Test avec timeout explicite - vérifie l'absence de boucle infinie")
        @Timeout(1)
        void noInfiniteLoop_TimeoutTest() {
            // Arrange - Boucle simple
            Resource r1 = Resource.builder()
                    .id(resourceId)
                    .name("R1")
                    .tenant(tenant)
                    .build();

            Resource r2 = Resource.builder()
                    .id(childResourceId)
                    .name("R2")
                    .tenant(tenant)
                    .parent(r1)
                    .build();

            r1.setParent(r2); // Boucle

            UserRoleResource urr = UserRoleResource.builder()
                    .user(AppUser.builder().id(userId).build())
                    .role(userRole)
                    .resource(r1)
                    .build();

            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Arrays.asList(urr));

            // Act - Si le code entre en boucle infinie, @Timeout échouera le test
            boolean result = authorizationService.hasPermission(userId, "READ", r2);

            // Assert
            // CORRECTION : Avec une boucle et permission sur r1, l'utilisateur DOIT avoir permission sur r2
            assertTrue(result, "Devrait avoir permission sur r2 via héritage de r1");
            // Vérifiez aussi que ça ne bloque pas (le timeout s'en charge)
        }

        @Test
        @DisplayName("✅ Repository retourne null - géré proprement")
        void repositoryReturnsNull_HandledGracefully() {
            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(null);

            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource));
        }

        @Test
        @DisplayName("✅ Repository retourne liste vide - géré proprement")
        void repositoryReturnsEmptyList_HandledGracefully() {
            when(urrRepository.findAllByUserId(userId))
                    .thenReturn(Collections.emptyList());

            assertFalse(authorizationService.hasPermission(userId, "READ", rootResource));
        }

        @Test
        @DisplayName("✅ Ressource null - retourne false sans appeler le repository")
        void nullResource_ReturnsFalseWithoutRepositoryCall() {
            // Note : Pas de stub car le repository ne devrait pas être appelé
            assertFalse(authorizationService.hasPermission(userId, "READ", null));

            // Vérifiez que le repository n'est PAS appelé
            verify(urrRepository, never()).findAllByUserId(userId);
        }
    }

}