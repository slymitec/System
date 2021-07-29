package indi.sly.system.kernel.processes.prototypes.processors;


import indi.sly.system.kernel.core.prototypes.processors.IOrderlyResolver;
import indi.sly.system.kernel.processes.prototypes.wrappers.ProcessLifeProcessorMediator;

public interface IProcessCreatorResolver extends IOrderlyResolver {
    void resolve(ProcessLifeProcessorMediator processorCreatorMediator);
}
