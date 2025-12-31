package com.yow.access.entities;

/**
 * Factory for Resource entity creation.
 */
public final class ResourceFactory {

    private ResourceFactory() {
    }

    public static Resource createRootResource(Tenant tenant, String name) {
        Resource resource = new Resource();
        resource.setTenant(tenant);
        resource.setParent(null);
        resource.setType("TENANT_ROOT");
        resource.setName(name);
        resource.setPath("/");
        return resource;
    }

    public static Resource createChildResource(
            Resource parent,
            String name,
            String type
    ) {
        Resource resource = new Resource();
        resource.setTenant(parent.getTenant());
        resource.setParent(parent);
        resource.setType(type);
        resource.setName(name);

        // Hierarchical path construction
        String parentPath = parent.getPath();
        String normalized = parentPath.endsWith("/")
                ? parentPath
                : parentPath + "/";

        resource.setPath(normalized + name);

        return resource;
    }
}
