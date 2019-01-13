package com.dvelopp.agenttest;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection.Eager;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class Main {

    public static void premain(String agentOps, Instrumentation inst) {
        instrument(agentOps, inst);
    }

    public static void agentmain(String agentOps, Instrumentation inst) {
        instrument(agentOps, inst);
    }

    private static void instrument(String agentOps, Instrumentation inst) {
        new AgentBuilder.Default().with(new Eager())
                .ignore(ElementMatchers.nameContains("com.dvelopp.agenttest"))
                .or(ElementMatchers.nameContains("liquibase"))
                .or(ElementMatchers.nameContains("sun"))
                .or(ElementMatchers.nameContains("jdk"))
                .or(ElementMatchers.nameContains("java"))
                .or(ElementMatchers.nameContains("ConcurrentReferenceHashMap"))
                .or(ElementMatchers.hasAnnotation(ElementMatchers.annotationType(ElementMatchers.nameContains("SpringBootApplication"))))
                .type((ElementMatchers.any()))
                .transform((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(Interceptor.class)))
                .installOn(inst);
    }

    public static class Interceptor {

        @RuntimeType
        public static Object intercept(@SuperCall Callable<?> superCall, @SuperMethod Method superMethod, @Origin Method currentMethod,
                                       @AllArguments Object[] args, @This(optional = true) Object me) throws Exception {
            return superCall.call();
        }
    }

}
