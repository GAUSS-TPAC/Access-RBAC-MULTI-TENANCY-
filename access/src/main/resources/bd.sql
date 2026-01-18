CREATE TABLE app_user (
                          id              UUID PRIMARY KEY,
                          username        VARCHAR(100) NOT NULL UNIQUE,
                          email           VARCHAR(150) NOT NULL UNIQUE,
                          password_hash   VARCHAR(255) NOT NULL,
                          enabled         BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tenant (
                        id              UUID PRIMARY KEY,
                        name            VARCHAR(150) NOT NULL,
                        code            VARCHAR(50) NOT NULL UNIQUE,
                        status          VARCHAR(30),
                        created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resource (
                          id              UUID PRIMARY KEY,
                          tenant_id       UUID NOT NULL,
                          parent_id       UUID,
                          type            VARCHAR(50) NOT NULL,
                          name            VARCHAR(150) NOT NULL,
                          path            VARCHAR(500),
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_resource_tenant
                              FOREIGN KEY (tenant_id) REFERENCES tenant(id),

                          CONSTRAINT fk_resource_parent
                              FOREIGN KEY (parent_id) REFERENCES resource(id)
);

CREATE TABLE role (
                      id              UUID PRIMARY KEY,
                      name            VARCHAR(50) NOT NULL UNIQUE,
                      scope           VARCHAR(20) NOT NULL CHECK (scope IN ('GLOBAL', 'TENANT', 'RESOURCE'))
);

CREATE TABLE permission (
                            id              UUID PRIMARY KEY,
                            name            VARCHAR(100) NOT NULL UNIQUE,
                            description     VARCHAR(255)
);

CREATE TABLE role_permission (
                                 role_id         UUID NOT NULL,
                                 permission_id   UUID NOT NULL,

                                 PRIMARY KEY (role_id, permission_id),

                                 CONSTRAINT fk_rp_role
                                     FOREIGN KEY (role_id) REFERENCES role(id),

                                 CONSTRAINT fk_rp_permission
                                     FOREIGN KEY (permission_id) REFERENCES permission(id)
);

CREATE TABLE user_role_resource (
                                    id              UUID PRIMARY KEY,
                                    user_id         UUID NOT NULL,
                                    role_id         UUID NOT NULL,
                                    resource_id     UUID NOT NULL,
                                    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_urr_user
                                        FOREIGN KEY (user_id) REFERENCES app_user(id),

                                    CONSTRAINT fk_urr_role
                                        FOREIGN KEY (role_id) REFERENCES role(id),

                                    CONSTRAINT fk_urr_resource
                                        FOREIGN KEY (resource_id) REFERENCES resource(id),

                                    CONSTRAINT uq_user_role_resource
                                        UNIQUE (user_id, role_id, resource_id)
);


CREATE UNIQUE INDEX uq_one_admin_tenant
    ON user_role_resource (resource_id)
    WHERE role_id = (
    SELECT id FROM role WHERE name = 'ADMIN_TENANT'
);

CREATE UNIQUE INDEX uq_single_super_admin
    ON user_role_resource (role_id)
    WHERE role_id = (
    SELECT id FROM role WHERE name = 'SUPER_ADMIN'
);


CREATE INDEX idx_resource_tenant ON resource(tenant_id);
CREATE INDEX idx_resource_parent ON resource(parent_id);
CREATE INDEX idx_resource_path ON resource(path);

CREATE INDEX idx_urr_user ON user_role_resource(user_id);
CREATE INDEX idx_urr_resource ON user_role_resource(resource_id);
CREATE INDEX idx_urr_role ON user_role_resource(role_id);
