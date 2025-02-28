package com.xm.util.valid;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
    解决非controller层数据校验问题， @Validated、@Valid
 */
public class ValidationUtils {

    private static final Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 校验对象
     *
     * @param object 待校验对象
     * @param groups 待校验的组
     */
    public static void validateEntity(Object object, Class<?>... groups) throws IllegalArgumentException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            String msg = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("||"));
            throw new CommonException(msg);
        }
    }

    /**
     * 校验对象
     * @param object 待校验对象
     * @param ignorePropertyList 校验忽略属性
     * @param groups 待校验的组
     */
    public static void validateEntityIgnore(Object object, List<String> ignorePropertyList, Class<?>... groups) throws IllegalArgumentException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        constraintViolations=constraintViolations.stream()
                .filter(item->!ignorePropertyList.contains(item.getPropertyPath().toString())).collect(Collectors.toSet());
        if (!constraintViolations.isEmpty()) {
            String msg = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("||"));
            throw new CommonException(msg);
        }
    }

    public static <T> void  validateEntityListIgnore(List<T> objectList, List<String> ignorePropertyList, Class<?>... groups){
        if (CollectionUtil.isEmpty(objectList)) {
            return;
        }
        for (int i = 0; i < objectList.size(); i++) {
            T object = objectList.get(i);
            ValidationResult validationResult = ValidationUtils.validateEntityWithResultIgnore(object, ignorePropertyList);
            if (!validationResult.getIsLegal()) {
                throw new CommonException(StrUtil.format("第{}行数据错误=>{}", (i + 1), validationResult.getMsg()));
            }
        }
    }

    public static ValidationResult validateEntityWithResult(Object object, Class<?>... groups) throws IllegalArgumentException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        ValidationResult validationResult=new ValidationResult();
        if (!constraintViolations.isEmpty()) {
            String msg = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("||"));
            validationResult.setIsLegal(false);
            validationResult.setMsg(msg);
        }else {
            validationResult.setIsLegal(true);
        }
        return validationResult;
    }


    public static ValidationResult validateEntityWithResultIgnore(Object object, List<String> ignorePropertyList, Class<?>... groups) throws IllegalArgumentException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        ValidationResult validationResult=new ValidationResult();
        constraintViolations=constraintViolations.stream().filter(item->!ignorePropertyList.contains(item.getPropertyPath().toString())).collect(Collectors.toSet());
        if (!constraintViolations.isEmpty()) {
            String msg = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("||"));
            validationResult.setIsLegal(false);
            validationResult.setMsg(msg);
        }else {
            validationResult.setIsLegal(true);
        }
        return validationResult;
    }
}

