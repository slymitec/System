package indi.sly.system.kernel.processes.prototypes;

import indi.sly.system.kernel.core.prototypes.APrototype;
import indi.sly.system.kernel.processes.lang.KillProcessFunction;
import indi.sly.system.kernel.processes.prototypes.wrappers.ProcessLifeCycleProcessorMediator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.List;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessKillerBuilder extends APrototype {
    protected ProcessFactory factory;
    protected ProcessLifeCycleProcessorMediator processorMediator;

    protected ProcessObject process;
    protected ProcessObject parentProcess;

    public void build() {
        ProcessStatusObject processStatus = process.getStatus();

        processStatus.die();

        List<KillProcessFunction> resolvers = this.processorMediator.getKills();

        for (KillProcessFunction resolver : resolvers) {
            process = resolver.apply(this.parentProcess, this.process);
        }

        processStatus.zombie();
    }
}
