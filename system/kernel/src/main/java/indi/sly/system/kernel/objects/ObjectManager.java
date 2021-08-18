package indi.sly.system.kernel.objects;

import indi.sly.system.common.lang.ConditionParametersException;
import indi.sly.system.common.supports.ObjectUtil;
import indi.sly.system.common.values.IdentificationDefinition;
import indi.sly.system.kernel.core.AManager;
import indi.sly.system.kernel.core.boot.values.StartupType;
import indi.sly.system.kernel.objects.prototypes.InfoFactory;
import indi.sly.system.kernel.objects.prototypes.InfoObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.List;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ObjectManager extends AManager {
    protected InfoFactory factory;

    @Override
    public void startup(long startup) {
        if (startup == StartupType.STEP_INIT) {
            this.factory = this.factoryManager.create(InfoFactory.class);
            this.factory.init();
        } else if (startup == StartupType.STEP_KERNEL) {
            this.factory.buildRootInfo();
        }
    }

    @Override
    public void shutdown() {
    }

    public InfoObject get(List<IdentificationDefinition> identifications) {
        if (ObjectUtil.isAnyNull(identifications) || identifications.size() > 256) {
            throw new ConditionParametersException();
        }

        InfoObject info = this.factory.getRootInfo();

        for (IdentificationDefinition identification : identifications) {
            info = info.getChild(identification);
        }

        return info;
    }
}
