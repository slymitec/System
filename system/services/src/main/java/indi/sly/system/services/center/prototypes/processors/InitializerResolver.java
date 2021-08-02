package indi.sly.system.services.center.prototypes.processors;

import indi.sly.system.common.lang.AKernelException;
import indi.sly.system.common.lang.StatusNotExistedException;
import indi.sly.system.common.supports.LogicalUtil;
import indi.sly.system.common.supports.ObjectUtil;
import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.services.center.lang.*;
import indi.sly.system.services.center.prototypes.CenterContentObject;
import indi.sly.system.services.center.prototypes.wrappers.CenterProcessorMediator;
import indi.sly.system.services.center.values.CenterAttributeType;
import indi.sly.system.services.center.values.CenterDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.Map;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InitializerResolver extends APrototype implements ICenterResolver {
    public InitializerResolver() {
        this.start = (center, status) -> {
            ACenterInitializer initializer = center.getInitializer();

            initializer.start(center);
        };

        this.finish = (center, status) -> {
            ACenterInitializer initializer = center.getInitializer();

            initializer.finish(center);
        };

        this.run = (center, status, name, run, content) -> {
            ACenterInitializer initializer = center.getInitializer();
            Map<String, InitializerConsumer> initializerRuns = initializer.getRuns();

            InitializerConsumer initializerRunEntry = initializerRuns.getOrDefault(name, null);

            if (ObjectUtil.isAnyNull(initializerRunEntry)) {
                throw new StatusNotExistedException();
            }

            try {
                if (LogicalUtil.isAnyExist(center.getAttribute(), CenterAttributeType.DO_NOT_NEED_TRANSACTIONAL)) {
                    this.runEntryWithTransactional(initializerRunEntry, run, content);
                } else {
                    this.runEntryWithoutTransactional(initializerRunEntry, run, content);
                }
            } catch (AKernelException exception) {
                content.setException(exception);
            }
        };
    }

    @Override
    public int order() {
        return 2;
    }

    private final StartFunction start;
    private final FinishConsumer finish;
    private final RunConsumer run;

    @Transactional
    protected void runEntryWithTransactional(InitializerConsumer initializerRunEntry, RunSelfConsumer run,
                                             CenterContentObject content) {
        initializerRunEntry.accept(run, content);
    }

    protected void runEntryWithoutTransactional(InitializerConsumer initializerRunEntry, RunSelfConsumer run,
                                                CenterContentObject content) {
        initializerRunEntry.accept(run, content);
    }

    @Override
    public void resolve(CenterDefinition center, CenterProcessorMediator processorMediator) {
        processorMediator.getStarts().add(this.start);
        processorMediator.getFinishes().add(this.finish);
        processorMediator.getRuns().add(this.run);
    }
}
