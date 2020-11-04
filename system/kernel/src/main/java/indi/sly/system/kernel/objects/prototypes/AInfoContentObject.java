package indi.sly.system.kernel.objects.prototypes;

import indi.sly.system.common.functions.Consumer;
import indi.sly.system.common.functions.Provider;
import indi.sly.system.kernel.core.ACoreObject;
import indi.sly.system.kernel.objects.entities.InfoEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class AInfoContentObject extends ABytesProcessObject {
}
