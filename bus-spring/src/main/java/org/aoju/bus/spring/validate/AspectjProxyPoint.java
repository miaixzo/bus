package org.aoju.bus.spring.validate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

@Aspect
@Order(99)
public class AspectjProxyPoint {

    /**
     * requestMapping
     * putMapping
     * postMapping
     * patchMapping
     * modelAttribute
     * getMapping
     * deleteMapping
     * CrossOrigin
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.PatchMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.ModelAttribute)" +
            "||@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            "||@annotation(org.springframework.web.bind.annotation.CrossOrigin)" +
            "||execution(* *(@org.aoju.bus.validate.annotation.Valid (*), ..))")
    public void match() {

    }

    /**
     * 执行结果
     *
     * @param joinPoint 切点
     * @return 返回结果
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return new AutoValidateAdvice().access(new AspectjProxyChain(joinPoint));
    }

}
