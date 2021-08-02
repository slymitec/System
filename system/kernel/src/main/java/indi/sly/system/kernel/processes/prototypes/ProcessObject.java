package indi.sly.system.kernel.processes.prototypes;

import indi.sly.system.common.lang.ConditionContextException;
import indi.sly.system.common.supports.ValueUtil;
import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.kernel.memory.MemoryManager;
import indi.sly.system.kernel.memory.repositories.prototypes.ProcessRepositoryObject;
import indi.sly.system.kernel.processes.ThreadManager;
import indi.sly.system.kernel.processes.lang.ProcessProcessorReadProcessComponentFunction;
import indi.sly.system.kernel.processes.lang.ProcessProcessorWriteProcessComponentConsumer;
import indi.sly.system.kernel.processes.prototypes.wrappers.ProcessProcessorMediator;
import indi.sly.system.kernel.processes.values.ProcessEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.Set;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessObject extends APrototype {
    protected ProcessFactory factory;
    protected ProcessProcessorMediator processorMediator;

    protected UUID id;

    public UUID getID() {
        if (ValueUtil.isAnyNullOrEmpty(this.id)) {
            throw new ConditionContextException();
        }

        return this.id;
    }

    public synchronized UUID getParentID() {
        return this.getSelf().getParentProcessID();
    }

    public synchronized boolean isCurrent() {
        ThreadManager threadManager = this.factoryManager.getManager(ThreadManager.class);

        ThreadObject thread = threadManager.getCurrent();

        return this.id.equals(thread.getProcessID());
    }

    private synchronized ProcessEntity getSelf() {
        return this.processorMediator.getSelf().apply(this.id);
    }

    public synchronized ProcessStatusObject getStatus() {
        ProcessEntity process = this.getSelf();

        ProcessStatusObject processStatus = this.factoryManager.create(ProcessStatusObject.class);

        processStatus.processorMediator = this.processorMediator;
        processStatus.setSource(() -> process, (ProcessEntity source) -> {
        });
        processStatus.process = this;

        return processStatus;
    }

    public synchronized ProcessCommunicationObject getCommunication() {
        ProcessEntity process = this.getSelf();

        ProcessCommunicationObject processCommunication = this.factoryManager.create(ProcessCommunicationObject.class);

        processCommunication.setSource(() -> {
            Set<ProcessProcessorReadProcessComponentFunction> methods = this.processorMediator.getReadProcessCommunications();

            byte[] source = null;

            for (ProcessProcessorReadProcessComponentFunction method : methods) {
                source = method.apply(source, process);
            }

            return source;
        }, (byte[] source) -> {
            Set<ProcessProcessorWriteProcessComponentConsumer> resolvers = this.processorMediator.getWriteProcessCommunications();

            for (ProcessProcessorWriteProcessComponentConsumer resolver : resolvers) {
                resolver.accept(process, source);
            }
        });
        processCommunication.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processCommunication.process = this;

        return processCommunication;
    }

    public synchronized ProcessContextObject getContext() {
        ProcessEntity process = this.getSelf();

        ProcessContextObject processContext = this.factoryManager.create(ProcessContextObject.class);

        processContext.setSource(() -> {
            Set<ProcessProcessorReadProcessComponentFunction> resolvers = this.processorMediator.getReadProcessContexts();

            byte[] source = null;

            for (ProcessProcessorReadProcessComponentFunction resolver : resolvers) {
                source = resolver.apply(source, process);
            }

            return source;
        }, (byte[] source) -> {
            Set<ProcessProcessorWriteProcessComponentConsumer> resolvers = this.processorMediator.getWriteProcessContexts();

            for (ProcessProcessorWriteProcessComponentConsumer resolver : resolvers) {
                resolver.accept(process, source);
            }
        });
        processContext.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processContext.process = this;

        return processContext;
    }

    public synchronized ProcessHandleTableObject getHandleTable() {
        ProcessEntity process = this.getSelf();

        ProcessHandleTableObject processHandleTable = this.factoryManager.create(ProcessHandleTableObject.class);

        processHandleTable.setSource(() -> {
            Set<ProcessProcessorReadProcessComponentFunction> resolvers = this.processorMediator.getReadProcessHandleTables();

            byte[] source = null;

            for (ProcessProcessorReadProcessComponentFunction resolver : resolvers) {
                source = resolver.apply(source, process);
            }

            return source;
        }, (byte[] source) -> {
            Set<ProcessProcessorWriteProcessComponentConsumer> resolvers = this.processorMediator.getWriteProcessHandleTables();

            for (ProcessProcessorWriteProcessComponentConsumer resolver : resolvers) {
                resolver.accept(process, source);
            }
        });
        processHandleTable.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processHandleTable.process = this;

        return processHandleTable;
    }

    public synchronized ProcessSessionObject getSession() {
        ProcessEntity process = this.getSelf();

        ProcessSessionObject processSession = this.factoryManager.create(ProcessSessionObject.class);

        processSession.setSource(() -> process, (processEntity -> {
        }));
        processSession.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processSession.process = this;
        return processSession;
    }

    public synchronized ProcessStatisticsObject getStatistics() {
        ProcessEntity process = this.getSelf();

        ProcessStatisticsObject processStatistics = this.factoryManager.create(ProcessStatisticsObject.class);

        processStatistics.setSource(() -> {
            Set<ProcessProcessorReadProcessComponentFunction> resolvers = this.processorMediator.getReadProcessStatistics();

            byte[] source = null;

            for (ProcessProcessorReadProcessComponentFunction resolver : resolvers) {
                source = resolver.apply(source, process);
            }

            return source;
        }, (byte[] source) -> {
            Set<ProcessProcessorWriteProcessComponentConsumer> resolvers = this.processorMediator.getWriteProcessStatistics();

            for (ProcessProcessorWriteProcessComponentConsumer resolver : resolvers) {
                resolver.accept(process, source);
            }
        });
        processStatistics.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processStatistics.process = this;

        return processStatistics;
    }

    public synchronized ProcessTokenObject getToken() {
        ProcessEntity process = this.getSelf();

        ProcessTokenObject processToken = this.factoryManager.create(ProcessTokenObject.class);

        processToken.setSource(() -> {
            Set<ProcessProcessorReadProcessComponentFunction> resolvers = this.processorMediator.getReadProcessTokens();

            byte[] source = null;

            for (ProcessProcessorReadProcessComponentFunction resolver : resolvers) {
                source = resolver.apply(source, process);
            }

            return source;
        }, (byte[] source) -> {
            Set<ProcessProcessorWriteProcessComponentConsumer> resolvers = this.processorMediator.getWriteProcessTokens();

            for (ProcessProcessorWriteProcessComponentConsumer resolver : resolvers) {
                resolver.accept(process, source);
            }
        });
        processToken.setLock((lock) -> {
            MemoryManager memoryManager = this.factoryManager.getManager(MemoryManager.class);
            ProcessRepositoryObject processRepository = memoryManager.getProcessRepository();

            processRepository.lock(process, lock);
        });
        processToken.process = this;

        return processToken;
    }
}
