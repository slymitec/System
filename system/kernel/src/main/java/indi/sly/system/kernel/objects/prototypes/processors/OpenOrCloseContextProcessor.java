package indi.sly.system.kernel.objects.prototypes.processors;

import indi.sly.system.common.functions.Consumer3;
import indi.sly.system.common.functions.Function6;
import indi.sly.system.common.utility.StringUtils;
import indi.sly.system.common.utility.UUIDUtils;
import indi.sly.system.kernel.core.prototypes.ACoreObject;
import indi.sly.system.kernel.core.enviroment.SpaceTypes;
import indi.sly.system.kernel.memory.caches.InfoObjectCacheObject;
import indi.sly.system.kernel.objects.Identification;
import indi.sly.system.kernel.objects.entities.InfoEntity;
import indi.sly.system.kernel.objects.prototypes.InfoObject;
import indi.sly.system.kernel.objects.prototypes.InfoObjectProcessorRegister;
import indi.sly.system.kernel.objects.prototypes.StatusDefinition;
import indi.sly.system.kernel.objects.prototypes.StatusOpenDefinitionOpenAttributeTypes;
import indi.sly.system.kernel.objects.types.TypeInitializerAttributeTypes;
import indi.sly.system.kernel.objects.types.TypeObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OpenOrCloseContextProcessor extends ACoreObject implements IInfoObjectProcessor {
    public OpenOrCloseContextProcessor() {
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
            status.getOpen().setAttribute(StatusOpenDefinitionOpenAttributeTypes.CLOSE);

            if (!type.isTypeInitializerAttributeExist(TypeInitializerAttributeTypes.DONOT_USE_TYPE_COUNT)) {
                type.minusTotalOccupiedCount();
            }

            info.setOpened(info.getOpened() - 1);
            info.setOccupied(info.getOccupied() - 1);

            if (!UUIDUtils.isAnyNullOrEmpty(status.getParentID())) {
                if (type.isTypeInitializerAttributeExist(TypeInitializerAttributeTypes.TEMPORARY) && info.getOpened() <= 0) {
                    Identification identification;
                    if (StringUtils.isNameIllegal(info.getName())) {
                        identification = new Identification(info.getName());
                    } else {
                        identification = new Identification(info.getID());
                    }

                    InfoObjectCacheObject infoCacheObject = this.factoryManager.getCoreObjectRepository().get(SpaceTypes.KERNEL, InfoObjectCacheObject.class);

                    InfoObject parentInfo = infoCacheObject.getIfExisted(SpaceTypes.ALL, status.getParentID());
                    parentInfo.deleteChild(identification);
                }
            }
        };
    }

    private final Function6<UUID, UUID, InfoEntity, TypeObject, StatusDefinition, Long, Object[]> open;
    private final Consumer3<InfoEntity, TypeObject, StatusDefinition> close;

    @Override
    public void postProcess(InfoEntity info, InfoObjectProcessorRegister processorRegister) {
        processorRegister.getOpens().add(this.open);
        processorRegister.getCloses().add(this.close);
    }

}
