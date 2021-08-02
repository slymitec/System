package indi.sly.system.services.center.prototypes.processors;

import indi.sly.system.common.lang.StatusNotReadyException;
import indi.sly.system.common.lang.StatusRelationshipErrorException;
import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.kernel.processes.ThreadManager;
import indi.sly.system.kernel.processes.prototypes.ThreadContextObject;
import indi.sly.system.kernel.processes.prototypes.ThreadObject;
import indi.sly.system.services.center.lang.FinishConsumer;
import indi.sly.system.services.center.lang.GetContentFunction;
import indi.sly.system.services.center.lang.RunConsumer;
import indi.sly.system.services.center.lang.StartFunction;
import indi.sly.system.services.center.prototypes.wrappers.CenterProcessorMediator;
import indi.sly.system.services.center.values.CenterDefinition;
import indi.sly.system.services.center.values.CenterStatusRuntimeType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GetContentResolver extends APrototype implements ICenterResolver {
    public GetContentResolver() {
        this.content = (center, status, threadRun) -> {
            ThreadManager threadManager = this.factoryManager.getManager(ThreadManager.class);
            ThreadObject thread = threadManager.getCurrent();

            return thread.getContext();
        };
    }

    @Override
    public int order() {
        return 2;
    }

    private final GetContentFunction content;

    @Override
    public void resolve(CenterDefinition center, CenterProcessorMediator processorMediator) {
        processorMediator.getContents().add(this.content);
    }
}