package com.jewelvaulterp.auth.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceContext;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.dto.InventoryResponse;
import com.jewelvaulterp.stockmovement.dto.StockMovementResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class CompanyOwnershipResolver {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public boolean isControllerResourceOwnedBy(HandlerMethod handler, UUID resourceId, UUID companyId) {
        Class<?> entityType = entityTypeFor(handler.getBeanType());
        if (entityType == null) return true;
        Object resource = entityManager.find(entityType, resourceId);
        if (resource == null) return true;
        Set<UUID> owners = companyIds(resource);
        return owners.isEmpty() || owners.stream().allMatch(companyId::equals);
    }

    @Transactional(readOnly = true)
    public Set<UUID> companyIds(Object value) {
        if (value == null) return Set.of();
        if (value instanceof InventoryResponse inventoryResponse) {
            return companyIds(entityManager.find(Inventory.class, inventoryResponse.id()));
        }
        if (value instanceof StockMovementResponse movementResponse) {
            return companyIds(entityManager.find(Inventory.class, movementResponse.inventoryId()));
        }
        Object directCompanyId = invoke(value, "companyId", "getCompanyId");
        if (directCompanyId instanceof UUID id) return Set.of(id);
        if (directCompanyId != null) {
            try {
                return Set.of(UUID.fromString(directCompanyId.toString()));
            } catch (IllegalArgumentException ignored) {
                return Set.of();
            }
        }
        Set<UUID> companies = new LinkedHashSet<>();
        collectCompanyIds(value, companies, Collections.newSetFromMap(new IdentityHashMap<>()));
        return companies;
    }

    public boolean belongsToCompany(Object value, UUID companyId) {
        Set<UUID> companies = companyIds(value);
        return companies.isEmpty() || companies.stream().allMatch(companyId::equals);
    }

    private Class<?> entityTypeFor(Class<?> controllerType) {
        String packageName = controllerType.getPackageName();
        if (!packageName.contains(".controller")) return null;
        String entityPackage = packageName.replaceFirst("\\.controller(?:\\..*)?$", ".entity");
        String entityName = controllerType.getSimpleName().replaceFirst("Controller$", "");
        try {
            Class<?> entityType = Class.forName(entityPackage + "." + entityName);
            return entityType.isAnnotationPresent(jakarta.persistence.Entity.class) ? entityType : null;
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private void collectCompanyIds(Object value, Set<UUID> companies, Set<Object> visited) {
        if (value == null || !visited.add(value)) return;
        if (value.getClass().getName().equals("com.jewelvaulterp.company.entity.Company")) {
            Object id = invoke(value, "getId", "id");
            if (id instanceof UUID companyId) companies.add(companyId);
            return;
        }

        for (Class<?> type = value.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ManyToOne.class) && !field.isAnnotationPresent(OneToOne.class)) continue;
                try {
                    if (!field.canAccess(value)) field.setAccessible(true);
                    collectCompanyIds(field.get(value), companies, visited);
                } catch (IllegalAccessException | RuntimeException ignored) {
                    // Unavailable lazy associations are not treated as ownership evidence.
                }
            }
        }
    }

    private Object invoke(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
                // Try the next conventional accessor.
            }
        }
        return null;
    }
}