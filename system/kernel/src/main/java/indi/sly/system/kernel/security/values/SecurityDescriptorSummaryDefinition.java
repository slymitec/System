package indi.sly.system.kernel.security.values;

import indi.sly.system.common.values.IdentificationDefinition;

import java.util.*;

public class SecurityDescriptorSummaryDefinition {
    public SecurityDescriptorSummaryDefinition() {
        this.identifications = new ArrayList<>();
        this.accessControl = new HashMap<>();
    }

    private final List<IdentificationDefinition> identifications;
    private boolean inherit;
    private final Map<UUID, Long> accessControl;

    public List<IdentificationDefinition> getIdentifications() {
        return this.identifications;
    }

    public boolean isInherit() {
        return this.inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    public Map<UUID, Long> getAccessControl() {
        return this.accessControl;
    }
}
