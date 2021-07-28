package indi.sly.system.kernel.processes.prototypes.processors;


import indi.sly.system.kernel.core.prototypes.processors.IOrderlyResolver;
import indi.sly.system.kernel.processes.prototypes.wrappers.ProcessLifeCycleProcessorMediator;

public interface IProcessKillerResolver extends IOrderlyResolver {
    void resolve(ProcessLifeCycleProcessorMediator processorCreatorMediator);
}