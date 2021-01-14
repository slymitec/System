package indi.sly.system.kernel.objects.prototypes.processors;

import indi.sly.system.common.lang.Consumer3;
import indi.sly.system.common.lang.Function6;
import indi.sly.system.common.supports.StringUtil;
import indi.sly.system.common.supports.ValueUtil;
import indi.sly.system.kernel.core.prototypes.ACorePrototype;
import indi.sly.system.kernel.core.enviroment.types.SpaceTypes;
import indi.sly.system.kernel.memory.caches.prototypes.InfoCacheObject;
import indi.sly.system.common.values.IdentificationDefinition;
import indi.sly.system.kernel.objects.values.InfoEntity;
import indi.sly.system.kernel.objects.prototypes.InfoObject;
import indi.sly.system.kernel.objects.prototypes.InfoProcessorRegister;
import indi.sly.system.kernel.objects.values.InfoStatusDefinition;
import indi.sly.system.kernel.objects.types.InfoStatusOpenAttributeTypes;
import indi.sly.system.kernel.objects.infotypes.types.TypeInitializerAttributeTypes;
import indi.sly.system.kernel.objects.infotypes.prototypes.TypeObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OpenOrCloseProcessor extends ACorePrototype implements IInfoObjectProcessor {
    public OpenOrCloseProcessor() {
        this.open = (handle, info, type, status, openAttribute, arguments) -> {
            status.getOpen().setAttribute(openAttribute);

            if (!type.isTypeInitializerAttributeExist(TypeInitializerAttributeTypes.DONOT_USE_TYPE_COUNT)) {
                type.addTotalOccupiedCount();
            }

            info.setOpened(info.getOpened() + 1);
            info.setOccupied(info.getOccupied() + 1);

            return handle;
        };

        this.close = (info, type, status) -> {
            status.getOpen().setAttribute(InfoStatusOpenAttributeTypes.CLOSE);

            if (!type.isTypeInitializerAttributeExist(TypeInitializerAttributeTypes.DONOT_USE_TYPE_COUNT)) {
                type.minusTotalOccupiedCount();
            }

            info.setOpened(info.getOpened() - 1);
            info.setOccupied(info.getOccupied() - 1);

            if (!ValueUtil.isAnyNullOrEmpty(status.getParentID())) {
                if (type.isTypeInitializerAttributeExist(TypeInitializerAttributeTypes.TEMPORARY) && info.getOpened() <= 0) {
                    IdentificationDefinition identification;
                    if (StringUtil.isNameIllegal(info.getName())) {
                        identification = new IdentificationDefinition(info.getName());
                    } else {
                        identification = new IdentificationDefinition(info.getID());
                    }

                    InfoCacheObject infoObject = this.factoryManager.getCoreRepository().get(SpaceTypes.KERNEL, InfoCacheObject.class);

                    InfoObject parentInfo = infoObject.getIfExisted(SpaceTypes.ALL, status.getParentID());
                    parentInfo.deleteChild(identification);
                }
            }
        };
    }

    private final Function6<UUID, UUID, InfoEntity, TypeObject, InfoStatusDefinition, Long, Object[]> open;
    private final Consumer3<InfoEntity, TypeObject, InfoStatusDefinition> close;

    @Override
    public void process(InfoEntity info, InfoProcessorRegister processorRegister) {
        processorRegister.getOpens().add(this.open);
        processorRegister.getCloses().add(this.close);
    }

}
