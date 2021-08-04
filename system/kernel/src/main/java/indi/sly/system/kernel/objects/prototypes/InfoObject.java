package indi.sly.system.kernel.objects.prototypes;

import indi.sly.system.common.lang.*;
import indi.sly.system.common.supports.*;
import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.kernel.core.enviroment.values.SpaceType;
import indi.sly.system.kernel.memory.caches.prototypes.InfoCacheObject;
import indi.sly.system.common.values.IdentificationDefinition;
import indi.sly.system.kernel.objects.TypeManager;
import indi.sly.system.kernel.objects.lang.*;
import indi.sly.system.kernel.objects.prototypes.wrappers.InfoProcessorMediator;
import indi.sly.system.kernel.objects.values.*;
import indi.sly.system.kernel.objects.infotypes.prototypes.TypeObject;
import indi.sly.system.kernel.processes.ProcessManager;
import indi.sly.system.kernel.processes.prototypes.ProcessHandleEntryObject;
import indi.sly.system.kernel.processes.prototypes.ProcessHandleTableObject;
import indi.sly.system.kernel.processes.prototypes.ProcessObject;
import indi.sly.system.kernel.processes.prototypes.ProcessTokenObject;
import indi.sly.system.kernel.security.values.PrivilegeType;
import indi.sly.system.kernel.security.prototypes.SecurityDescriptorObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.*;
import java.util.function.Predicate;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InfoObject extends APrototype {
    protected InfoFactory factory;
    protected InfoProcessorMediator processorMediator;

    protected UUID id;
    protected UUID poolID;
    protected InfoStatusDefinition status;

    public UUID getID() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.id;
    }

    public UUID getParentID() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.status.getParentID();
    }

    public UUID getType() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.getSelf().getType();
    }

    public long getOccupied() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.getSelf().getOccupied();
    }

    public long getOpened() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.getSelf().getOpened();
    }

    public String getName() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.getSelf().getName();
    }

    public List<IdentificationDefinition> getIdentifications() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return CollectionUtil.unmodifiable(this.status.getIdentifications());
    }

    private synchronized void occupy() {
        InfoEntity info = this.getSelf();
        info.setOccupied(info.getOccupied() + 1);

        InfoObject parent = this.getParent();
        if (ObjectUtil.allNotNull(parent)) {
            parent.occupy();
        }
    }

    private synchronized void free() {
        InfoEntity info = this.getSelf();
        info.setOccupied(info.getOccupied() - 1);

        InfoObject parent = this.getParent();
        if (ObjectUtil.allNotNull(parent)) {
            parent.free();
        }
    }

    private synchronized void cache(long space) {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);
        ProcessObject currentProcess = processManager.getCurrent();
        ProcessTokenObject currentProcessToken = currentProcess.getToken();

        InfoCacheObject kernelCache = this.factoryManager.getCoreRepository().get(SpaceType.KERNEL,
                InfoCacheObject.class);

        if (LogicalUtil.isAnyExist(space, SpaceType.KERNEL)) {
            if (!currentProcessToken.isPrivileges(PrivilegeType.MEMORY_CACHE_MODIFY_KERNEL_SPACE_CACHE)) {
                throw new ConditionPermissionsException();
            }

            kernelCache.add(SpaceType.KERNEL, this);
        }
        if (LogicalUtil.isAnyExist(space, SpaceType.USER)) {
            kernelCache.add(SpaceType.USER, this);
        }
    }

    private synchronized void uncache(long space) {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);
        ProcessObject currentProcess = processManager.getCurrent();
        ProcessTokenObject currentProcessToken = currentProcess.getToken();

        InfoCacheObject kernelCache = this.factoryManager.getCoreRepository().get(SpaceType.KERNEL,
                InfoCacheObject.class);

        if (LogicalUtil.isAnyExist(space, SpaceType.KERNEL)) {
            if (!currentProcessToken.isPrivileges(PrivilegeType.MEMORY_CACHE_MODIFY_KERNEL_SPACE_CACHE)) {
                throw new ConditionPermissionsException();
            }

            kernelCache.delete(SpaceType.KERNEL, this.id);
        }
        if (LogicalUtil.isAnyExist(space, SpaceType.USER)) {
            kernelCache.delete(SpaceType.USER, this.id);
        }
    }

    private synchronized InfoEntity getSelf() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.processorMediator.getSelf().apply(this.poolID, this.id, this.status);
    }

    private synchronized UUID getHandle() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);

        ProcessObject process = processManager.getCurrent();
        ProcessHandleTableObject processHandleTable = process.getHandleTable();
        ProcessHandleEntryObject processHandleTableEntry = processHandleTable.getByInfoID(this.id);

        return processHandleTableEntry.getHandle();
    }

    public synchronized InfoObject getParent() {
        InfoEntity info = this.getSelf();

        if (ValueUtil.isAnyNullOrEmpty(this.status.getParentID())) {
            return null;
        } else {
            return this.processorMediator.getParent().apply(this.status.getParentID());
        }
    }

    public synchronized Map<Long, Long> getDate() {
        InfoEntity info = this.getSelf();

        Map<Long, Long> date = ObjectUtil.transferFromByteArray(info.getDate());

        return CollectionUtil.unmodifiable(date);
    }

    public synchronized SecurityDescriptorObject getSecurityDescriptor() {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        InfoProcessorSecurityDescriptorFunction resolver = this.processorMediator.getSecurityDescriptor();

        if (ObjectUtil.isAnyNull(resolver)) {
            throw new StatusNotSupportedException();
        }

        return resolver.apply(info, type, this.status);
    }

    public synchronized DumpObject dump() {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorDumpFunction> resolvers = this.processorMediator.getDumps();

        DumpDefinition dump = new DumpDefinition();

        for (InfoProcessorDumpFunction pair : resolvers) {
            dump = pair.apply(dump, info, type, this.status);
        }

        return this.factory.buildDump(dump);
    }

    public synchronized UUID open(long openAttribute, Object... arguments) {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorOpenFunction> resolvers = this.processorMediator.getOpens();

        UUID handle = UUIDUtil.getEmpty();

        for (InfoProcessorOpenFunction resolver : resolvers) {
            handle = resolver.apply(handle, info, type, this.status, openAttribute, arguments);
            if (ObjectUtil.isAnyNull(handle)) {
                throw new StatusUnexpectedException();
            }
        }

        InfoObject parentInfo = this.getParent();
        if (ObjectUtil.allNotNull(parentInfo)) {
            parentInfo.occupy();
        }

        return handle;
    }

    public synchronized void close() {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorCloseConsumer> resolvers = this.processorMediator.getCloses();

        for (InfoProcessorCloseConsumer resolver : resolvers) {
            resolver.accept(info, type, this.status);
        }

        InfoObject parentInfo = this.getParent();
        if (ObjectUtil.allNotNull(parentInfo)) {
            parentInfo.free();
        }
    }

    public synchronized long getOpenAttribute() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);

        ProcessObject process = processManager.getCurrent();
        ProcessHandleTableObject processHandleTable = process.getHandleTable();

        if (!processHandleTable.containByInfoID(this.id)) {
            return InfoOpenAttributeType.CLOSE;
        } else {
            ProcessHandleEntryObject processHandleTableEntry = processHandleTable.getByInfoID(this.id);

            return processHandleTableEntry.getOpen().getAttribute();
        }
    }

    public synchronized InfoObject createChildAndOpen(UUID type, IdentificationDefinition identification,
                                                      long openAttribute, Object... arguments) {
        if (!ValueUtil.isAnyNullOrEmpty(this.getHandle()) || ObjectUtil.isAnyNull(identification)) {
            throw new ConditionParametersException();
        }
        if (ObjectUtil.isAnyNull(arguments)) {
            arguments = new Object[0];
        }

        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject typeObject = typeManager.get(this.getType());

        List<InfoProcessorCreateChildAndOpenFunction> resolvers = this.processorMediator.getCreateChildAndOpens();

        InfoEntity childInfo = null;

        for (InfoProcessorCreateChildAndOpenFunction resolver : resolvers) {
            childInfo = resolver.apply(childInfo, info, typeObject, this.status, type, identification);
        }

        if (ObjectUtil.isAnyNull(info)) {
            throw new StatusUnexpectedException();
        }

        InfoObject childInfoObject = this.factory.buildInfo(childInfo, this);

        childInfoObject.open(openAttribute, arguments);

        return childInfoObject;
    }

    public synchronized InfoObject getChild(IdentificationDefinition identification) {
        return this.rebuildChild(identification, null);
    }

    public synchronized InfoObject rebuildChild(IdentificationDefinition identification, InfoOpenDefinition infoOpen) {
        if (ObjectUtil.isAnyNull(identification)) {
            throw new ConditionParametersException();
        }

        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorGetOrRebuildChildFunction> resolvers = this.processorMediator.getGetOrRebuildChilds();

        InfoEntity childInfo = null;

        for (InfoProcessorGetOrRebuildChildFunction resolver : resolvers) {
            childInfo = resolver.apply(childInfo, info, type, this.status, identification, infoOpen);
        }

        if (ObjectUtil.isAnyNull(childInfo)) {
            throw new StatusUnexpectedException();
        }

        InfoCacheObject infoCache = this.factoryManager.getCoreRepository().get(SpaceType.KERNEL,
                InfoCacheObject.class);

        InfoObject childCachedInfo = infoCache.getIfExisted(SpaceType.ALL, childInfo.getID());
        if (ObjectUtil.isAnyNull(childCachedInfo)) {
            childCachedInfo = this.factory.buildInfo(childInfo, this);

            childCachedInfo.cache(SpaceType.USER);
        }
        return childCachedInfo;
    }

    public synchronized void deleteChild(IdentificationDefinition identification) {
        if (ObjectUtil.isAnyNull(identification)) {
            throw new ConditionParametersException();
        }

        InfoEntity info = this.getSelf();
        InfoObject childInfo = this.getChild(identification);

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorDeleteChildConsumer> resolvers = this.processorMediator.getDeleteChilds();

        for (InfoProcessorDeleteChildConsumer resolver : resolvers) {
            resolver.accept(info, type, this.status, identification);
        }

        childInfo.uncache(SpaceType.ALL);
    }

    public synchronized Set<InfoSummaryDefinition> queryChild(Predicate<InfoSummaryDefinition> wildcard) {
        if (ObjectUtil.isAnyNull(wildcard)) {
            throw new ConditionParametersException();
        }

        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorQueryChildFunction> resolvers = this.processorMediator.getQueryChilds();

        Set<InfoSummaryDefinition> infoSummaries = new HashSet<>();

        for (InfoProcessorQueryChildFunction resolver : resolvers) {
            infoSummaries = resolver.apply(infoSummaries, info, type, this.status, wildcard);
        }

        return infoSummaries;
    }

    public synchronized void renameChild(IdentificationDefinition oldIdentification,
                                         IdentificationDefinition newIdentification) {
        if (ObjectUtil.isAnyNull(oldIdentification, newIdentification)) {
            throw new ConditionParametersException();
        }

        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorRenameChildConsumer> resolvers = this.processorMediator.getRenameChilds();

        for (InfoProcessorRenameChildConsumer resolver : resolvers) {
            resolver.accept(info, type, this.status, oldIdentification, newIdentification);
        }
    }

    public synchronized Map<String, String> readProperties() {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorReadPropertyFunction> resolvers = this.processorMediator.getReadProperties();

        Map<String, String> properties = new HashMap<>();

        for (InfoProcessorReadPropertyFunction resolver : resolvers) {
            properties = resolver.apply(properties, info, type, this.status);
        }

        return CollectionUtil.unmodifiable(properties);
    }

    public synchronized void writeProperties(Map<String, String> properties) {
        if (ObjectUtil.isAnyNull(properties)) {
            throw new ConditionParametersException();
        }

        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        List<InfoProcessorWritePropertyConsumer> resolvers = this.processorMediator.getWriteProperties();

        for (InfoProcessorWritePropertyConsumer resolver : resolvers) {
            resolver.accept(info, type, this.status, properties);
        }
    }

    public synchronized AInfoContentObject getContent() {
        InfoEntity info = this.getSelf();

        TypeManager typeManager = this.factoryManager.getManager(TypeManager.class);
        TypeObject type = typeManager.get(this.getType());

        AInfoContentObject content = type.getInitializer().getContentProcedure(info, () -> {
            List<InfoProcessorReadContentFunction> resolvers = this.processorMediator.getReadContents();

            byte[] contentSource = null;

            for (InfoProcessorReadContentFunction resolver : resolvers) {
                contentSource = resolver.apply(contentSource, info, type, status);
            }

            return contentSource;
        }, (byte[] contentSource) -> {
            List<InfoProcessorWriteContentConsumer> resolvers = this.processorMediator.getWriteContents();

            for (InfoProcessorWriteContentConsumer resolver : resolvers) {
                resolver.accept(info, type, status, contentSource);
            }
        }, () -> {
            List<InfoProcessorExecuteContentConsumer> resolvers = this.processorMediator.getExecuteContents();

            for (InfoProcessorExecuteContentConsumer resolver : resolvers) {
                resolver.accept(info, type, status);
            }
        });

        content.setInfo(this);

        return content;
    }
}
