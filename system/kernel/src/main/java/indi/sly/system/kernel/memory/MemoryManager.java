package indi.sly.system.kernel.memory;

import indi.sly.system.common.exceptions.ConditionParametersException;
import indi.sly.system.common.utility.ObjectUtils;
import indi.sly.system.common.utility.UUIDUtils;
import indi.sly.system.kernel.core.AManager;
import indi.sly.system.kernel.core.boot.StartupTypes;
import indi.sly.system.kernel.core.enviroment.SpaceTypes;
import indi.sly.system.kernel.memory.caches.InfoObjectCacheObject;
import indi.sly.system.kernel.memory.repositories.AInfoRepositoryObject;
import indi.sly.system.kernel.memory.repositories.AccountGroupRepositoryObject;
import indi.sly.system.kernel.memory.repositories.DatabaseInfoRepositoryObject;
import indi.sly.system.kernel.memory.repositories.ProcessRepositoryObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MemoryManager extends AManager {
    @Override
    public void startup(long startupTypes) {
        if (startupTypes == StartupTypes.STEP_INIT) {
            this.factoryManager.getCoreObjectRepository().add(SpaceTypes.KERNEL, ObjectUtils.getObject(InfoObjectCacheObject.class));
            this.factoryManager.getCoreObjectRepository().addByID(SpaceTypes.KERNEL, this.factoryManager.getKernelSpace().getConfiguration().MEMORY_REPOSITORIES_DATABASEENTITYREPOSITORYOBJECT_ID, ObjectUtils.getObject(DatabaseInfoRepositoryObject.class));

            this.factoryManager.getCoreObjectRepository().add(SpaceTypes.KERNEL, ObjectUtils.getObject(ProcessRepositoryObject.class));
            this.factoryManager.getCoreObjectRepository().add(SpaceTypes.KERNEL, ObjectUtils.getObject(AccountGroupRepositoryObject.class));
        } else if (startupTypes == StartupTypes.STEP_KERNEL) {
        }
    }

    public AInfoRepositoryObject getInfoRepository(UUID id) {
        if (UUIDUtils.isAnyNullOrEmpty(id)) {
            throw new ConditionParametersException();
        }

        return this.factoryManager.getCoreObjectRepository().getByID(SpaceTypes.KERNEL, AInfoRepositoryObject.class, id);
    }

    public ProcessRepositoryObject getProcessRepository() {
        return this.factoryManager.getCoreObjectRepository().get(SpaceTypes.KERNEL, ProcessRepositoryObject.class);
    }

    public AccountGroupRepositoryObject getAccountGroupRepository() {
        return this.factoryManager.getCoreObjectRepository().get(SpaceTypes.KERNEL, AccountGroupRepositoryObject.class);
    }
}
