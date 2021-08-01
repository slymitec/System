package indi.sly.system.services.center.prototypes;

import indi.sly.system.common.lang.AKernelException;
import indi.sly.system.kernel.core.prototypes.APrototype;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CenterContentObject extends APrototype {

    public boolean isException() {
        return false;
    }

    public AKernelException getException() {
        return null;
    }

    public void setException(AKernelException exception) {

    }
}
