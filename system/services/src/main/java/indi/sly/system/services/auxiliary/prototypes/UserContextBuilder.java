package indi.sly.system.services.auxiliary.prototypes;

import indi.sly.system.common.lang.ConditionParametersException;
import indi.sly.system.common.supports.ValueUtil;
import indi.sly.system.kernel.core.prototypes.ABuilder;
import indi.sly.system.services.auxiliary.values.UserContextDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

@Named
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserContextBuilder extends ABuilder {
    protected AuxiliaryFactory factory;

    public UserContextObject create(String userRequest) {
        if (ValueUtil.isAnyNullOrEmpty(userRequest)) {
            throw new ConditionParametersException();
        }

        UserContextDefinition userContext = new UserContextDefinition();



        return this.factory.buildUserContext(userContext);
    }
}
