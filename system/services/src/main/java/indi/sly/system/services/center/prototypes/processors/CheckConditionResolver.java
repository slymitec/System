package indi.sly.system.services.center.prototypes.processors;

import indi.sly.system.common.lang.StatusNotReadyException;
import indi.sly.system.common.lang.StatusRelationshipErrorException;
import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.services.center.lang.CenterProcessorFinishConsumer;
import indi.sly.system.services.center.lang.CenterProcessorContentFunction;
import indi.sly.system.services.center.lang.CenterProcessorRunConsumer;
import indi.sly.system.services.center.lang.CenterProcessorStartFunction;
import indi.sly.system.services.center.prototypes.wrappers.CenterProcessorMediator;
import indi.sly.system.services.center.values.CenterDefinition;
import indi.sly.system.services.center.values.CenterStatusRuntimeType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CheckConditionResolver extends APrototype implements ICenterResolver {
    public CheckConditionResolver() {
        this.start = (center, status) -> {
            if (status.getRuntime() != CenterStatusRuntimeType.INITIALIZATION) {
                throw new StatusRelationshipErrorException();
            }
        };

        this.finish = (center, status) -> {
            if (status.getRuntime() != CenterStatusRuntimeType.RUNNING) {
                throw new StatusRelationshipErrorException();
            }
        };

        this.run = (center, status, name, run, content) -> {
            if (status.getRuntime() != CenterStatusRuntimeType.RUNNING) {
                throw new StatusRelationshipErrorException();
            }

            if (content.isException()) {
                throw new StatusNotReadyException();
            }
        };

        this.content = (center, status, threadRun) -> {
            if (status.getRuntime() != CenterStatusRuntimeType.RUNNING) {
                throw new StatusRelationshipErrorException();
            }

            return threadRun;
        };
    }

    @Override
    public int order() {
        return 0;
    }

    private final CenterProcessorStartFunction start;
    private final CenterProcessorFinishConsumer finish;
    private final CenterProcessorRunConsumer run;
    private final CenterProcessorContentFunction content;

    @Override
    public void resolve(CenterDefinition center, CenterProcessorMediator processorMediator) {
        processorMediator.getStarts().add(this.start);
        processorMediator.getFinishes().add(this.finish);
        processorMediator.getRuns().add(this.run);
        processorMediator.getContents().add(this.content);
    }
}
