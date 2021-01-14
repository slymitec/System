package indi.sly.system.kernel.sessions.instances.values;

import indi.sly.system.common.lang.ISerializeCapable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ClientDefinition implements ISerializeCapable<ClientDefinition> {
    public ClientDefinition() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return this.deepClone();
    }

    @Override
    public ClientDefinition deepClone() {
        ClientDefinition definition = new ClientDefinition();

        return definition;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }
}
