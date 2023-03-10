package indi.sly.system.services.job.instances.prototypes.processors;

import indi.sly.system.common.lang.*;
import indi.sly.system.common.supports.*;
import indi.sly.system.common.values.MethodScopeType;
import indi.sly.system.kernel.core.enviroment.values.SpaceType;
import indi.sly.system.kernel.core.prototypes.AObject;
import indi.sly.system.services.core.values.TransactionType;
import indi.sly.system.services.job.lang.TaskRunConsumer;
import indi.sly.system.services.job.prototypes.TaskContentObject;
import indi.sly.system.services.job.values.TaskDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HandleTaskInitializer extends ATaskInitializer {
    public HandleTaskInitializer() {
        this.register("getAllHandle", this::getAllHandle, TransactionType.INDEPENDENCE);
        this.register("deleteHandle", this::deleteHandle, TransactionType.INDEPENDENCE);
        this.register("deleteAllHandle", this::deleteAllHandle, TransactionType.INDEPENDENCE);
        this.register("customHandle", this::customHandle, TransactionType.INDEPENDENCE);
    }

    @Override
    public void start(TaskDefinition task) {
    }

    @Override
    public void finish(TaskDefinition task) {
    }

    private void customHandle(TaskRunConsumer run, TaskContentObject content) {
        UUID handleID = content.getParameter(UUID.class, "handleID");
        String methodName = content.getParameter("method");
        if (ValueUtil.isAnyNullOrEmpty(handleID) || StringUtil.isNameIllegal(methodName)) {
            throw new ConditionParametersException();
        }
        String[] methodParameters = content.getParameterOrNull(String[].class, "methodParameterTypes");
        Class<?>[] methodParameterTypes;
        if (ArrayUtil.isNullOrEmpty(methodParameters)) {
            methodParameterTypes = new Class[0];
        } else {
            methodParameterTypes = new Class[methodParameters.length];

            try {
                for (int i = 0; i < methodParameters.length; i++) {
                    methodParameterTypes[i] = Class.forName(methodParameters[i]);
                }
            } catch (ClassNotFoundException e) {
                throw new StatusUnreadableException();
            }
        }

        AObject object = content.getCache(handleID);

        Class<? extends AObject> clazz = object.getClass();
        Method method;
        try {
            method = clazz.getMethod(methodName, methodParameterTypes);
        } catch (NoSuchMethodException e) {
            throw new StatusNotExistedException();
        }

        Annotation[] methodDeclaredAnnotations = method.getDeclaredAnnotations();
        for (Annotation methodDeclaredAnnotation : methodDeclaredAnnotations) {
            if (methodDeclaredAnnotation.annotationType() == MethodScope.class && (LogicalUtil.isAnyExist(((MethodScope) methodDeclaredAnnotation).value(), MethodScopeType.ONLY_KERNEL))) {
                throw new StatusNotSupportedException();
            }
        }

        methodParameterTypes = method.getParameterTypes();
        Object[] methodParameterValues = new Object[methodParameterTypes.length];
        Class<?> methodReturnType = method.getReturnType();

        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (ClassUtil.isThisOrSuperContain(methodParameterTypes[i], AObject.class)) {
                UUID handle = content.getParameter(UUID.class, content.getParameter("methodParameter" + i));
                methodParameterValues[i] = content.getCache(handle);
            } else {
                methodParameterValues[i] = content.getParameter(Object.class, content.getParameter("methodParameter" + i));
            }
        }

        Object result;
        try {
            result = method.invoke(object, methodParameterValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new StatusUnexpectedException();
        }
        if (methodReturnType != void.class) {
            if (ClassUtil.isThisOrSuperContain(methodReturnType, AObject.class)) {
                AObject resultObject = (AObject) result;
                UUID handle = resultObject.cache(SpaceType.USER);
                content.setResult("result", handle);
            } else {
                content.setResult("result", result);
            }
        }
    }

    private void getAllHandle(TaskRunConsumer run, TaskContentObject content) {
        Map<UUID, String> handleSummary = new HashMap<>();

        Set<UUID> handles = content.getAllHandle();
        for (UUID handle : handles) {
            AObject object = content.getCache(handle);
            handleSummary.put(handle, object.getClass().getName());
        }

        content.setResult("result", handleSummary);
    }

    private void deleteHandle(TaskRunConsumer run, TaskContentObject content) {
        UUID handleID = content.getParameter(UUID.class, "handleID");
        if (ValueUtil.isAnyNullOrEmpty(handleID)) {
            throw new ConditionParametersException();
        }

        content.deleteCache(handleID);
    }

    private void deleteAllHandle(TaskRunConsumer run, TaskContentObject content) {
        Set<UUID> handles = content.getAllHandle();

        for (UUID handle : handles) {
            content.deleteCache(handle);
        }
    }
}
