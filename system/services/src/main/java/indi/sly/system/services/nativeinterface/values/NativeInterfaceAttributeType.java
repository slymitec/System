package indi.sly.system.services.nativeinterface.values;

public interface NativeInterfaceAttributeType {
    long CAN_BE_CALLED_BY_THIRD_PARTY = 1L;
    long RUNNING_CONTEXT_IS_NOT_REQUIRED = 1L << 1;
    long SUPPORT = 1L << 2;
}