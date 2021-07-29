package indi.sly.system.kernel.processes.prototypes.processors;

import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.kernel.processes.lang.EndProcessFunction;
import indi.sly.system.kernel.processes.prototypes.ProcessCommunicationObject;
import indi.sly.system.kernel.processes.prototypes.wrappers.ProcessLifeProcessorMediator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EndProcessCommunicationResolver extends APrototype implements IProcessEndResolver {
    private final EndProcessFunction endProcessFunction;

    public EndProcessCommunicationResolver() {
        this.endProcessFunction = (process, parentProcess) -> {
            ProcessCommunicationObject processCommunication = process.getCommunication();

            processCommunication.deleteAllPort();
            processCommunication.deleteSignal();

            return process;
        };
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void resolve(ProcessLifeProcessorMediator processorCreatorMediator) {
        processorCreatorMediator.getEnds().add(endProcessFunction);
    }
}
