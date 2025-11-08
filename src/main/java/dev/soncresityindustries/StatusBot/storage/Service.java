package dev.soncresityindustries.StatusBot.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a service with its status information.
 * Supports hierarchical relationships through parent service references.
 *
 * @author SkyKing_PX
 */
public class Service {
    
    private final String displayName;
    private final String serviceId;
    private final String status;
    private final String outageDescription;
    private final String description;
    private final String parentId;

    /**
     * Creates a new Service instance.
     *
     * @param displayName The human-readable name of the service
     * @param serviceId The unique identifier for the service
     * @param status The current status of the service (e.g., "ONLINE", "OFFLINE", "MAINTENANCE")
     * @param outageDescription A brief description of any current outage, if applicable
     * @param description A detailed description of the service or its current state
     * @param parentId The ID of the parent service, or null if this is a root service
     */
    @JsonCreator
    public Service(
            @JsonProperty("displayName") String displayName,
            @JsonProperty("serviceId") String serviceId,
            @JsonProperty("status") String status,
            @JsonProperty("outageDescription") String outageDescription,
            @JsonProperty("description") String description,
            @JsonProperty("parentId") String parentId) {
        this.displayName = displayName;
        this.serviceId = serviceId;
        this.status = status;
        this.outageDescription = outageDescription;
        this.description = description;
        this.parentId = parentId;
    }

    /**
     * Creates a new Service instance without a parent (root service).
     *
     * @param displayName The human-readable name of the service
     * @param serviceId The unique identifier for the service
     * @param status The current status of the service
     * @param outageDescription A brief description of any current outage, if applicable
     * @param description A detailed description of the service or its current state
     */
    public Service(String displayName, String serviceId, String status, String outageDescription, String description) {
        this(displayName, serviceId, status, outageDescription, description, null);
    }

    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("serviceId")
    public String getServiceId() {
        return serviceId;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("outageDescription")
    public String getOutageDescription() {
        return outageDescription;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    /**
     * Checks if this service has a parent.
     *
     * @return true if the service has a parent, false otherwise
     */
    public boolean hasParent() {
        return parentId != null && !parentId.trim().isEmpty();
    }

    /**
     * Creates a new Service instance with updated status information.
     *
     * @param newStatus The new status
     * @param newDescription The new description
     * @return A new Service instance with updated information
     */
    public Service withUpdatedStatus(String newStatus, String newDescription, String newOutageDescription) {
        return new Service(this.displayName, this.serviceId, newStatus, newOutageDescription, newDescription, this.parentId);
    }

    /**
     * Creates a new Service instance with a different parent.
     *
     * @param newParentId The new parent ID
     * @return A new Service instance with updated parent
     */
    public Service withParent(String newParentId) {
        return new Service(this.displayName, this.serviceId, this.status, this.outageDescription, this.description, newParentId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(serviceId, service.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }

    @Override
    public String toString() {
        return "Service{" +
                "displayName='" + displayName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", status='" + status + '\'' +
                ", outageDescription='" + outageDescription + '\'' +
                ", description='" + description + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}