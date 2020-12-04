package indi.sly.system.kernel.core.prototypes;

import indi.sly.system.common.functions.Consumer;
import indi.sly.system.common.functions.Provider;
import indi.sly.system.common.utility.ObjectUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class ABytesProcessPrototype<T> extends ACoreProcessPrototype {
    private Provider<byte[]> funcRead;
    private Consumer<byte[]> funcWrite;
    protected T value;

    public final void setSource(Provider<byte[]> funcRead, Consumer<byte[]> funcWrite) {
        this.funcRead = funcRead;
        this.funcWrite = funcWrite;
    }

    protected final void init() {
        byte[] value = this.funcRead.acquire();
        this.read(value);
    }

    protected final void fresh() {
        byte[] value = this.write();
        this.funcWrite.accept(value);
    }

    protected void read(byte[] source) {
        if (ObjectUtils.isAnyNull(source)) {
            this.value = null;
        } else {
            this.value = ObjectUtils.transferFromByteArray(source);
        }
    }

    protected byte[] write() {
        return ObjectUtils.isAnyNull(this.value) ? null : ObjectUtils.transferToByteArray(this.value);
    }
}