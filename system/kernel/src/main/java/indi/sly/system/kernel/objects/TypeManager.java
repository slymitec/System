package indi.sly.system.kernel.objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Named;

import indi.sly.system.common.lang.ConditionParametersException;
import indi.sly.system.common.supports.*;
import indi.sly.system.kernel.core.enviroment.types.SpaceTypes;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import indi.sly.system.common.lang.StatusAlreadyExistedException;
import indi.sly.system.kernel.core.AManager;
import indi.sly.system.kernel.core.boot.types.StartupTypes;
import indi.sly.system.kernel.core.enviroment.KernelConfiguration;
import indi.sly.system.kernel.objects.infotypes.prototypes.ATypeInitializer;
import indi.sly.system.kernel.objects.infotypes.values.TypeDefinition;
import indi.sly.system.kernel.objects.infotypes.types.TypeInitializerAttributeTypes;
import indi.sly.system.kernel.objects.infotypes.prototypes.TypeObject;
import indi.sly.system.kernel.objects.instances.prototypes.FolderTypeInitializer;
import indi.sly.system.kernel.objects.instances.prototypes.NamelessFolderTypeInitializer;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TypeManager extends AManager {
    @Override
    public void startup(long startupTypes) {
        if (startupTypes == StartupTypes.STEP_INIT) {
        } else if (startupTypes == StartupTypes.STEP_KERNEL) {
            KernelConfiguration kernelConfiguration = this.factoryManager.getKernelSpace().getConfiguration();

            Set<UUID> childTypes = new HashSet<>();
            childTypes.add(UUIDUtil.getEmpty());

            this.create(kernelConfiguration.OBJECTS_TYPES_INSTANCE_FOLDER_ID,
                    kernelConfiguration.OBJECTS_TYPES_INSTANCE_FOLDER_NAME,
                    LogicalUtil.or(TypeInitializerAttributeTypes.CAN_BE_SENT_AND_INHERITED,
                            TypeInitializerAttributeTypes.CAN_BE_SHARED_READ, TypeInitializerAttributeTypes.HAS_AUDIT,
                            TypeInitializerAttributeTypes.HAS_CHILD, TypeInitializerAttributeTypes.HAS_CONTENT,
                            TypeInitializerAttributeTypes.HAS_PERMISSION,
                            TypeInitializerAttributeTypes.HAS_PROPERTIES),
                    childTypes, this.factoryManager.create(FolderTypeInitializer.class));

            this.create(kernelConfiguration.OBJECTS_TYPES_INSTANCE_NAMELESSFOLDER_ID,
                    kernelConfiguration.OBJECTS_TYPES_INSTANCE_NAMELESSFOLDER_NAME,
                    LogicalUtil.or(TypeInitializerAttributeTypes.CAN_BE_SENT_AND_INHERITED,
                            TypeInitializerAttributeTypes.CAN_BE_SHARED_READ,
                            TypeInitializerAttributeTypes.CHILD_IS_NAMELESS,
                            TypeInitializerAttributeTypes.HAS_AUDIT, TypeInitializerAttributeTypes.HAS_CHILD,
                            TypeInitializerAttributeTypes.HAS_CONTENT,
                            TypeInitializerAttributeTypes.HAS_PERMISSION, TypeInitializerAttributeTypes.HAS_PROPERTIES),
                    childTypes, this.factoryManager.create(NamelessFolderTypeInitializer.class));
        }
    }

    public TypeObject get(UUID typeID) {
        if (ValueUtil.isAnyNullOrEmpty(typeID)) {
            throw new ConditionParametersException();
        }

        TypeObject type = this.factoryManager.getCoreRepository().getByID(SpaceTypes.KERNEL, TypeObject.class,
                typeID);

        return type;
    }

    public TypeObject getIDByName(String typeName) {
        if (StringUtil.isNameIllegal(typeName)) {
            throw new ConditionParametersException();
        }

        TypeObject type = this.factoryManager.getCoreRepository().getByName(SpaceTypes.KERNEL, TypeObject.class
                , "Objects_Types_" + typeName);

        return type;
    }

    public synchronized TypeObject create(UUID typeID, String typeName, long attribute, Set<UUID> childTypes,
                                          ATypeInitializer typeInitializer) {
        if (ObjectUtil.isAnyNull(typeID, childTypes, typeInitializer) || StringUtil.isNameIllegal(typeName)) {
            throw new ConditionParametersException();
        }

        TypeDefinition typeDefinition = new TypeDefinition();
        typeDefinition.setId(UUIDUtil.createRandom());
        typeDefinition.setName(typeName);
        typeDefinition.setAttribute(attribute);
        typeDefinition.setTypeInitializer(typeInitializer);
        if (ObjectUtil.allNotNull(childTypes)) {
            typeDefinition.getChildTypes().addAll(childTypes);
        }

        TypeObject typeObject = this.factoryManager.create(TypeObject.class);
        typeObject.setType(typeDefinition);

        Set<UUID> objectTypes = this.factoryManager.getKernelSpace().getPrototypeTypes();

        if (objectTypes.contains(typeID)) {
            throw new StatusAlreadyExistedException();
        }

        this.factoryManager.getCoreRepository().add(SpaceTypes.KERNEL, typeID, "Objects_Types_" + typeName,
                typeObject);
        objectTypes.add(typeID);

        typeInitializer.install();

        return typeObject;
    }

    public synchronized void delete(UUID typeID) {
        if (ObjectUtil.isAnyNull(typeID)) {
            throw new ConditionParametersException();
        }

        Set<UUID> objectTypes = this.factoryManager.getKernelSpace().getPrototypeTypes();

        TypeObject type = this.get(typeID);

        type.getTypeInitializer().uninstall();

        this.factoryManager.getCoreRepository().deleteByID(SpaceTypes.KERNEL, TypeObject.class, typeID);
        objectTypes.remove(typeID);
    }

    public Set<UUID> list() {
        Set<UUID> objectTypes = this.factoryManager.getKernelSpace().getPrototypeTypes();

        return Collections.unmodifiableSet(objectTypes);
    }
}
