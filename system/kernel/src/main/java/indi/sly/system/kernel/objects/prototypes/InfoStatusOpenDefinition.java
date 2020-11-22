package indi.sly.system.kernel.objects.prototypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import indi.sly.system.common.support.IDeepCloneable;
import indi.sly.system.common.support.ISerializable;
import indi.sly.system.common.utility.NumberUtils;
import indi.sly.system.common.utility.ObjectUtils;

public class InfoStatusOpenDefinition implements ISerializable<InfoStatusOpenDefinition> {
    private long attribute;
    private ISerializable context;

    public long getAttribute() {
        return this.attribute;
    }

    public void setAttribute(long openAttribute) {
        this.attribute = openAttribute;
    }

    public ISerializable getContext() {
        return this.context;
    }

    public void setContext(ISerializable context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoStatusOpenDefinition that = (InfoStatusOpenDefinition) o;
        return attribute == that.attribute &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, context);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return this.deepClone();
    }

    @Override
    public InfoStatusOpenDefinition deepClone() {
        InfoStatusOpenDefinition infoStatusOpen = new InfoStatusOpenDefinition();

        infoStatusOpen.attribute = this.attribute;
        infoStatusOpen.context = this.context;

        return infoStatusOpen;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.attribute = NumberUtils.readExternalLong(in);
        this.context = ObjectUtils.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        NumberUtils.writeExternalLong(out, this.attribute);
        ObjectUtils.writeExternal(out, this.context);
    }
}
