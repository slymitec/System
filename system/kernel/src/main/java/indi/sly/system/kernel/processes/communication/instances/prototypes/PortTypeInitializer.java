package indi.sly.system.kernel.processes.communication.instances.prototypes;

import indi.sly.system.common.exceptions.StatusNotSupportedException;
import indi.sly.system.common.utility.ObjectUtils;
import indi.sly.system.kernel.objects.Identification;
import indi.sly.system.kernel.objects.entities.InfoEntity;
import indi.sly.system.kernel.objects.entities.InfoSummaryDefinition;
import indi.sly.system.kernel.objects.prototypes.AInfoContentObject;
import indi.sly.system.kernel.objects.definitions.DumpDefinition;
import indi.sly.system.kernel.objects.definitions.InfoStatusOpenDefinition;
import indi.sly.system.kernel.objects.infotypes.prototypes.ATypeInitializer;
import indi.sly.system.kernel.processes.ProcessManager;
import indi.sly.system.kernel.processes.communication.instances.definitions.PortDefinition;
import indi.sly.system.kernel.processes.prototypes.ProcessObject;
import indi.sly.system.kernel.processes.types.ProcessTokenLimitTypes;
import indi.sly.system.kernel.processes.prototypes.ProcessTokenObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PortTypeInitializer extends ATypeInitializer {
    @Override
    public void install() {
    }

    @Override
    public void uninstall() {
    }

    @Override
    public UUID getPoolID(UUID id, UUID type) {
        return this.factoryManager.getKernelSpace().getConfiguration().MEMORY_REPOSITORIES_DATABASEENTITYREPOSITORYOBJECT_ID;
    }

    @Override
    public void createProcedure(InfoEntity info) {
        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);
        ProcessObject process = processManager.getCurrentProcess();
        ProcessTokenObject processToken = process.getToken();

        PortDefinition port = new PortDefinition();

        port.setProcessID(process.getID());
        port.setLimit(processToken.getLimits().get(ProcessTokenLimitTypes.PORT_LENGTH_MAX));

        info.setContent(ObjectUtils.transferToByteArray(port));
    }

    @Override
    public void deleteProcedure(InfoEntity info) {
    }

    @Override
    public void getProcedure(InfoEntity info) {
    }

    @Override
    public void dumpProcedure(InfoEntity info, DumpDefinition dump) {
    }

    @Override
    public void openProcedure(InfoEntity info, InfoStatusOpenDefinition statusOpen, long openAttribute,
                              Object... arguments) {
    }

    @Override
    public void closeProcedure(InfoEntity info, InfoStatusOpenDefinition statusOpen) {
    }

    @Override
    public void createChildProcedure(InfoEntity info, InfoEntity childInfo) {
        throw new StatusNotSupportedException();
    }

    @Override
    public InfoSummaryDefinition getChildProcedure(InfoEntity info, Identification identification) {
        throw new StatusNotSupportedException();
    }

    @Override
    public Set<InfoSummaryDefinition> queryChildProcedure(InfoEntity info, Predicate<InfoSummaryDefinition> wildcard) {
        throw new StatusNotSupportedException();
    }

    @Override
    public void renameChildProcedure(InfoEntity info, Identification oldIdentification,
                                     Identification newIdentification) {
        throw new StatusNotSupportedException();
    }

    @Override
    public void deleteChildProcedure(InfoEntity info, Identification identification) {
        throw new StatusNotSupportedException();
    }

    @Override
    public Class<? extends AInfoContentObject> getContentTypeProcedure(InfoEntity info,
                                                                       InfoStatusOpenDefinition statusOpen) {
        return PortContentObject.class;
    }

    @Override
    public void refreshPropertiesProcedure(InfoEntity info, InfoStatusOpenDefinition statusOpen) {
    }
}