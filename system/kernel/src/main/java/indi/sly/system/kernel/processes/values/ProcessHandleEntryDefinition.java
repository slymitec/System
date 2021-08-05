package indi.sly.system.kernel.processes.values;

import indi.sly.system.common.supports.NumberUtil;
import indi.sly.system.common.supports.ObjectUtil;
import indi.sly.system.common.supports.UUIDUtil;
import indi.sly.system.common.values.IdentificationDefinition;
import indi.sly.system.common.values.ADefinition;
import indi.sly.system.kernel.objects.values.InfoOpenDefinition;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.Map.Entry;

public class ProcessHandleEntryDefinition extends ADefinition<ProcessHandleEntryDefinition> {
    private UUID handle;
    private final Map<Long, Long> date;
    private UUID infoID;
    private final List<IdentificationDefinition> identifications;
    private InfoOpenDefinition infoOpen;

    public ProcessHandleEntryDefinition() {
        this.date = new HashMap<>();
        this.identifications = new ArrayList<>();
    }

    public UUID getHandle() {
        return this.handle;
    }

    public void setHandle(UUID handle) {
        this.handle = handle;
    }

    public Map<Long, Long> getDate() {
        return this.date;
    }

    public UUID getInfoID() {
        return this.infoID;
    }

    public void setInfoID(UUID infoID) {
        this.infoID = infoID;
    }

    public List<IdentificationDefinition> getIdentifications() {
        return this.identifications;
    }

    public InfoOpenDefinition getInfoOpen() {
        return this.infoOpen;
    }

    public void setInfoOpen(InfoOpenDefinition infoOpen) {
        this.infoOpen = infoOpen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessHandleEntryDefinition that = (ProcessHandleEntryDefinition) o;
        return Objects.equals(handle, that.handle) && date.equals(that.date) && Objects.equals(infoID, that.infoID) && identifications.equals(that.identifications) && Objects.equals(infoOpen, that.infoOpen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle, date, infoID, identifications, infoOpen);
    }

    @Override
    public ProcessHandleEntryDefinition deepClone() {
        ProcessHandleEntryDefinition definition = new ProcessHandleEntryDefinition();

        definition.handle = this.handle;
        definition.date.putAll(this.date);
        definition.infoID = this.infoID;
        definition.identifications.addAll(this.identifications);
        definition.infoOpen = this.infoOpen.deepClone();

        return definition;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        this.handle = UUIDUtil.readExternal(in);

        int valueInteger;

        valueInteger = NumberUtil.readExternalInteger(in);
        for (int i = 0; i < valueInteger; i++) {
            this.date.put(NumberUtil.readExternalLong(in), NumberUtil.readExternalLong(in));
        }

        this.infoID = UUIDUtil.readExternal(in);

        valueInteger = NumberUtil.readExternalInteger(in);
        for (int i = 0; i < valueInteger; i++) {
            this.identifications.add(ObjectUtil.readExternal(in));
        }
        this.infoOpen = ObjectUtil.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        UUIDUtil.writeExternal(out, this.handle);

        for (Entry<Long, Long> pair : this.date.entrySet()) {
            NumberUtil.writeExternalLong(out, pair.getKey());
            NumberUtil.writeExternalLong(out, pair.getValue());
        }

        UUIDUtil.writeExternal(out, this.infoID);

        NumberUtil.writeExternalInteger(out, this.identifications.size());
        for (IdentificationDefinition pair : this.identifications) {
            ObjectUtil.writeExternal(out, pair);
        }

        ObjectUtil.writeExternal(out, this.infoOpen);
    }
}
