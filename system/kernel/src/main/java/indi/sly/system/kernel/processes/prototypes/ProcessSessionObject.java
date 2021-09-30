package indi.sly.system.kernel.processes.prototypes;

import indi.sly.system.common.lang.*;
import indi.sly.system.common.supports.LogicalUtil;
import indi.sly.system.common.supports.StringUtil;
import indi.sly.system.common.supports.ValueUtil;
import indi.sly.system.common.values.IdentificationDefinition;
import indi.sly.system.common.values.LockType;
import indi.sly.system.kernel.core.enviroment.values.KernelConfigurationDefinition;
import indi.sly.system.kernel.core.prototypes.ABytesValueProcessObject;
import indi.sly.system.kernel.objects.ObjectManager;
import indi.sly.system.kernel.objects.prototypes.InfoObject;
import indi.sly.system.kernel.objects.values.InfoOpenAttributeType;
import indi.sly.system.kernel.processes.ProcessManager;
import indi.sly.system.kernel.processes.instances.prototypes.SessionContentObject;
import indi.sly.system.kernel.processes.values.ProcessSessionDefinition;
import indi.sly.system.kernel.processes.values.ProcessStatusType;
import indi.sly.system.kernel.security.prototypes.SecurityDescriptorObject;
import indi.sly.system.kernel.security.values.AccessControlDefinition;
import indi.sly.system.kernel.security.values.AccessControlScopeType;
import indi.sly.system.kernel.security.values.PermissionType;
import indi.sly.system.kernel.security.values.UserType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessSessionObject extends ABytesValueProcessObject<ProcessSessionDefinition, ProcessObject> {
    public void create(String name) {
        if (StringUtil.isNameIllegal(name)) {
            throw new ConditionParametersException();
        }

        if (!this.parent.isCurrent() || LogicalUtil.allNotEqual(this.parent.getStatus().get(),
                ProcessStatusType.RUNNING)) {
            throw new StatusRelationshipErrorException();
        }

        ProcessTokenObject processToken = this.parent.getToken();
        UUID accountID = processToken.getAccountID();

        KernelConfigurationDefinition kernelConfiguration = this.factoryManager.getKernelSpace().getConfiguration();

        ObjectManager objectManager = this.factoryManager.getManager(ObjectManager.class);

        UUID sessionID;

        try {
            this.lock(LockType.WRITE);
            this.init();

            if (!ValueUtil.isAnyNullOrEmpty(this.value.getSessionID())) {
                throw new StatusAlreadyFinishedException();
            }

            InfoObject sessionsInfo = objectManager.get(List.of(new IdentificationDefinition("Sessions"),
                    new IdentificationDefinition(accountID)));

            InfoObject sessionInfo = sessionsInfo.createChildAndOpen(kernelConfiguration.PROCESSES_SESSION_INSTANCE_ID,
                    new IdentificationDefinition(UUID.randomUUID()), InfoOpenAttributeType.OPEN_SHARED_WRITE);

            SecurityDescriptorObject securityDescriptor = sessionInfo.getSecurityDescriptor();
            Set<AccessControlDefinition> permissions = new HashSet<>();
            AccessControlDefinition permission = new AccessControlDefinition();
            permission.getUserID().setID(accountID);
            permission.getUserID().setType(UserType.ACCOUNT);
            permission.setScope(AccessControlScopeType.THIS);
            permission.setValue(LogicalUtil.or(PermissionType.LISTCHILD_READDATA_ALLOW, PermissionType.CREATECHILD_WRITEDATA_ALLOW));
            permissions.add(permission);
            permission = new AccessControlDefinition();
            permission.getUserID().setID(sessionInfo.getID());
            permission.getUserID().setType(UserType.SESSION);
            permission.setScope(AccessControlScopeType.THIS);
            permission.setValue(LogicalUtil.or(PermissionType.LISTCHILD_READDATA_ALLOW, PermissionType.CREATECHILD_WRITEDATA_ALLOW));
            permissions.add(permission);
            securityDescriptor.setPermissions(permissions);

            SessionContentObject sessionContent = (SessionContentObject) sessionInfo.getContent();
            sessionContent.setName(name);
            sessionContent.addProcessID(this.parent.getID());

            ProcessInfoTableObject processInfoTable = this.parent.getInfoTable();
            ProcessInfoEntryObject processInfoEntry = processInfoTable.getByID(sessionInfo.getID());
            processInfoEntry.setUnsupportedDelete(true);

            sessionID = sessionInfo.getID();

            this.value.setAccountID(accountID);
            this.value.setSessionID(sessionID);

            this.fresh();
        } finally {
            this.lock(LockType.NONE);
        }
    }

    public void close() {
        if (!this.parent.isCurrent() || LogicalUtil.allNotEqual(this.parent.getStatus().get(),
                ProcessStatusType.DIED)) {
            throw new StatusRelationshipErrorException();
        }

        try {
            this.lock(LockType.WRITE);
            this.init();

            SessionContentObject sessionContent = this.getContent();
            sessionContent.deleteProcessID(this.parent.getID());

            UUID sessionID = this.value.getSessionID();

            ProcessInfoTableObject processInfoTable = this.parent.getInfoTable();
            if (processInfoTable.containByID(sessionID)) {
                ProcessInfoEntryObject processInfoEntry = processInfoTable.getByID(sessionID);
                processInfoEntry.setUnsupportedDelete(false);

                InfoObject info = processInfoEntry.getInfo();
                info.close();
            }

            this.value.setAccountID(null);
            this.value.setSessionID(null);

            this.fresh();
        } finally {
            this.lock(LockType.NONE);
        }
    }

    public UUID getID() {
        try {
            this.lock(LockType.READ);
            this.init();

            return this.value.getSessionID();
        } finally {
            this.lock(LockType.NONE);
        }
    }

    public void inheritID() {
        if (this.parent.isCurrent() || LogicalUtil.allNotEqual(this.parent.getStatus().get(),
                ProcessStatusType.INITIALIZATION)) {
            throw new StatusRelationshipErrorException();
        }

        ProcessManager processManager = this.factoryManager.getManager(ProcessManager.class);
        ProcessObject process = processManager.getCurrent();

        if (!process.getID().equals(parent.getParentID())) {
            throw new ConditionRefuseException();
        }

        ProcessSessionObject processSession = process.getSession();
        ProcessTokenObject processToken = process.getToken();
        UUID accountID = processToken.getAccountID();
        UUID sessionID = processSession.getID();

        ObjectManager objectManager = this.factoryManager.getManager(ObjectManager.class);

        try {
            this.lock(LockType.WRITE);
            this.init();

            if (!ValueUtil.isAnyNullOrEmpty(sessionID)) {
                List<IdentificationDefinition> identifications = List.of(new IdentificationDefinition("Sessions"),
                        new IdentificationDefinition(accountID), new IdentificationDefinition(sessionID));

                InfoObject sessionInfo = objectManager.get(identifications);

                sessionInfo.open(InfoOpenAttributeType.OPEN_SHARED_WRITE);

                SessionContentObject sessionContent = (SessionContentObject) sessionInfo.getContent();
                sessionContent.addProcessID(this.parent.getID());

                ProcessInfoTableObject processInfoTable = this.parent.getInfoTable();
                ProcessInfoEntryObject processInfoEntry = processInfoTable.getByID(sessionInfo.getID());
                processInfoEntry.setUnsupportedDelete(true);
            }

            this.value.setAccountID(accountID);
            this.value.setSessionID(sessionID);

            this.fresh();
        } finally {
            this.lock(LockType.NONE);
        }
    }

    public void setID(UUID sessionID) {
        if (ValueUtil.isAnyNullOrEmpty(sessionID)) {
            throw new ConditionParametersException();
        }

        if (LogicalUtil.allNotEqual(this.parent.getStatus().get(), ProcessStatusType.INITIALIZATION,
                ProcessStatusType.RUNNING)) {
            throw new StatusRelationshipErrorException();
        }

        ProcessTokenObject processToken = this.parent.getToken();
        UUID accountID = processToken.getAccountID();

        ObjectManager objectManager = this.factoryManager.getManager(ObjectManager.class);

        try {
            this.lock(LockType.WRITE);
            this.init();

            InfoObject newSessionInfo = objectManager.get(List.of(new IdentificationDefinition("Sessions"),
                    new IdentificationDefinition(accountID), new IdentificationDefinition(sessionID)));

            newSessionInfo.open(InfoOpenAttributeType.OPEN_SHARED_WRITE);

            SessionContentObject newSessionContent = (SessionContentObject) newSessionInfo.getContent();
            newSessionContent.addProcessID(this.parent.getID());

            if (!ValueUtil.isAnyNullOrEmpty(this.value.getAccountID(), this.value.getSessionID())) {
                InfoObject oldSessionInfo = objectManager.get(List.of(new IdentificationDefinition("Sessions"),
                        new IdentificationDefinition(this.value.getAccountID()), new IdentificationDefinition(this.value.getSessionID())));

                SessionContentObject oldSessionContent = (SessionContentObject) oldSessionInfo.getContent();
                oldSessionContent.deleteProcessID(this.parent.getID());

                ProcessInfoTableObject processInfoTable = this.parent.getInfoTable();
                if (processInfoTable.containByID(this.value.getSessionID())) {
                    ProcessInfoEntryObject processInfoEntry = processInfoTable.getByID(this.value.getSessionID());
                    processInfoEntry.setUnsupportedDelete(false);

                    InfoObject info = processInfoEntry.getInfo();
                    info.close();
                }

                this.value.setAccountID(null);
                this.value.setSessionID(null);
            }

            ProcessInfoTableObject processInfoTable = this.parent.getInfoTable();
            ProcessInfoEntryObject processInfoEntry = processInfoTable.getByID(newSessionInfo.getID());
            processInfoEntry.setUnsupportedDelete(true);

            this.value.setAccountID(accountID);
            this.value.setSessionID(sessionID);

            this.fresh();
        } finally {
            this.lock(LockType.NONE);
        }
    }

    public SessionContentObject getContent() {
        if (LogicalUtil.allNotEqual(this.parent.getStatus().get(), ProcessStatusType.INITIALIZATION,
                ProcessStatusType.RUNNING)) {
            throw new StatusRelationshipErrorException();
        }

        ObjectManager objectManager = this.factoryManager.getManager(ObjectManager.class);

        try {
            this.lock(LockType.READ);
            this.init();

            UUID accountID = this.value.getAccountID();
            UUID sessionID = this.value.getSessionID();

            if (ValueUtil.isAnyNullOrEmpty(accountID, sessionID)) {
                throw new StatusRelationshipErrorException();
            }

            List<IdentificationDefinition> identifications = List.of(new IdentificationDefinition("Sessions"),
                    new IdentificationDefinition(accountID), new IdentificationDefinition(sessionID));

            InfoObject sessionInfo = objectManager.get(identifications);

            SecurityDescriptorObject sessionSecurityDescriptor = sessionInfo.getSecurityDescriptor();

            boolean allow;
            try {
                sessionSecurityDescriptor.checkPermission(PermissionType.LISTCHILD_READDATA_ALLOW);
                sessionSecurityDescriptor.checkPermission(PermissionType.CREATECHILD_WRITEDATA_ALLOW);

                allow = true;
            } catch (AKernelException ignored) {
                allow = false;
            }

            if (!allow) {
                sessionSecurityDescriptor.setInherit(true);
                Set<AccessControlDefinition> permissions = new HashSet<>();
                AccessControlDefinition permission = new AccessControlDefinition();
                permission.getUserID().setID(accountID);
                permission.getUserID().setType(UserType.ACCOUNT);
                permission.setScope(AccessControlScopeType.THIS);
                permission.setValue(LogicalUtil.or(PermissionType.LISTCHILD_READDATA_ALLOW, PermissionType.CREATECHILD_WRITEDATA_ALLOW));
                permissions.add(permission);
                permission = new AccessControlDefinition();
                permission.getUserID().setID(sessionInfo.getID());
                permission.getUserID().setType(UserType.SESSION);
                permission.setScope(AccessControlScopeType.THIS);
                permission.setValue(LogicalUtil.or(PermissionType.LISTCHILD_READDATA_ALLOW, PermissionType.CREATECHILD_WRITEDATA_ALLOW));
                permissions.add(permission);
                sessionSecurityDescriptor.setPermissions(permissions);
            }

            return (SessionContentObject) sessionInfo.getContent();
        } finally {
            this.lock(LockType.NONE);
        }
    }
}
