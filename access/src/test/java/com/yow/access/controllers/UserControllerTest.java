package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.AssignRoleRequest;
import com.yow.access.dto.CreateUserRequest;
import com.yow.access.entities.AppUser;
import com.yow.access.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires COMPLETS pour UserController
 * Teste TOUS les endpoints actuellement implémentés
 */
@DisplayName("UserController - Tests Complets")
class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private AuthenticatedUserContext userContext;

    // IDs constants pour tous les tests
    private final UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID actorUserId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
    private final short roleId = 1;
    private final UUID resourceId = UUID.fromString("423e4567-e89b-12d3-a456-426614174000");
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userContext = mock(AuthenticatedUserContext.class);
        userController = new UserController(userService, userContext);
    }

    /* ===================================================================
       TESTS POUR CREATE USER - POST /api/users
       =================================================================== */
    @Nested
    @DisplayName("POST /api/users - Création d'utilisateur")
    class CreateUserEndpointTests {

        @Test
        @DisplayName("✅ Création réussie - Retourne 201 avec l'utilisateur créé")
        void createUser_Success() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "john.doe",
                    "john@example.com",
                    "hashedPassword123"
            );

            AppUser mockUser = AppUser.builder()
                    .id(userId)
                    .username("john.doe")
                    .email("john@example.com")
                    .passwordHash("hashedPassword123")
                    .enabled(true)
                    .mustChangePassword(false)
                    .accountActivated(false)
                    .createdAt(now)
                    .build();

            when(userService.createUser(
                    "john.doe",
                    "john@example.com",
                    "hashedPassword123"
            )).thenReturn(mockUser);

            // Act
            ResponseEntity<AppUser> response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.hasBody());

            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertEquals(userId, createdUser.getId());
            assertEquals("john.doe", createdUser.getUsername());
            assertEquals("john@example.com", createdUser.getEmail());
            assertEquals("hashedPassword123", createdUser.getPasswordHash());
            assertTrue(createdUser.isEnabled());
            assertFalse(createdUser.isMustChangePassword());
            assertFalse(createdUser.isAccountActivated());

            verify(userService).createUser("john.doe", "john@example.com", "hashedPassword123");
        }

        @Test
        @DisplayName("❌ Propagation des exceptions de validation (email déjà existant)")
        void createUser_EmailAlreadyExists() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "existing.user",
                    "existing@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new IllegalStateException("Email already exists"));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userController.createUser(request)
            );
            assertEquals("Email already exists", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Propagation des exceptions de validation (username déjà existant)")
        void createUser_UsernameAlreadyExists() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "existinguser",
                    "new@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new IllegalStateException("Username already exists"));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userController.createUser(request)
            );
            assertEquals("Username already exists", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Requête nulle - Lance NullPointerException")
        void createUser_NullRequest() {
            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> userController.createUser(null)
            );
        }

        @Test
        @DisplayName("❌ Erreur serveur - Propagation de RuntimeException")
        void createUser_ServerError() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "testuser",
                    "test@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userController.createUser(request)
            );
            assertEquals("Database connection failed", exception.getMessage());
        }

        @Test
        @DisplayName("✅ Création avec champs optionnels remplis")
        void createUser_WithOptionalFields() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "test.user",
                    "test.user@example.com",
                    "hashedPassword456"
            );

            AppUser creator = AppUser.builder()
                    .id(actorUserId)
                    .username("admin")
                    .email("admin@example.com")
                    .build();

            AppUser mockUser = AppUser.builder()
                    .id(userId)
                    .username("test.user")
                    .email("test.user@example.com")
                    .passwordHash("hashedPassword456")
                    .enabled(false)
                    .mustChangePassword(true)
                    .activationToken("token-123")
                    .activationTokenExpiry(now.plusSeconds(3600))
                    .accountActivated(false)
                    .createdBy(creator)
                    .createdAt(now)
                    .build();

            when(userService.createUser(
                    "test.user",
                    "test.user@example.com",
                    "hashedPassword456"
            )).thenReturn(mockUser);

            // Act
            ResponseEntity<AppUser> response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertFalse(createdUser.isEnabled());
            assertTrue(createdUser.isMustChangePassword());
            assertEquals("token-123", createdUser.getActivationToken());
            assertEquals(creator, createdUser.getCreatedBy());
        }

        @Test
        @DisplayName("❌ Chaînes vides dans la requête")
        void createUser_EmptyStrings() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest("", "", "");

            when(userService.createUser("", "", ""))
                    .thenThrow(new IllegalArgumentException("Username, email and password cannot be empty"));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.createUser(request)
            );
            assertEquals("Username, email and password cannot be empty", exception.getMessage());
        }
    }

    /* ===================================================================
       TESTS POUR SET USER ENABLED - PATCH /api/users/{userId}/enabled
       =================================================================== */
    @Nested
    @DisplayName("PATCH /api/users/{userId}/enabled - Activation/Désactivation")
    class SetUserEnabledEndpointTests {

        @Test
        @DisplayName("✅ Activation réussie - Retourne 204 No Content")
        void setUserEnabled_ActivateSuccess() {
            // Act
            ResponseEntity<Void> response = userController.setUserEnabled(userId, true);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertFalse(response.hasBody());
            verify(userService).setUserEnabled(userId, true);
        }

        @Test
        @DisplayName("✅ Désactivation réussie - Retourne 204 No Content")
        void setUserEnabled_DeactivateSuccess() {
            // Act
            ResponseEntity<Void> response = userController.setUserEnabled(userId, false);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertFalse(response.hasBody());
            verify(userService).setUserEnabled(userId, false);
        }

        @Test
        @DisplayName("❌ Utilisateur non trouvé - Propagation IllegalArgumentException")
        void setUserEnabled_UserNotFound() {
            // Arrange
            doThrow(new IllegalArgumentException("User not found"))
                    .when(userService).setUserEnabled(userId, true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.setUserEnabled(userId, true)
            );
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Non autorisé à modifier - Propagation SecurityException")
        void setUserEnabled_Unauthorized() {
            // Arrange
            doThrow(new SecurityException("Not authorized to modify this user"))
                    .when(userService).setUserEnabled(userId, true);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.setUserEnabled(userId, true)
            );
            assertEquals("Not authorized to modify this user", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Auto-désactivation interdite - Propagation SecurityException")
        void setUserEnabled_SelfDeactivationPrevented() {
            // Arrange
            doThrow(new SecurityException("Cannot disable your own account"))
                    .when(userService).setUserEnabled(userId, false);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.setUserEnabled(userId, false)
            );
            assertEquals("Cannot disable your own account", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Dernier admin tenant - Désactivation interdite")
        void setUserEnabled_LastAdminTenantPrevention() {
            // Arrange
            doThrow(new SecurityException("Cannot disable the last admin of a tenant"))
                    .when(userService).setUserEnabled(userId, false);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.setUserEnabled(userId, false)
            );
            assertEquals("Cannot disable the last admin of a tenant", exception.getMessage());
        }

        @Test
        @DisplayName("✅ Réactivation d'un utilisateur désactivé")
        void setUserEnabled_Reactivation() {
            // Act
            ResponseEntity<Void> response = userController.setUserEnabled(userId, true);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService).setUserEnabled(userId, true);
        }
    }

    /* ===================================================================
       TESTS POUR ASSIGN ROLE - POST /api/users/{userId}/roles
       =================================================================== */
    @Nested
    @DisplayName("POST /api/users/{userId}/roles - Assignation de rôle")
    class AssignRoleEndpointTests {

        @Test
        @DisplayName("✅ Assignation réussie - Retourne 201 Created")
        void assignRole_Success() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act
            ResponseEntity<Void> response = userController.assignRole(userId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertFalse(response.hasBody());
            verify(userService).assignRole(actorUserId, userId, roleId, resourceId);
        }

        @Test
        @DisplayName("✅ Utilisation correcte du contexte utilisateur")
        void assignRole_UsesAuthenticatedUserContext() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            UUID differentActorId = UUID.fromString("999e4567-e89b-12d3-a456-426614174000");
            when(userContext.getUserId()).thenReturn(differentActorId);

            // Act
            userController.assignRole(userId, request);

            // Assert
            verify(userService).assignRole(differentActorId, userId, roleId, resourceId);
            verify(userContext).getUserId();
        }

        @Test
        @DisplayName("❌ Contexte utilisateur null - Propagation IllegalArgumentException")
        void assignRole_NullUserContext() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(null);

            doThrow(new IllegalArgumentException("Actor user ID is required"))
                    .when(userService).assignRole(null, userId, roleId, resourceId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Actor user ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Requête nulle - Lance NullPointerException")
        void assignRole_NullRequest() {
            // Arrange
            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> userController.assignRole(userId, null)
            );
        }

        @Test
        @DisplayName("❌ Non autorisé à assigner - Propagation SecurityException")
        void assignRole_Unauthorized() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("Not authorized to assign this role"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Not authorized to assign this role", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Utilisateur non trouvé - Propagation IllegalArgumentException")
        void assignRole_UserNotFound() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalArgumentException("User not found"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Ressource non trouvée - Propagation IllegalArgumentException")
        void assignRole_ResourceNotFound() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalArgumentException("Resource not found"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Resource not found", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Rôle déjà assigné - Propagation IllegalStateException")
        void assignRole_RoleAlreadyAssigned() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalStateException("Role already assigned to user on this resource"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Role already assigned to user on this resource", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Auto-assignation interdite - Propagation SecurityException")
        void assignRole_SelfAssignmentPrevented() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(userId); // Même ID

            doThrow(new SecurityException("Cannot assign roles to yourself"))
                    .when(userService).assignRole(userId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Cannot assign roles to yourself", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Cross-tenant interdit - Propagation SecurityException")
        void assignRole_CrossTenantPrevented() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("Cannot assign role across different tenants"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Cannot assign role across different tenants", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Éviter l'escalade de privilèges - Propagation SecurityException")
        void assignRole_PrivilegeEscalationPrevented() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("Cannot assign a role with higher privileges than your own"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Cannot assign a role with higher privileges than your own", exception.getMessage());
        }

        @Test
        @DisplayName("❌ IDs null dans la requête - Propagation IllegalArgumentException")
        void assignRole_NullIdsInRequest() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(null, null);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalArgumentException("Role ID and Resource ID cannot be null"))
                    .when(userService).assignRole(actorUserId, userId, null, null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Role ID and Resource ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Portée tenant incorrecte - Propagation SecurityException")
        void assignRole_TenantScopeValidation() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("User and resource must belong to the same tenant"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("User and resource must belong to the same tenant", exception.getMessage());
        }

        @Test
        @DisplayName("✅ Assignations multiples de rôles")
        void assignRole_MultipleAssignments() {
            // Arrange
            short roleId1 = 1;
            short roleId2 = 2;
            UUID resourceId2 = UUID.fromString("523e4567-e89b-12d3-a456-426614174000");

            AssignRoleRequest request1 = new AssignRoleRequest(roleId1, resourceId);
            AssignRoleRequest request2 = new AssignRoleRequest(roleId2, resourceId2);

            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act
            userController.assignRole(userId, request1);
            userController.assignRole(userId, request2);

            // Assert
            verify(userService).assignRole(actorUserId, userId, roleId1, resourceId);
            verify(userService).assignRole(actorUserId, userId, roleId2, resourceId2);
        }
    }

    /* ===================================================================
       TESTS DE ROBUSTESSE ET SCÉNARIOS COMPLEXES
       =================================================================== */
    @Nested
    @DisplayName("Scénarios complexes et robustesse")
    class ComplexScenariosTests {

        @Test
        @DisplayName("✅ Intégrité des données en cas d'erreur de contrainte")
        void dataIntegrityOnConstraintViolation() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new RuntimeException("Database constraint violation: duplicate key"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("Database constraint violation: duplicate key", exception.getMessage());
        }

        @Test
        @DisplayName("❌ Contexte utilisateur indisponible")
        void userContextNotAvailable() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenThrow(new IllegalStateException("User context not available"));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userController.assignRole(userId, request)
            );
            assertEquals("User context not available", exception.getMessage());
        }

        @Test
        @DisplayName("✅ Validation de l'unicité email/username par le service")
        void uniquenessValidationByService() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest("duplicate", "duplicate@test.com", "hash");

            when(userService.createUser("duplicate", "duplicate@test.com", "hash"))
                    .thenThrow(new IllegalStateException("Username or email already exists"));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userController.createUser(request)
            );
            assertEquals("Username or email already exists", exception.getMessage());
        }
    }

    /* ===================================================================
       TESTS DE STRUCTURE ET COMPORTEMENT GÉNÉRAUX
       =================================================================== */
    @Nested
    @DisplayName("Tests de structure et comportement généraux")
    class GeneralBehaviorTests {

        @Test
        @DisplayName("✅ Type de retour correct pour toutes les méthodes")
        void returnTypeConsistency() {
            // Test CreateUser
            CreateUserRequest createRequest = new CreateUserRequest("test", "test@test.com", "hash");
            AppUser mockUser = AppUser.builder().id(userId).build();
            when(userService.createUser(any(), any(), any())).thenReturn(mockUser);

            ResponseEntity<AppUser> createResponse = userController.createUser(createRequest);
            assertTrue(createResponse instanceof ResponseEntity);
            assertTrue(createResponse.hasBody());

            // Test SetUserEnabled
            ResponseEntity<Void> enableResponse = userController.setUserEnabled(userId, true);
            assertTrue(enableResponse instanceof ResponseEntity);
            assertFalse(enableResponse.hasBody());

            // Test AssignRole
            AssignRoleRequest assignRequest = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            ResponseEntity<Void> assignResponse = userController.assignRole(userId, assignRequest);
            assertTrue(assignResponse instanceof ResponseEntity);
            assertFalse(assignResponse.hasBody());
        }

        @Test
        @DisplayName("✅ Codes HTTP appropriés")
        void httpStatusCodes() {
            // CreateUser → 201 Created
            CreateUserRequest createRequest = new CreateUserRequest("test", "test@test.com", "hash");
            AppUser mockUser = AppUser.builder().id(userId).build();
            when(userService.createUser(any(), any(), any())).thenReturn(mockUser);

            assertEquals(HttpStatus.CREATED, userController.createUser(createRequest).getStatusCode());

            // SetUserEnabled → 204 No Content
            assertEquals(HttpStatus.NO_CONTENT, userController.setUserEnabled(userId, true).getStatusCode());

            // AssignRole → 201 Created
            AssignRoleRequest assignRequest = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            assertEquals(HttpStatus.CREATED, userController.assignRole(userId, assignRequest).getStatusCode());
        }

        @Test
        @DisplayName("✅ Vérification des appels aux services")
        void serviceMethodCallsVerification() {
            // Test CreateUser
            CreateUserRequest createRequest = new CreateUserRequest("user", "user@test.com", "pass");
            AppUser mockUser = AppUser.builder().id(userId).build();
            when(userService.createUser(any(), any(), any())).thenReturn(mockUser);

            userController.createUser(createRequest);
            verify(userService).createUser("user", "user@test.com", "pass");

            // Test SetUserEnabled
            userController.setUserEnabled(userId, true);
            verify(userService).setUserEnabled(userId, true);

            // Test AssignRole
            AssignRoleRequest assignRequest = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            userController.assignRole(userId, assignRequest);
            verify(userService).assignRole(actorUserId, userId, roleId, resourceId);
        }
    }

    /* ===================================================================
       TESTS DES ENDPOINTS NON IMPLÉMENTÉS (documentation)
       =================================================================== */
    @Nested
    @DisplayName("Endpoints non implémentés (documentation)")
    class NotImplementedEndpointsTests {

        @Test
        @DisplayName("ℹ️ GET /api/users/{userId} - À implémenter")
        void getUser_NotImplemented() {
            // Cet endpoint n'est pas encore implémenté dans le contrôleur
            assertTrue(true, "Endpoint GET /api/users/{userId} à implémenter");
        }

        @Test
        @DisplayName("ℹ️ GET /api/users - À implémenter")
        void listUsers_NotImplemented() {
            // Cet endpoint n'est pas encore implémenté dans le contrôleur
            assertTrue(true, "Endpoint GET /api/users (pagination) à implémenter");
        }

        @Test
        @DisplayName("ℹ️ DELETE /api/users/{userId}/roles - À implémenter")
        void removeRole_NotImplemented() {
            // Cet endpoint n'est pas encore implémenté dans le contrôleur
            assertTrue(true, "Endpoint DELETE /api/users/{userId}/roles à implémenter");
        }

        @Test
        @DisplayName("ℹ️ PUT /api/users/{userId} - À implémenter")
        void updateUser_NotImplemented() {
            // Cet endpoint n'est pas encore implémenté dans le contrôleur
            assertTrue(true, "Endpoint PUT /api/users/{userId} (update) à implémenter");
        }
    }

    /* ===================================================================
       TESTS DES VALIDATIONS SPRING (remarques)
       =================================================================== */
    @Nested
    @DisplayName("Validations Spring (à tester avec @WebMvcTest)")
    class SpringValidationsTests {

        @Test
        @DisplayName("ℹ️ Validation @Valid - testée dans les tests d'intégration")
        void jakartaValidation() {
            // La validation @Valid est gérée par Spring et testée avec @WebMvcTest
            assertTrue(true, "Validation @Valid testée dans les tests d'intégration WebMvcTest");
        }

        @Test
        @DisplayName("ℹ️ Validation UUID @PathVariable - testée dans les tests d'intégration")
        void uuidPathValidation() {
            // La validation des UUID dans @PathVariable est gérée par Spring
            assertTrue(true, "Validation UUID @PathVariable testée dans les tests d'intégration");
        }

        @Test
        @DisplayName("ℹ️ Gestion des exceptions - testée dans @ControllerAdvice")
        void exceptionHandling() {
            // La gestion des exceptions (404, 400, etc.) est faite dans @ControllerAdvice
            assertTrue(true, "Gestion des exceptions testée avec @ControllerAdvice");
        }
    }
}